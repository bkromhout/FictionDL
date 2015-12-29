package bkromhout.fdl.ex;

/**
 * Exception that can be thrown by {@link bkromhout.fdl.storys.LocalStory LocalStory} during the execution of any of its
 * methods in order to indicate that it cannot continue, and that we should abandon any attempts to continue processing
 * it.
 * <p>
 * Note: Since {@link bkromhout.fdl.storys.LocalStory LocalStory} is a subclass of {@link bkromhout.fdl.storys.Story
 * Story}, it will throw {@link InitStoryException} if issues occur during its creation, rather than this.
 */
public class LocalStoryException extends Exception {

    /**
     * Create a new {@link LocalStoryException} with some message.
     * @param message Message.
     */
    public LocalStoryException(String message) {
        super(message);
    }
}
