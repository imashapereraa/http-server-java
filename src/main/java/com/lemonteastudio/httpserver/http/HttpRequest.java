package com.lemonteastudio.httpserver.http;

import com.lemonteastudio.httpserver.exception.BadHttpVersionException;
import com.lemonteastudio.httpserver.exception.HttpParsingException;

import java.util.HashMap;

public class HttpRequest extends HttpMessage {

    private HttpMethod method;
    private String requestTarget;
    private String originalHttpVersion;
    private HttpVersion bestCompatibleVersion;
    private HashMap<String, String> headers = new HashMap<>();

    public String getHeader(String fieldName) {
        return this.headers.get(fieldName);
    }

    public HashMap<String, String> getHeaders() {
        return this.headers;
    }

    public void setHeaders(String fieldName, String fieldValue) {
        this.headers.put(fieldName, fieldValue);
    }

    HttpRequest() {
    }

    public HttpMethod getMethod() {
        return this.method;
    }

    void setMethod(String methodName) throws HttpParsingException {
        for (HttpMethod method : HttpMethod.values()) {
            if (methodName.equals(method.name())) {
                this.method = method;
                return;
            }
        }
        throw new HttpParsingException(HttpStatusCode.CLIENT_ERROR_501_NOT_IMPLEMENTED);
    }

    public void setRequestTarget(String requestTarget) throws HttpParsingException {
        if (requestTarget == null || requestTarget.isEmpty()) {
            throw new HttpParsingException(HttpStatusCode.CLIENT_ERROR_500_INTERNAL_SERVER_ERROR);
        }
        this.requestTarget = requestTarget;
    }

    public String getRequestTarget() {
        return this.requestTarget;
    }

    public void setHttpVersion(String originalHttpVersion) throws HttpParsingException, BadHttpVersionException {
        this.originalHttpVersion = originalHttpVersion;
        this.bestCompatibleVersion = HttpVersion.getBestCompatibleVersion(originalHttpVersion);

        if (this.bestCompatibleVersion == null) {
            throw new HttpParsingException(HttpStatusCode.CLIENT_ERROR_505_VERSION_NOT_SUPPORTED);
        }
    }

    public HttpVersion getHttpVersion() {
        return this.bestCompatibleVersion;
    }
}