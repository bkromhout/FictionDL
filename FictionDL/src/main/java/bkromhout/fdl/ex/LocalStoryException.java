package bkromhout.fdl.ex;

/**
 * Thrown by {@link bkromhout.fdl.storys.LocalStory LocalStory} if there is an issue while processing it.
 */
public class LocalStoryException extends Exception {

    /**
     * Create a new {@link LocalStoryException} with some {@code message} and previous throwable as the {@code cause}.
     * @param message Message.
     * @param cause   Cause.
     */
    public LocalStoryException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Create a new {@link LocalStoryException} with some {@code message}.
     * @param message Message.
     */
    public LocalStoryException(String message) {
        this(message, null);
    }
}
