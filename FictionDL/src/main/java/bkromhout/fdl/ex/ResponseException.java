package bkromhout.fdl.ex;

import com.squareup.okhttp.Response;

/**
 * Exception to throw when an OkHttp Response doesn't have what we expect it to.
 */
public final class ResponseException extends RuntimeException {
    private final Response response;

    public ResponseException(String message, Response response) {
        super(message);
        this.response = response;
    }

    public Response getResponse() {
        return response;
    }
}
