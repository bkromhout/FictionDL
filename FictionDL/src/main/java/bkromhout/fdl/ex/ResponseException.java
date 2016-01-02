package bkromhout.fdl.ex;

import com.squareup.okhttp.Response;

/**
 * Exception to throw when an OkHttp Response doesn't have what we expect it to.
 */
public final class ResponseException extends RuntimeException {
    private final Response response;

    /**
     * Create a new {@link ResponseException}.
     * <p>
     * It is assumed that the given Response's ResponseBody is closed prior to it being passed into this constructor.
     * @param message  Exception message.
     * @param response Response associated with this exception.
     */
    public ResponseException(String message, Response response) {
        super(message);
        this.response = response;
    }

}
