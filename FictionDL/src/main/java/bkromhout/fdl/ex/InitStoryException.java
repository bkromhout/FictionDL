package bkromhout.fdl.ex;

/**
 * Exception thrown when a {@link bkromhout.fdl.storys.Story Story} cannot be created for some reason.
 */
public class InitStoryException extends Exception {

    /**
     * Create a new {@link InitStoryException} with some {@code message}.
     * @param message Message.
     */
    public InitStoryException(String message) {
        this(message, null);
    }

    /**
     * Create a new {@link InitStoryException} with some {@code message} and {@code cause}.
     * @param message Message.
     * @param cause   Cause.
     */
    public InitStoryException(String message, Throwable cause) {
        super(message, cause);
    }
}
