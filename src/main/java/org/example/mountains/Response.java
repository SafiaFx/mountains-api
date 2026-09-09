package org.example.mountains;

import java.net.http.HttpResponse;
import java.util.List;

/**
 * Wrapper expected by the supplied client-side tests.
 *
 * <p>It keeps both the parsed mountain data and the raw {@link HttpResponse} so the caller can
 * inspect HTTP status codes as recommended in Chapter 11.5.</p>
 */
public class Response {
    private final List<Mountain> mountains;
    private final HttpResponse<String> response;

    public Response(final List<Mountain> mountains, final HttpResponse<String> response) {
        this.mountains = mountains;
        this.response = response;
    }

    public List<Mountain> getMountains() {
        return mountains;
    }

    public HttpResponse<String> getResponse() {
        return response;
    }
}
