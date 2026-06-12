package com.lemonteastudio.httpserver.http;

import com.lemonteastudio.httpserver.exception.HttpParsingException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class HttpHeadersParserTest {

    private HttpParser httpParser;
    private Method parseHeadersMethod;

    @BeforeAll
    public void beforeClass() throws NoSuchMethodException {
        httpParser = new HttpParser();
        parseHeadersMethod = HttpParser.class.getDeclaredMethod("parseHeaders", InputStreamReader.class, HttpRequest.class);
        parseHeadersMethod.setAccessible(true);
    }

    // -- valid cases --

    @Test
    void testValidSingleHeader() throws Exception {
        HttpRequest request = new HttpRequest();
        parseHeadersMethod.invoke(httpParser, generateReader(
                "Host: localhost:8080\r\n" +
                        "\r\n"
        ), request);
        assertEquals("localhost:8080", request.getHeader("Host"));
    }

    @Test
    void testValidMultipleHeaders() throws Exception {
        HttpRequest request = new HttpRequest();
        parseHeadersMethod.invoke(httpParser, generateReader(
                "Host: localhost:8080\r\n" +
                        "Accept: */*\r\n" +
                        "Connection: keep-alive\r\n" +
                        "\r\n"
        ), request);
        assertEquals(3, request.getHeaders().size());
        assertEquals("localhost:8080", request.getHeader("Host"));
        assertEquals("*/*", request.getHeader("Accept"));
        assertEquals("keep-alive", request.getHeader("Connection"));
    }

    @Test
    void testValidHeaderWithTabOWS() throws Exception {
        HttpRequest request = new HttpRequest();
        parseHeadersMethod.invoke(httpParser, generateReader(
                "Host:\tlocalhost:8080\r\n" +
                        "\r\n"
        ), request);
        assertEquals("localhost:8080", request.getHeader("Host"));
    }

    @Test
    void testValidHeaderAllTcharFieldName() throws Exception {
        HttpRequest request = new HttpRequest();
        parseHeadersMethod.invoke(httpParser, generateReader(
                "X-Custom_Header.Name: value\r\n" +
                        "\r\n"
        ), request);
        assertEquals("value", request.getHeader("X-Custom_Header.Name"));
    }

    // -- invalid cases --

    @Test
    void testInvalidSpaceBeforeColon() {
        // RFC 7230 3.2.4 - space before colon MUST be rejected with 400
        HttpRequest request = new HttpRequest();
        InvocationTargetException ex = assertThrows(InvocationTargetException.class, () ->
                parseHeadersMethod.invoke(httpParser, generateReader(
                        "Host : localhost:8080\r\n" +
                                "\r\n"
                ), request)
        );
        assertEquals(HttpParsingException.class, ex.getCause().getClass());
    }

    @Test
    void testInvalidEmptyFieldName() {
        HttpRequest request = new HttpRequest();
        InvocationTargetException ex = assertThrows(InvocationTargetException.class, () ->
                parseHeadersMethod.invoke(httpParser, generateReader(
                        ": localhost:8080\r\n" +
                                "\r\n"
                ), request)
        );
        assertEquals(HttpParsingException.class, ex.getCause().getClass());
    }

    @Test
    void testInvalidDelimiterInFieldName() {
        // delimiters like '(' are not valid tchar per RFC 7230 3.2.6
        HttpRequest request = new HttpRequest();
        InvocationTargetException ex = assertThrows(InvocationTargetException.class, () ->
                parseHeadersMethod.invoke(httpParser, generateReader(
                        "Hos(t: localhost:8080\r\n" +
                                "\r\n"
                ), request)
        );
        assertEquals(HttpParsingException.class, ex.getCause().getClass());
    }

    // -- helpers --

    private InputStreamReader generateReader(String rawData) {
        return new InputStreamReader(
                new ByteArrayInputStream(rawData.getBytes(StandardCharsets.US_ASCII)),
                StandardCharsets.US_ASCII
        );
    }
}