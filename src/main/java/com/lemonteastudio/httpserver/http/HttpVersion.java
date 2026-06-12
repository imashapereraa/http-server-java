package com.lemonteastudio.httpserver.http;
import com.lemonteastudio.httpserver.exception.BadHttpVersionException;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public enum HttpVersion {
    HTTP_1_1("HTTP/1.1", 1, 1);

    public final String LITERAL;
    public final int MAJOR;
    public final int MINOR;

    HttpVersion(String literal, int major, int minor) {
        this.LITERAL = literal;
        this.MAJOR = major;
        this.MINOR = minor;
    }

    private static final Pattern HTTP_VERSION_REGEX = Pattern.compile("^HTTP/(?<major>\\d+)\\.(?<minor>\\d+)$");

    public static HttpVersion getBestCompatibleVersion(String literal) throws BadHttpVersionException {
        Matcher matcher = HTTP_VERSION_REGEX.matcher(literal);

        if (!matcher.matches()) {
            throw new BadHttpVersionException("Version Error");
        }
        int major = Integer.parseInt(matcher.group("major"));
        int minor = Integer.parseInt(matcher.group("minor"));
        HttpVersion bestCompatible = null;
        for (HttpVersion version : HttpVersion.values()) {
            if (version.MAJOR == major) {
                if (bestCompatible == null) {
                    bestCompatible = version;
                } else if ( version.MINOR <= minor && version.MINOR > bestCompatible.MINOR) {
                    // Find the highest minor version we support that doesn't exceed the request's minor version
                    // e.g. client sends HTTP/1.9, we support HTTP/1.1 → return HTTP/1.1
                    bestCompatible = version;
                }
            }
        }
        return bestCompatible;
    }
}
