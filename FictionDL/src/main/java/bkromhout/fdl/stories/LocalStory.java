package bkromhout.fdl.stories;

import bkromhout.fdl.chapter.Chapter;
import bkromhout.fdl.chapter.ChapterSource;
import bkromhout.fdl.ex.InitStoryException;
import bkromhout.fdl.ex.LocalStoryException;
import bkromhout.fdl.ex.StoryinfoJsonException;
import bkromhout.fdl.rx.RxChapAction;
import bkromhout.fdl.rx.RxMakeChapters;
import bkromhout.fdl.util.C;
import bkromhout.fdl.util.ProgressHelper;
import bkromhout.fdl.util.Util;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import rx.Observable;
import rx.schedulers.Schedulers;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Represents a story which is created using local files.
 * <p>
 * Unlike most story classes, this one is in charge of its own {@link Chapter Chapter} creation.
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
     * Tracks whether or not we have already called {@link #setNumChapFiles(int)}, since calling it more than once is a
     * fallacy of logic.
     */
    private boolean numChapFilesAlreadySet;
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
        this.chapTitles = new HashMap<>();
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

        // Get the rest of the story details.
        url = safeGetJsonStr(info, C.J_URL);
        summary = Util.cleanHtmlString(safeGetJsonStr(info, C.J_SUMMARY));
        series = safeGetJsonStr(info, C.J_SERIES);
        ficType = safeGetJsonStr(info, C.J_FIC_TYPE);
        warnings = safeGetJsonStr(info, C.J_WARNINGS);
        warnings = safeGetJsonStr(info, C.J_WARNINGS);
        rating = safeGetJsonStr(info, C.J_RATING);
        genres = safeGetJsonStr(info, C.J_GENRES);
        characters = safeGetJsonStr(info, C.J_CHARACTERS);
        wordCount = safeGetJsonInt(info, C.J_WORD_COUNT);
        datePublished = safeGetJsonStr(info, C.J_DATE_PUBLISHED);
        dateUpdated = safeGetJsonStr(info, C.J_DATE_UPDATED);
        status = safeGetJsonStr(info, C.J_STATUS);

        // Try to get the chapterTitles object.
        JsonObject chapTitlesObj = tryGetChapTitlesObj();
        if (chapTitlesObj == null) return; // If it doesn't exist, or isn't an object, we're done now.

        // Try to create a HashMap<String, String> using the object.
        try {
            chapTitles = new Gson().fromJson(chapTitlesObj, new TypeToken<HashMap<String, String>>() {}.getType());
        } catch (JsonParseException | ClassCastException | IllegalStateException e) {
            // The object exists, but its contents are not formed in such a way that we can turn it into a
            // HashMap<String, String>.
            Util.logf(C.JSON_BAD_ELEM_TITLE, title, C.J_CHAPTER_TITLES);
        }
    }

    /**
     * Calls through to {@link Util#getJsonStr(JsonObject, String)}, but if it throws an exception, catches it, prints a
     * log line, and returns null.
     * <p>
     * The log line used is {@link C#JSON_BAD_ELEM_TITLE}, which requires a story title and an element name.
     * @param json     JSON object which contains an element called {@code elemName}.
     * @param elemName Name of the JSON element to get the String value of.
     * @return Return value of {@link Util#getJsonStr(JsonObject, String)}, or null an exception is caught.
     */
    private String safeGetJsonStr(JsonObject json, String elemName) {
        try {
            return Util.getJsonStr(json, elemName);
        } catch (StoryinfoJsonException e) {
            // Print a warning and return null.
            Util.logf(C.JSON_BAD_ELEM_TITLE, title, elemName);
            return null;
        }
    }

    /**
     * Calls through to {@link Util#getJsonInt(JsonObject, String)}, but if it throws an exception, catches it, prints a
     * log line, and returns -1.
     * <p>
     * The log line used is {@link C#JSON_BAD_ELEM_TITLE}, which requires a story title and an element name.
     * @param json     JSON object which contains an element called {@code elemName}.
     * @param elemName Name of the JSON element to get the integer value of.
     * @return Return value of {@link Util#getJsonInt(JsonObject, String)}, or -1 an exception is caught.
     */
    private int safeGetJsonInt(JsonObject json, String elemName) {
        try {
            return Util.getJsonInt(json, elemName);
        } catch (StoryinfoJsonException e) {
            // Print a warning and return null.
            Util.logf(C.JSON_BAD_ELEM_TITLE, title, elemName);
            return -1;
        }
    }

    /**
     * Try to get the "chapterTitles" object from {@link #storyInfo}.
     * @return The object if is exists, otherwise null.
     */
    private JsonObject tryGetChapTitlesObj() {
        if (!storyInfo.has(C.J_CHAPTER_TITLES)) return null;
        JsonElement elem = storyInfo.get(C.J_CHAPTER_TITLES);
        if (!elem.isJsonObject()) {
            Util.logf(C.JSON_BAD_ELEM_TITLE, title, C.J_CHAPTER_TITLES);
            return null;
        }
        return elem.getAsJsonObject();
    }

    /**
     * Attempts to read all chapter HTML files in this local story's directory and make {@link Chapter Chapter}s from
     * them.
     * @throws LocalStoryException   if there were issues while processing chapter files.
     * @throws IllegalStateException if this is called and {@link #numChapFiles} is < 1.
     */
    public void processChapters() throws LocalStoryException {
        if (this.numChapFiles < 1) throw new IllegalStateException();

        // Have ProgressHelper recalculate the worth of work units based on the number of chapters we expect to process.
        ProgressHelper.recalcUnitWorth(numChapFiles);

        // Create chapters using an Observable from a range [1..numChapFiles]. We use the IO Scheduler from the start
        // for two reasons; First is, obviously, that we'll be reading files at some point; Second is that we don't
        // need to keep anything in order since the chapter filenames have the chapter number in them.
        chapters = (ArrayList<Chapter>) Observable
                //.range(1, numChapFiles)
                .range(1, numChapFiles, Schedulers.io())
                //.subscribeOn(Schedulers.newThread())
                .map(i -> storyDir.resolve(String.format("%d.html", i)).toFile()) // Create Files.
                .map(f -> new ChapterSource(f, true)) // Wrap the Files in ChapterSource objects.
                .compose(new RxMakeChapters(this)) // Create Chapter objects.
                //.observeOn(Schedulers.io()) // Using the IO scheduler, since we're reading from files.
                .filter(chap -> chap != null) // Filter out nulls.
                .compose(new RxChapAction(chapter -> {
                    // Give the chapters titles; either one parsed from storyinfo.json, or "Chapter #".
                    String t = chapTitles.get(String.valueOf(chapter.number));
                    chapter.title = (t != null && !t.isEmpty()) ? t : String.format("Chapter %d", chapter.number);
                }))
                .compose(new RxChapAction(Chapter::wrapContentInTemplate)) // Wrap chapter content in chapter template.
                .compose(new RxChapAction(Chapter::sanitizeContent)) // Sanitize chapter content.
                .compose(new RxChapAction(chapter -> ProgressHelper.finishedWorkUnit()))
                .doOnCompleted(() -> Util.log(C.SANITIZING_CHAPS))
                .observeOn(Schedulers.immediate())
                .toSortedList(Chapter::sort) // Get the chapters as a properly sorted list.
                .toBlocking()
                .single();

        // Make sure that we got all of the chapters, throw an exception if we didn't.
        if (chapters.size() != numChapFiles) throw new LocalStoryException(C.PARTIAL_READ_FAIL);
    }

    /**
     * Set {@link #numChapFiles} to the number of files in the directory which have names like "#.html".
     * @param numChapFiles Number of chapter files in the directory.
     * @throws IllegalStateException if this is called more than once.
     * @throws InitStoryException    if {@code numChapFiles} is < 1.
     */
    public void setNumChapFiles(int numChapFiles) throws InitStoryException {
        if (numChapFilesAlreadySet) throw new IllegalStateException();
        if (numChapFiles < 1) throw new InitStoryException(C.NO_CHAP_FILES, title);
        this.numChapFiles = numChapFiles;
        this.numChapFilesAlreadySet = true;
    }

    /**
     * Get the number of chapter files which haven't been turned into {@link Chapter Chapter}s yet.
     * @return Number of chapters remaining.
     * @throws IllegalStateException if this is called and {@link #numChapFiles} is < 1.
     */
    public int getNumChapsLeft() {
        if (this.numChapFiles < 1) throw new IllegalStateException();
        return numChapFiles - chapters.size();
    }
}
