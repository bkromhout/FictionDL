package bkromhout.fdl.ex;

import bkromhout.fdl.util.C;

import java.nio.file.Path;

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

    /**
     * Create a new {@link StoryinfoJsonException} using the {@link C#JSON_BAD_ELEM} log string.
     * @param storyDir Directory of associated local story.
     * @param elementName Name of JSON element that is missing/malformed.
     */
    public StoryinfoJsonException(Path storyDir, String elementName) {
        this(String.format(C.JSON_BAD_ELEM, storyDir.toString(), elementName));
    }

    /**
     * Create a new {@link StoryinfoJsonException} using the {@link C#JSON_BAD_ELEM_TITLE} log string.
     * @param storyTitle Title of local story.
     * @param elementName Name of JSON element that is missing/malformed.
     */
    public StoryinfoJsonException(String storyTitle, String elementName) {
        this(String.format(C.JSON_BAD_ELEM_TITLE, storyTitle, elementName));
    }
}
