package com.lemonteastudio.httpserver;


import com.lemonteastudio.httpserver.config.Configuration;
import com.lemonteastudio.httpserver.config.ConfigurationManager;
import com.lemonteastudio.httpserver.core.ServerListenerThread;
import com.lemonteastudio.httpserver.exception.WebRootHandlerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class HttpServer {

    private final static Logger LOGGER = LoggerFactory.getLogger(HttpServer.class);

    public static void main(String[] args) {
        LOGGER.info("Server, starting...");
        ConfigurationManager.getInstance().loadConfigurationFile("src/main/resources/http.json");
        Configuration configuration = ConfigurationManager.getInstance().getCurrentConfiguration();

        LOGGER.info("Configuration loaded, port: {}", configuration.getPort());
        LOGGER.info("Configuration loaded, webroot: {}", configuration.getWebroot());

        try {
            ServerListenerThread serverListenerThread = new ServerListenerThread(configuration.getPort(), configuration.getWebroot());
            serverListenerThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (WebRootHandlerException e) {
            throw new RuntimeException(e);
        }
    }
}
