package bkromhout.fdl.ex;

import com.squareup.okhttp.Request;

import java.io.IOException;

/**
 * Similar to OkHttp's RequestException, but more useful for RxJava purposes, and provides the original OkHttp Request
 * too.
 */
public final class RequestException extends RuntimeException {
    private final Request request;

    /**
     * Create a new {@link RequestException}.
     * @param request OkHttp Request which is involved.
     * @param cause Underlying cause.
     */
    public RequestException(Request request, IOException cause) {
        super(cause);
        this.request = request;
    }

    @Override
    public synchronized IOException getCause() {
        return (IOException) super.getCause();
    }

    public Request getRequest() {
        return request;
    }
}
