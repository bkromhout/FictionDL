package bkromhout.fdl.ex;

/**
 * Exception thrown when a Story object cannot be created for some reason.
 */
public class InitStoryException extends Exception {
    /**
     * Create a new InitStoryException with some message text.
     * @param message Message.
     */
    public InitStoryException(String message) {
        super(message);
    }
}
