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

import com.teamdev.jxbrowser.angular.AppDetails;
import com.teamdev.jxbrowser.net.HttpHeader;
import com.teamdev.jxbrowser.net.HttpStatus;
import com.teamdev.jxbrowser.net.UrlRequestJob;
import com.teamdev.jxbrowser.net.callback.InterceptUrlRequestCallback;

import java.io.IOException;
import java.net.URI;

/**
 * Intercepts URL requests with the custom scheme and loads web resources
 * from the JAR file in production mode.
 *
 * <p>This allows the Angular application to be bundled inside the JAR
 * and loaded without requiring an external web server.
 */
public final class UrlRequestInterceptor implements InterceptUrlRequestCallback {

    private static final String CONTENT_TYPE = "Content-Type";
    private static final String INDEX_HTML = "/index.html";

    /**
     * The root path inside the JAR where web resources are located.
     * Angular build output is placed in this directory.
     */
    private static final String CONTENT_ROOT = "/web";

    @Override
    public Response on(Params params) {
        var url = params.urlRequest().url();
        var expectedUrlPrefix = AppDetails.appScheme() + "://" + AppDetails.appHost();

        if (url.startsWith(expectedUrlPrefix)) {
            var uri = URI.create(url);
            var path = uri.getPath();

            String fileName;
            if (path.equals("/")) {
                fileName = INDEX_HTML;
            } else {
                fileName = path;
            }

            return loadResource(params, fileName);
        }

        return Response.proceed();
    }

    /**
     * Loads a resource from the JAR and returns the appropriate response.
     *
     * @param params   the request parameters
     * @param fileName the file name to load
     * @return the response with the resource content or an error status
     */
    private Response loadResource(Params params, String fileName) {
        var resourcePath = CONTENT_ROOT + fileName;

        try (var stream = getClass().getResourceAsStream(resourcePath)) {
            if (stream == null) {
                // Resource not found, return 404.
                var job = createUrlRequestJob(params, HttpStatus.NOT_FOUND, fileName);
                job.complete();
                return Response.intercept(job);
            }

            // Resource found, return 200 with content.
            var job = createUrlRequestJob(params, HttpStatus.OK, fileName);
            job.write(stream.readAllBytes());
            job.complete();
            return Response.intercept(job);
        } catch (IOException e) {
            // Read failed, return 500.
            var job = createUrlRequestJob(params, HttpStatus.INTERNAL_SERVER_ERROR, fileName);
            job.complete();
            return Response.intercept(job);
        }
    }

    /**
     * Creates a URL request job with the specified status and content type.
     */
    private UrlRequestJob createUrlRequestJob(Params params, HttpStatus status, String fileName) {
        var builder = UrlRequestJob.Options.newBuilder(status);
        builder.addHttpHeader(HttpHeader.of(CONTENT_TYPE, MimeTypes.mimeType(fileName).value()));
        return params.newUrlRequestJob(builder.build());
    }
}
