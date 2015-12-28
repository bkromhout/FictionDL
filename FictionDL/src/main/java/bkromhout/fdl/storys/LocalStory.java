package bkromhout.fdl.storys;

import bkromhout.fdl.ex.InitStoryException;
import com.google.gson.JsonObject;

import java.nio.file.Path;

/**
 * Represents a story which is created using local files.
 */
public class LocalStory extends Story {
    /**
     * Holds the contents of a storyinfo.json file.
     */
    private JsonObject storyInfo;
    /**
     * Directory where the files for this story reside.
     */
    private Path storyDir;

    /**
     * Create a new {@link LocalStory} using the given json object from a storyinfo.json file and the given directory
     * path.
     * <p>
     * Due to the way that local stories are processed, it is assumed that none of the parameters are null.
     * @param storyInfo Json object from a storyinfo.json file.
     * @param storyDir  Directory where the files for this story reside.
     * @throws InitStoryException
     */
    public LocalStory(JsonObject storyInfo, Path storyDir) throws InitStoryException {
        super(null, null);

        this.storyInfo = storyInfo;
        this.storyDir = storyDir;
        // Call populateInfo() again now that we've set our local members.
        populateInfo();
    }

    @Override
    protected void populateInfo() throws InitStoryException {
        // When the super constructor first calls this, we haven't set the variables we need yet, so just return.
        if (storyInfo == null || storyDir == null) return;

        // TODO
    }
}
