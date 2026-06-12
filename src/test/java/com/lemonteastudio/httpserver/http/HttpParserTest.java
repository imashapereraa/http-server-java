package com.lemonteastudio.httpserver.http;

import com.lemonteastudio.httpserver.exception.HttpParsingException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class HttpParserTest {

    private HttpParser httpParser;

    @BeforeAll
    public void beforeClass() {
        httpParser = new HttpParser();
    }

    @Test
    void parseHttpRequest() {
        HttpRequest request = null;
        try {
            request = httpParser.parseHttpRequest(generateValidTestCase());
        } catch (HttpParsingException e) {
            fail(e);
        }

        assertEquals(HttpMethod.GET, request.getMethod());
        assertEquals("/", request.getRequestTarget());
        assertEquals("HTTP/1.1", HttpVersion.HTTP_1_1.LITERAL);
    }

    @Test
    void parseHttpRequestBadMethod() {
        HttpRequest request = null;
        try {
            request = httpParser.parseHttpRequest(generateInvalidTestCase());
            fail();
        } catch (HttpParsingException e) {
            assertEquals(HttpStatusCode.CLIENT_ERROR_501_NOT_IMPLEMENTED, e.getErrorCode());
        }
    }

    @Test
    void parseHttpRequestBadRequest() {
        HttpRequest request = null;
        try {
            request = httpParser.parseHttpRequest(generateInvalidRequestTestCase());
            fail();
        } catch (HttpParsingException e) {
            assertEquals(HttpStatusCode.CLIENT_ERROR_505_VERSION_NOT_SUPPORTED, e.getErrorCode());
        }
    }

    @Test
    void parseHttpRequestTooLongMethod() {
        HttpRequest request = null;
        try {
            request = httpParser.parseHttpRequest(generateTooBigMethodTestCase());
            fail();
        } catch (HttpParsingException e) {
            assertEquals(HttpStatusCode.CLIENT_ERROR_501_NOT_IMPLEMENTED, e.getErrorCode());
        }
    }

    @Test
    void parseHttpRequestTooManyMethodItems() {
        HttpRequest request = null;
        try {
            request = httpParser.parseHttpRequest(generateInvalidMethodNumberOfItemsMethodTestCase());
            fail();
        } catch (HttpParsingException e) {
            assertEquals(HttpStatusCode.CLIENT_ERROR_400_BAD_REQUEST, e.getErrorCode());
        }
    }

    @Test
    void parseHttpRequestEmptyMethodItems() {
        HttpRequest request = null;
        try {
            request = httpParser.parseHttpRequest(generateEmptyMethodTestCase());
            fail();
        } catch (HttpParsingException e) {
            assertEquals(HttpStatusCode.CLIENT_ERROR_400_BAD_REQUEST, e.getErrorCode());
        }
    }

    @Test
    void parseHttpRequestTrailingNMethodItems() {
        HttpRequest request = null;
        try {
            request = httpParser.parseHttpRequest(generateNotATrailingNMethodTestCase());
            fail();
        } catch (HttpParsingException e) {
            assertEquals(HttpStatusCode.CLIENT_ERROR_400_BAD_REQUEST, e.getErrorCode());
        }
    }

    private InputStream generateValidTestCase() {
        String rawData = "GET / HTTP/1.1\r\n" +
                "Host: localhost:8080\r\n" +
                "User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:151.0) Gecko/20100101 Firefox/151.0\r\n" +
                "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\r\n" +
                "Accept-Language: en-US,en;q=0.9\r\n" +
                "Accept-Encoding: gzip, deflate, br, zstd\r\n" +
                "Connection: keep-alive\r\n" +
                "Upgrade-Insecure-Requests: 1\r\n" +
                "Sec-Fetch-Dest: document\r\n" +
                "Sec-Fetch-Mode: navigate\r\n" +
                "Sec-Fetch-Site: none\r\n" +
                "Sec-Fetch-User: ?1\r\n" +
                "DNT: 1\r\n" +
                "Sec-GPC: 1\r\n" +
                "Priority: u=0, i" +
                "\r\n";

        return new ByteArrayInputStream(
                rawData.getBytes(StandardCharsets.US_ASCII)
        );
    }

    private InputStream generateInvalidTestCase() {
        String rawData = "BAD / HTTP/1.1\r\n" +
                "Host: localhost:8080\r\n" +
                "User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:151.0) Gecko/20100101 Firefox/151.0\r\n" +
                "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\r\n" +
                "\r\n";

        return new ByteArrayInputStream(
                rawData.getBytes(StandardCharsets.US_ASCII)
        );
    }

    private InputStream generateInvalidRequestTestCase() {
        String rawData = "GET / HT/1.1\r\n" +
                "Host: localhost:8080\r\n" +
                "User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:151.0) Gecko/20100101 Firefox/151.0\r\n" +
                "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\r\n" +
                "\r\n";

        return new ByteArrayInputStream(
                rawData.getBytes(StandardCharsets.US_ASCII)
        );
    }

    private InputStream generateTooBigMethodTestCase() {
        String rawData = "BADDDDDD / HTTP/1.1\r\n" +
                "Host: localhost:8080\r\n" +
                "User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:151.0) Gecko/20100101 Firefox/151.0\r\n" +
                "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\r\n" +
                "\r\n";

        return new ByteArrayInputStream(
                rawData.getBytes(StandardCharsets.US_ASCII)
        );
    }

    private InputStream generateInvalidMethodNumberOfItemsMethodTestCase() {
        String rawData = "GET / fefefaef / HTTP/1.1\r\n" +
                "Host: localhost:8080\r\n" +
                "User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:151.0) Gecko/20100101 Firefox/151.0\r\n" +
                "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\r\n" +
                "\r\n";

        return new ByteArrayInputStream(
                rawData.getBytes(StandardCharsets.US_ASCII)
        );
    }

    private InputStream generateNotATrailingNMethodTestCase() {
        String rawData = "GET / HTTP/1.1\n" +
                "Host: localhost:8080\r\n" +
                "User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:151.0) Gecko/20100101 Firefox/151.0\r\n" +
                "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\r\n" +
                "\r\n";

        return new ByteArrayInputStream(
                rawData.getBytes(StandardCharsets.US_ASCII)
        );
    }

    private InputStream generateEmptyMethodTestCase() {
        String rawData = "\r\n" +
                "Host: localhost:8080\r\n" +
                "User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:151.0) Gecko/20100101 Firefox/151.0\r\n" +
                "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\r\n" +
                "\r\n";

        return new ByteArrayInputStream(
                rawData.getBytes(StandardCharsets.US_ASCII)
        );
    }
}