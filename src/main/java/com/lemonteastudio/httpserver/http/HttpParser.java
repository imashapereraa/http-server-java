package com.lemonteastudio.httpserver.http;

import com.lemonteastudio.httpserver.exception.BadHttpVersionException;
import com.lemonteastudio.httpserver.exception.HttpParsingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpParser {

    private final static Logger LOGGER = LoggerFactory.getLogger(HttpParser.class);

    // ASCII values for special characters used in HTTP
    // SP = Space, CR = Carriage Return (\r), LF = Line Feed (\n)
    // HTTP spec requires lines to end with CR+LF (\r\n)
    private static final int SP = 0x20; //32
    private static final int CR = 0x0D; //13
    private static final int LF = 0x0A; //10

    public HttpRequest parseHttpRequest(InputStream inputStream) throws HttpParsingException {
        // US_ASCII because HTTP/1.1 headers must be ASCII encoded
        InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.US_ASCII);

        HttpRequest request = new HttpRequest();

        try {
            // HTTP request has 3 parts: Request Line, Headers, Body
            // e.g. "GET / HTTP/1.1\r\n"
            parseRequestLine(reader, request);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            parseHeaders(reader, request);
        } catch (IOException e) {
            e.printStackTrace();
        }
        parseBody(reader, request);

        return request;
    }

    private void parseRequestLine(InputStreamReader reader, HttpRequest request) throws IOException, HttpParsingException {
        StringBuilder processingDataBuffer = new StringBuilder();

        // Track which parts of the request line we've parsed
        // Request line format: METHOD SP REQUEST-TARGET SP HTTP-VERSION CR LF
        // e.g:                 GET    /  HTTP/1.1             \r\n
        boolean methodParsed = false;
        boolean requestTargetParsed = false;

        int _byte;
        // Read one byte at a time until end of stream (-1)
        while ((_byte = reader.read()) >= 0) {

            // CR+LF signals end of the request line
            // We expect CR to always be followed by LF per HTTP spec
            if (_byte == CR) {
                _byte = reader.read();
                if (_byte == LF) {
                    if (methodParsed && requestTargetParsed) {
                        // Valid end of request line — all tokens parsed successfully
                        LOGGER.debug("Request VERSION Line to Process: {}", processingDataBuffer);

                        try {
                            request.setHttpVersion(processingDataBuffer.toString());
                        } catch (BadHttpVersionException e) {
                            throw new HttpParsingException(HttpStatusCode.CLIENT_ERROR_505_VERSION_NOT_SUPPORTED);
                        }

                        return;
                    } else {
                        // CR+LF found, but we haven't parsed both method and request target yet
                        // e.g. "GET\r\n" is missing the request target and version
                        throw new HttpParsingException(HttpStatusCode.CLIENT_ERROR_400_BAD_REQUEST);
                    }
                } else {
                    // CR must always be followed by LF per HTTP spec
                    // Anything else is a malformed request
                    throw new HttpParsingException(HttpStatusCode.CLIENT_ERROR_400_BAD_REQUEST);
                }
            }

            if (_byte == SP) {
                // First space separates METHOD from REQUEST-TARGET
                // e.g. "GET " → method is "GET"
                if (!methodParsed) {
                    LOGGER.debug("Request Line METHOD TARGET to Process: {}", processingDataBuffer);
                    request.setMethod(processingDataBuffer.toString());
                    methodParsed = true;

                    // Second space separates REQUEST-TARGET from HTTP-VERSION
                    // e.g. "/ " → request target is "/"
                } else if (!requestTargetParsed) {
                    LOGGER.debug("Request Line REQ TARGET to Process: {}", processingDataBuffer);
                    request.setRequestTarget(processingDataBuffer.toString());
                    requestTargetParsed = true;

                    // A third space means malformed request line — reject it
                } else {
                    throw new HttpParsingException(HttpStatusCode.CLIENT_ERROR_400_BAD_REQUEST);
                }

                // Clear the buffer after each token so we start fresh for the next one
                processingDataBuffer.delete(0, processingDataBuffer.length());

            } else {
                processingDataBuffer.append((char) _byte);

                // Validate method length while we're still parsing it
                // HTTP methods have a known max length — anything longer is not implemented
                if (!methodParsed) {
                    if (processingDataBuffer.length() > HttpMethod.MAX_LENGTH) {
                        throw new HttpParsingException(HttpStatusCode.CLIENT_ERROR_501_NOT_IMPLEMENTED);
                    }
                }
            }
        }
    }

    private void parseHeaders(InputStreamReader reader, HttpRequest request) throws IOException, HttpParsingException {
        StringBuilder processingDataBuffer = new StringBuilder();
        int seenCount = 0;
        int[] terminatorSequence = {CR, LF};
        boolean lastWasCRLF = false;

        int _byte;
        while ((_byte = reader.read()) >= 0) {

            if (_byte == terminatorSequence[seenCount]) {
                seenCount++;

                if (seenCount == 2) {
                    // completed a \r\n — flush the current line
                    String line = processingDataBuffer.toString();
                    processingDataBuffer.setLength(0);
                    seenCount = 0;

                    if (line.isEmpty() && lastWasCRLF) {
                        // two consecutive \r\n = \r\n\r\n — end of headers
                        LOGGER.debug("Headers parsed successfully");
                        break;
                    }

                    lastWasCRLF = true;

                    if (!line.isEmpty()) {
                        Matcher matcher = HTTP_HEADER_PATTERN.matcher(line);
                        if (!matcher.matches()) {
                            throw new HttpParsingException(HttpStatusCode.CLIENT_ERROR_400_BAD_REQUEST);
                        }
                        // group(1) = field-name, group(3) = field-value
                        request.setHeaders(matcher.group(1), matcher.group(3).stripTrailing());
                    }
                }

            } else {
                // partial \r\n sequence broken — push back into buffer
                for (int i = 0; i < seenCount; i++) {
                    processingDataBuffer.append((char) terminatorSequence[i]);
                }
                seenCount = 0;
                lastWasCRLF = false;
                processingDataBuffer.append((char) _byte);
            }
        }
    }

    // RFC 7230 3.2.6 — field-name = token, tchar = VCHAR except delimiters
    // group(1) = field-name, group(2) = OWS, group(3) = field-value
    private static final Pattern HTTP_HEADER_PATTERN = Pattern.compile(
            "^([!#$%&'*+\\-.^_`|~0-9a-zA-Z]+):([ \\t]*)(.*)$"
    );



    private void parseBody(InputStreamReader reader, HttpRequest request) {

    }
}