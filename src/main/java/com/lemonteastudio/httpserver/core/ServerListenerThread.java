package com.lemonteastudio.httpserver.core;

import com.lemonteastudio.httpserver.core.io.WebRootHandler;
import com.lemonteastudio.httpserver.exception.WebRootHandlerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerListenerThread extends Thread {

    private final static Logger LOGGER = LoggerFactory.getLogger(ServerListenerThread.class);

    private int port;
    private String webroot;
    private ServerSocket serverSocket;
    private WebRootHandler webRootHandler;

    public ServerListenerThread(int port, String webroot) throws IOException, WebRootHandlerException {
        this.webroot = webroot;
        this.port = port;
        this.serverSocket = new ServerSocket(this.port);
        this.webRootHandler = new WebRootHandler(webroot);
    }

    @Override
    public void run() {
        try {
            while (serverSocket.isBound() && !serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();

                LOGGER.info("Connection accepted: {}", socket.getInetAddress());

                HttpConnectionWorkerThread workerThread = new HttpConnectionWorkerThread(socket, webRootHandler);
                workerThread.start();
            }
        } catch (IOException e) {
            LOGGER.error("Socket error", e);
        } finally {
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    // pass
                }
            }
        }
    }
}