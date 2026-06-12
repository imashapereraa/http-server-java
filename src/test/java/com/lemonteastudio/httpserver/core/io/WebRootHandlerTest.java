package com.lemonteastudio.httpserver.core.io;

import com.lemonteastudio.httpserver.exception.ReadFileException;
import com.lemonteastudio.httpserver.exception.WebRootHandlerException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
class WebRootHandlerTest {

    @TempDir
    Path tempDir;

    @Test
    void validDirectoryDoesNotThrow() {
        assertDoesNotThrow(() -> new WebRootHandler(tempDir.toString()));
    }

    @Test
    void nonExistentPathThrows() {
        assertThrows(WebRootHandlerException.class, () ->
                new WebRootHandler("/this/path/does/not/exist")
        );
    }

    @Test
    void fileInsteadOfDirectoryThrows() throws Exception {
        // create a file where a directory is expected
        File file = tempDir.resolve("notadirectory.txt").toFile();
        file.createNewFile();

        assertThrows(WebRootHandlerException.class, () ->
                new WebRootHandler(file.getAbsolutePath())
        );
    }

    @Test
    void emptyStringPathThrows() {
        assertThrows(WebRootHandlerException.class, () ->
                new WebRootHandler("")
        );
    }

    @Test
    void pathWithTrailingSlashDoesNotThrow() {
        assertDoesNotThrow(() -> new WebRootHandler(tempDir.toString() + "/"));
    }

    @Test
    void pathWithoutTrailingSlashDoesNotThrow() {
        assertDoesNotThrow(() -> new WebRootHandler(tempDir.toString()));
    }

    @Test
    void pathUnderWebRootReturnsTrue() throws Exception, WebRootHandlerException {
        WebRootHandler handler = new WebRootHandler(tempDir.toString());
        assertTrue(handler.isPathUnderWebRoot("index.html"));
    }

    @Test
    void pathTraversalReturnsFalse() throws Exception, WebRootHandlerException {
        WebRootHandler handler = new WebRootHandler(tempDir.toString());
        assertFalse(handler.isPathUnderWebRoot("../../etc/passwd"));
    }

    @Test
    void nestedPathUnderWebRootReturnsTrue() throws Exception, WebRootHandlerException {
        WebRootHandler handler = new WebRootHandler(tempDir.toString());
        assertTrue(handler.isPathUnderWebRoot("img/logo.png"));
    }

    @Test
    void validFileReturnsBytes() throws Exception, WebRootHandlerException, ReadFileException {
        // write known content to a temp file
        File file = tempDir.resolve("index.html").toFile();
        Files.writeString(file.toPath(), "<html></html>");

        WebRootHandler handler = new WebRootHandler(tempDir.toString());
        byte[] bytes = handler.getFileByteArrayData("index.html");

        assertArrayEquals("<html></html>".getBytes(), bytes);
    }

    @Test
    void slashPathServesIndexHtmlBytes() throws Exception, WebRootHandlerException, ReadFileException {
        File file = tempDir.resolve("index.html").toFile();
        Files.writeString(file.toPath(), "<html></html>");

        WebRootHandler handler = new WebRootHandler(tempDir.toString());
        byte[] bytes = handler.getFileByteArrayData("/");

        assertArrayEquals("<html></html>".getBytes(), bytes);
    }

    @Test
    void missingFileThrowsFileNotFoundException() throws Exception, WebRootHandlerException {
        WebRootHandler handler = new WebRootHandler(tempDir.toString());
        assertThrows(FileNotFoundException.class, () ->
                handler.getFileByteArrayData("doesnotexist.html")
        );
    }

    @Test
    void pathTraversalThrowsWebRootHandlerException() throws Exception, WebRootHandlerException {
        WebRootHandler handler = new WebRootHandler(tempDir.toString());
        assertThrows(WebRootHandlerException.class, () ->
                handler.getFileByteArrayData("../../etc/passwd")
        );
    }
}