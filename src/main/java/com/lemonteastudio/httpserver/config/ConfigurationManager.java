package com.lemonteastudio.httpserver.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.lemonteastudio.httpserver.exception.HttpConfigurationException;
import com.lemonteastudio.httpserver.util.Json;

import java.io.FileNotFoundException;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;             // Import the Scanner class to read text files


public class ConfigurationManager {

    private static  ConfigurationManager myConfigurationManager;
    private static Configuration myCurrentConfiguration;

    private ConfigurationManager() {
    }

    public static ConfigurationManager getInstance()  {
        if (myConfigurationManager == null) {
            myConfigurationManager = new ConfigurationManager();
        }

        return myConfigurationManager;
    }

    public void loadConfigurationFile(String filepath) {
        File file = new File(filepath);
        StringBuilder sb = new StringBuilder();
        try (Scanner myScanner = new Scanner(file)) {
            while (myScanner.hasNextLine()) {
                sb.append(myScanner.nextLine());
            }
        } catch (FileNotFoundException exception) {
            System.out.println("Error file not found");
            exception.printStackTrace();
        }

        try {
            JsonNode conf = Json.parse(sb.toString());
            myCurrentConfiguration = Json.fromJson(conf, Configuration.class);
        } catch (IOException exception) {
            System.out.println("Error IO exception");
            exception.printStackTrace();
        }
    }

    public Configuration getCurrentConfiguration()   {
        if (myCurrentConfiguration == null) {
            throw new HttpConfigurationException("Current Configuration is Null");
        }
        return myCurrentConfiguration;
    }
}
