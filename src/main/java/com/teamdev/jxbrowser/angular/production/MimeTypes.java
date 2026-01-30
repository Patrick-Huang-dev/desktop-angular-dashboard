/*
 *  Copyright 2025, TeamDev. All rights reserved.
 *
 *  Redistribution and use in source and/or binary forms, with or without
 *  modification, must retain the above copyright notice and the following
 *  disclaimer.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.teamdev.jxbrowser.angular.production;

import com.teamdev.jxbrowser.net.MimeType;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Utility class for mapping file extensions to MIME types.
 * 
 * <p>MIME types are loaded from the {@code mime-types.properties} resource file.
 */
public final class MimeTypes {

    private static final MimeType DEFAULT_MIME_TYPE = MimeType.of("application/octet-stream");
    private static final Map<String, MimeType> MIME_TYPE_MAP = loadMimeTypes();

    private MimeTypes() {
        // Prevent instantiation
    }

    /**
     * Returns the MIME type for the given file path based on its extension.
     *
     * @param filePath the file path or name
     * @return the corresponding MIME type, or a default type if unknown
     */
    public static MimeType mimeType(String filePath) {
        int lastDot = filePath.lastIndexOf('.');
        if (lastDot == -1) {
            return DEFAULT_MIME_TYPE;
        }

        String extension = filePath.substring(lastDot + 1).toLowerCase();
        return MIME_TYPE_MAP.getOrDefault(extension, DEFAULT_MIME_TYPE);
    }

    /**
     * Loads MIME types from the properties file.
     */
    private static Map<String, MimeType> loadMimeTypes() {
        Map<String, MimeType> mimeTypes = new HashMap<>();
        URL propsUrl = MimeTypes.class.getClassLoader().getResource("mime-types.properties");
        
        if (propsUrl != null) {
            Properties props = new Properties();
            try (InputStream inputStream = propsUrl.openStream()) {
                props.load(inputStream);
                props.forEach((key, value) -> 
                    mimeTypes.put(key.toString(), MimeType.of(value.toString()))
                );
            } catch (IOException e) {
                System.err.println("Failed to load mime-types.properties: " + e.getMessage());
            }
        } else {
            System.err.println("mime-types.properties not found, using defaults");
            // Fallback to essential MIME types
            mimeTypes.put("html", MimeType.of("text/html"));
            mimeTypes.put("htm", MimeType.of("text/html"));
            mimeTypes.put("css", MimeType.of("text/css"));
            mimeTypes.put("js", MimeType.of("text/javascript"));
            mimeTypes.put("json", MimeType.of("application/json"));
            mimeTypes.put("png", MimeType.of("image/png"));
            mimeTypes.put("jpg", MimeType.of("image/jpeg"));
            mimeTypes.put("jpeg", MimeType.of("image/jpeg"));
            mimeTypes.put("gif", MimeType.of("image/gif"));
            mimeTypes.put("svg", MimeType.of("image/svg+xml"));
            mimeTypes.put("ico", MimeType.of("image/vnd.microsoft.icon"));
            mimeTypes.put("woff", MimeType.of("font/woff"));
            mimeTypes.put("woff2", MimeType.of("font/woff2"));
            mimeTypes.put("ttf", MimeType.of("font/ttf"));
        }
        
        return mimeTypes;
    }
}
