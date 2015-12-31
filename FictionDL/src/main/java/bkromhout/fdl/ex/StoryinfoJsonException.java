package bkromhout.fdl.ex;

import bkromhout.fdl.util.C;

import java.nio.file.Path;

/**
 * Thrown by {@link bkromhout.fdl.storys.LocalStory LocalStory} if there is some issue with the JSON parsed from a
 * storyinfo.json file.
 */
public class StoryinfoJsonException extends InitStoryException {

    /**
     * Create a new {@link StoryinfoJsonException}, and include a previous throwable as the {@code cause}.
     * @param message Message.
     * @param cause   Cause.
     */
    public StoryinfoJsonException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Create a new {@link StoryinfoJsonException}.
     * <p>
     * Substitute {@code storyDir.toString()} and {@code elementName} into {@link C#JSON_NO_ELEM} and use it as the
     * message.
     * @param storyDir    Directory of associated local story.
     * @param elementName Name of JSON element that is missing/malformed.
     */
    public StoryinfoJsonException(Path storyDir, String elementName) {
        super(String.format(C.JSON_NO_ELEM, storyDir.toString(), elementName));
    }

    /**
     * Create a new {@link StoryinfoJsonException}.
     * <p>
     * Substitute {@code storyDir.toString()} and {@code elementName} into {@link C#JSON_NO_ELEM_TITLE} and use it as
     * the message.
     * @param storyTitle  Title of local story.
     * @param elementName Name of JSON element that is missing/malformed.
     */
    public StoryinfoJsonException(String storyTitle, String elementName) {
        super(String.format(C.JSON_NO_ELEM_TITLE, storyTitle, elementName));
    }
}
