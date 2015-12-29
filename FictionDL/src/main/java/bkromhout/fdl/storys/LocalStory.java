package bkromhout.fdl.storys;

import bkromhout.fdl.ex.InitStoryException;
import bkromhout.fdl.ex.LocalStoryException;
import bkromhout.fdl.ex.StoryinfoJsonException;
import bkromhout.fdl.util.C;
import bkromhout.fdl.util.Util;
import com.google.gson.JsonObject;

import java.nio.file.Path;
import java.util.HashMap;

/**
 * Represents a story which is created using local files.
 * <p>
 * Unlike most story classes, this one is in charge of its own {@link bkromhout.fdl.Chapter Chapter} creation.
 * @author Brenden Kromhout
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
     * Chapter titles which were parsed from storyinfo.json.
     */
    private HashMap<String, String> chapTitles;

    /**
     * Create a new {@link LocalStory} using the given json object from a storyinfo.json file and the given directory
     * path.
     * <p>
     * It is assumed that {@code storyInfo} and {@code storyDir} are both nonnull and valid.
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

        // Populate Story fields using story info JSON.
        JsonObject info = storyInfo.getAsJsonObject(C.J_INFO);
        // We're already guaranteed to have both the title and author.
        title = info.getAsJsonPrimitive(C.J_TITLE).getAsString();
        author = info.getAsJsonPrimitive(C.J_AUTHOR).getAsString();

        // Everything else we have to wrap in a try/catch for safety.
        try {
            // Get the rest of the story details.
            url = Util.getJsonStr(info, C.J_URL);
            summary = Util.cleanHtmlString(Util.getJsonStr(info, C.J_SUMMARY));
            series = Util.getJsonStr(info, C.J_SERIES);
            ficType = Util.getJsonStr(info, C.J_FIC_TYPE);
            warnings = Util.getJsonStr(info, C.J_WARNINGS);
            warnings = Util.getJsonStr(info, C.J_WARNINGS);
            rating = Util.getJsonStr(info, C.J_RATING);
            genres = Util.getJsonStr(info, C.J_GENRES);
            characters = Util.getJsonStr(info, C.J_CHARACTERS);
            wordCount = Util.getJsonInt(info, C.J_WORD_COUNT);
            datePublished = Util.getJsonStr(info, C.J_DATE_PUBLISHED);
            dateUpdated = Util.getJsonStr(info, C.J_DATE_UPDATED);
            status = Util.getJsonStr(info, C.J_STATUS);

            // Try to get a map containing any chapter titles that were provided.
            chapTitles = Util.getJsonStrMap(storyInfo, C.J_CHAPTER_TITLES);
        } catch (StoryinfoJsonException e) {
            // The element we were trying to get a value from exists, but we had issues while getting the value from it.
            // The string in the exception is the name of the element we were trying to access when this exception
            // occurred, so we'll use that to create a proper message.
            throw new InitStoryException(String.format(C.JSON_BAD_ELEM_T, title, e.getMessage()), e);
        }
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
