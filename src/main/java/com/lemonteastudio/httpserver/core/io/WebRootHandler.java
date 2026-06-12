package com.lemonteastudio.httpserver.core.io;

import com.lemonteastudio.httpserver.exception.ReadFileException;
import com.lemonteastudio.httpserver.exception.WebRootHandlerException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URLConnection;

public class WebRootHandler {

    private File webRoot;

    public WebRootHandler(String webRootPath) throws WebRootHandlerException {
        webRoot = new File(webRootPath);
        if (!webRoot.exists() || !webRoot.isDirectory()) {
            throw new WebRootHandlerException("WebRoot does not exist or is not a valid directory");
        }
    }

    private boolean checkIfEndsWithSlash(String webRootPath) {
        return webRootPath.endsWith("/");
    }

    private boolean checkIfProvidedRelativePathExists(String relativePath) {
        return new File(webRoot, relativePath).exists();
    }

    public String getFileMimeType(String relativePath) throws FileNotFoundException, WebRootHandlerException {
        if (checkIfEndsWithSlash(relativePath)) {
            relativePath += "index.html"; // by default serve index.html if it exists
        }

        if (!isPathUnderWebRoot(relativePath)) {
            throw new WebRootHandlerException("Requested path is outside of web root");
        }

        if (!checkIfProvidedRelativePathExists(relativePath)) {
            throw new FileNotFoundException("File not found: " + relativePath);
        }

        File file = new File(webRoot, relativePath);

        String mimeType = URLConnection.getFileNameMap().getContentTypeFor(file.getName());

        return mimeType != null ? mimeType : "application/octet-stream";
    }

    public boolean isPathUnderWebRoot(String requestedPath) throws WebRootHandlerException {
        try {
            File requestedFile = new File(webRoot, requestedPath);
            String canonicalWebRoot = webRoot.getCanonicalPath();
            String canonicalRequested = requestedFile.getCanonicalPath();
            return canonicalRequested.startsWith(canonicalWebRoot);
        } catch (IOException e) {
            throw new WebRootHandlerException("Could not resolve canonical path: " + e.getMessage());
        }
    }

    public byte[] getFileByteArrayData(String relativePath) throws FileNotFoundException, WebRootHandlerException, ReadFileException {
        if (checkIfEndsWithSlash(relativePath)) {
            relativePath += "index.html"; // by default serve index.html if it exists
        }

        if (!isPathUnderWebRoot(relativePath)) {
            throw new WebRootHandlerException("Requested path is outside of web root");
        }

        if (!checkIfProvidedRelativePathExists(relativePath)) {
            throw new FileNotFoundException("File not found: " + relativePath);
        }

        File file = new File(webRoot, relativePath);
        FileInputStream fileInputStream = new FileInputStream(file);
        byte[] fileBytes = new byte[(int) file.length()];
        try {
            fileInputStream.read(fileBytes);
            fileInputStream.close();
        } catch (IOException e) {
            throw new ReadFileException(e);
        }

        return fileBytes;
    }
}
