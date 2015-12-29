package bkromhout.fdl.storys;

import bkromhout.fdl.ex.InitStoryException;
import bkromhout.fdl.ex.LocalStoryException;
import com.google.gson.JsonObject;

import java.nio.file.Path;

/**
 * Represents a story which is created using local files.
 * <p>
 * Unlike most story classes, this one is in charge of its own {@link bkromhout.fdl.Chapter Chapter} creation.
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
     * Number of chapter files.
     */
    private int numChapFiles;

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
        // Call populateInfo() again now.
        populateInfo();
    }

    @Override
    protected void populateInfo() throws InitStoryException {
        // When the super constructor first calls this, we haven't set the variables we need yet, so just return.
        if (storyInfo == null || storyDir == null) return;

        // TODO Populate Story fields using story info JSON.
    }

    /**
     * Attempts to read all chapter HTML files in this local story's directory and make {@link bkromhout.fdl.Chapter
     * Chapter}s from them.
     * @throws LocalStoryException   if there were issues while processing chapter files.
     * @throws IllegalStateException if this is called and {@link #numChapFiles} is < 0.
     */
    public void processChapters() throws LocalStoryException {
        if (this.numChapFiles < 0) throw new IllegalStateException();
        // TODO (note, will need to add another public static Chapter creation method to that class)

        // TODO At some point in here, make sure that we call Util.loudf(C.PARSING_FILE, [chapfilename]);

        // TODO At some point in here, make sure that we call Util.log(C.SANITIZING_CHAPS);
    }

    /**
     * Set {@link #numChapFiles} to the number of files in the directory which have names like "#.html".
     * @param numChapFiles Number of chapter files in the directory.
     * @throws IllegalStateException if this is called more than once.
     */
    public void setNumChapFiles(int numChapFiles) {
        if (this.numChapFiles != -1) throw new IllegalStateException();
        this.numChapFiles = numChapFiles;
    }

    /**
     * Get the number of chapter files which haven't been turned into {@link bkromhout.fdl.Chapter Chapter}s yet.
     * @return Number of chapters remaining.
     * @throws IllegalStateException if this is called and {@link #numChapFiles} is < 0.
     */
    public int getNumChapsLeft() {
        if (this.numChapFiles < 0) throw new IllegalStateException();
        return numChapFiles - chapters.size();
    }
}
