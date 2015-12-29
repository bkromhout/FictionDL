package bkromhout.fdl.ex;

/**
 * Exception thrown when a {@link bkromhout.fdl.storys.Story} cannot be created for some reason.
 */
public class InitStoryException extends Exception {

    /**
     * Create a new {@link InitStoryException} with some message.
     * @param message Message.
     */
    public InitStoryException(String message) {
        super(message);
    }
}
