package bkromhout.fdl.ex;

/**
 * Can be thrown by {@link bkromhout.fdl.storys.LocalStory LocalStory} during the execution of any of its methods in
 * order to indicate that it cannot continue, and that we should abandon any attempts to continue processing it.
 * <p>
 * Note: {@link bkromhout.fdl.storys.LocalStory LocalStory} will throw {@link InitStoryException} instead of this
 * exception if issues occur during its creation, since it is a subclass of {@link bkromhout.fdl.storys.Story Story}
 */
public class LocalStoryException extends Exception {

    /**
     * Create a new {@link LocalStoryException} with some {@code message}.
     * @param message Message.
     */
    public LocalStoryException(String message) {
        super(message);
    }
}
