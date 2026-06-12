package com.lemonteastudio.httpserver.http;

import com.lemonteastudio.httpserver.exception.BadHttpVersionException;
import com.lemonteastudio.httpserver.exception.BadHttpVersionException;
import com.lemonteastudio.httpserver.exception.HttpParsingException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HttpVersionTest {

    @Test
    void getBestCompatibleVersionExactMatch() throws BadHttpVersionException {
        HttpVersion version = HttpVersion.getBestCompatibleVersion("HTTP/1.1");

        assertNotNull(version);

        assertEquals(HttpVersion.HTTP_1_1, version);
    }

    @Test
    void getBestCompatibleVersionBestMinorMatch() throws BadHttpVersionException {
        HttpVersion version = HttpVersion.getBestCompatibleVersion("HTTP/1.9");

        assertNotNull(version);

        assertEquals(HttpVersion.HTTP_1_1, version);
    }


    @Test
    void getBestCompatibleVersionFailFindingMatch() {
        assertThrows(BadHttpVersionException.class, () -> {
            HttpVersion.getBestCompatibleVersion("HTTP/v.9");
        });
    }
}