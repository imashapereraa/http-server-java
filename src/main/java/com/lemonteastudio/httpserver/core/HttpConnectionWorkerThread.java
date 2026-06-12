package com.lemonteastudio.httpserver.core;

import com.lemonteastudio.httpserver.core.io.WebRootHandler;
import com.lemonteastudio.httpserver.exception.HttpParsingException;
import com.lemonteastudio.httpserver.exception.ReadFileException;
import com.lemonteastudio.httpserver.exception.WebRootHandlerException;
import com.lemonteastudio.httpserver.http.HttpParser;
import com.lemonteastudio.httpserver.http.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class HttpConnectionWorkerThread extends Thread {
    private final static Logger LOGGER = LoggerFactory.getLogger(HttpConnectionWorkerThread.class);

    private final Socket socket;
    private final WebRootHandler webRootHandler;
    private final HttpParser httpParser = new HttpParser();

    public HttpConnectionWorkerThread(Socket socket, WebRootHandler webRootHandler) {
        this.socket = socket;
        this.webRootHandler = webRootHandler;
    }

    @Override
    public void run() {
        InputStream inputStream = null;
        OutputStream outputStream = null;

        try {
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();

            HttpRequest request = httpParser.parseHttpRequest(inputStream);
            String relativePath = request.getRequestTarget();

            try {
                byte[] fileBytes = webRootHandler.getFileByteArrayData(relativePath);
                String mimeType  = webRootHandler.getFileMimeType(relativePath);
                sendResponse(outputStream, "200 OK", mimeType, fileBytes);

            } catch (FileNotFoundException e) {
                LOGGER.warn("File not found: {}", relativePath);
                sendResponse(outputStream, "404 Not Found", "text/html",
                        "<h1>404 Not Found</h1>".getBytes());

            } catch (WebRootHandlerException e) {
                LOGGER.warn("Bad request - path outside webroot: {}", relativePath);
                sendResponse(outputStream, "400 Bad Request", "text/html",
                        "<h1>400 Bad Request</h1>".getBytes());

            } catch (ReadFileException e) {
                LOGGER.error("Failed to read file: {}", relativePath, e);
                sendResponse(outputStream, "500 Internal Server Error", "text/html",
                        "<h1>500 Internal Server Error</h1>".getBytes());
            }

            LOGGER.info("Processing finished");

        } catch (HttpParsingException e) {
            LOGGER.error("HTTP parsing error", e);
        } catch (IOException e) {
            LOGGER.error("Communication error", e);
        } finally {
            try { if (inputStream  != null) inputStream.close();  } catch (IOException e) { /* pass */ }
            try { if (outputStream != null) outputStream.close(); } catch (IOException e) { /* pass */ }
            try { if (socket      != null) socket.close();        } catch (IOException e) { /* pass */ }
        }
    }

    private void sendResponse(OutputStream outputStream, String status, String mimeType, byte[] body) throws IOException {
        outputStream.write(("HTTP/1.1 " + status + "\r\n").getBytes());
        outputStream.write(("Content-Type: " + mimeType + "\r\n").getBytes());
        outputStream.write(("Content-Length: " + body.length + "\r\n").getBytes());
        outputStream.write("\r\n".getBytes());
        outputStream.write(body);
        outputStream.flush();
    }
}