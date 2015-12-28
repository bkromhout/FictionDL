package bkromhout.fdl.ex;

/**
 * Thrown by {@link bkromhout.fdl.storys.LocalStory} if there is some issue with the JSON parsed from a storyinfo.json
 * file.
 */
public class StoryinfoJsonException extends InitStoryException {
    /**
     * Create a new {@link StoryinfoJsonException} with some message text.
     * @param message Message.
     */
    public StoryinfoJsonException(String message) {
        super(message);
    }
}
