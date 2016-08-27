package bkromhout.fdl.chapter;

import bkromhout.fdl.stories.Story;
import bkromhout.fdl.util.C;
import bkromhout.fdl.util.Util;
import com.google.common.io.Files;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Represents an object which can be used to create a new {@link Chapter} object.
 */
public class ChapterSource {
    /* Possible sources, only one will be nonnull. */
    private final Response response;
    private final File file;
    private final String string;
    /**
     * Chapter number.
     */
    private int number;
    /**
     * If true, then we will run file sources' contents through Jsoup before creating a {@link Chapter} with them.
     */
    private boolean runThroughParser;
    /**
     * Story which the created {@link Chapter} will belong to.
     */
    private Story story;

    /**
     * Create a new {@link ChapterSource} using an OkHttp Response.
     * @param response Response.
     * @throws IllegalArgumentException if {@code response} is null.
     */
    public ChapterSource(Response response) {
        if (response == null) throw new IllegalArgumentException();
        this.response = response;
        this.file = null;
        this.string = null;
    }

    /**
     * Create a new {@link ChapterSource} using a File.
     * @param file             File.
     * @param runThroughParser If true, will run the contents of the file through Jsoup first.
     * @throws IllegalArgumentException if {@code file} is null.
     */
    public ChapterSource(File file, boolean runThroughParser) {
        if (file == null) throw new IllegalArgumentException();
        this.response = null;
        this.file = file;
        this.string = null;
        this.runThroughParser = runThroughParser;
    }

    /**
     * Create a new {@link ChapterSource} using a String.
     * @param string String.
     * @param number Chapter number, must be >= 1.
     * @throws IllegalArgumentException if {@code string} is null or {@code number} < 1.
     */
    public ChapterSource(String string, int number) {
        if (string == null || number < 1) throw new IllegalArgumentException();
        this.response = null;
        this.file = null;
        this.string = string;
        this.number = number;
    }

    /**
     * Have this {@link ChapterSource} process its data to produce a new {@link Chapter}.
     * <p>
     * The returned chapter object will have {@link Chapter#number number} and one of {@link Chapter#rawHtml rawHtml} or
     * {@link Chapter#content content} populated.
     * @param story The {@code Story} which the returned chapter will belong to.
     * @return New {@link Chapter}. If there was an issue creating it, {@code story} is null, or we don't have a source,
     * return null instead.
     */
    public Chapter toChapter(Story story) {
        if (story == null || (response == null && file == null && string == null)) return null;

        this.story = story;
        number = chapNum();

        // Create a Chapter.
        if (response != null) return fromResponse();
        if (file != null) return fromFile();
        return !string.isEmpty() ? new Chapter(story, string, number) : null;
    }

    /**
     * Get a {@link Chapter} from an OkHttp Response.
     * @return New {@link Chapter}, or null if we had issues.
     */
    private Chapter fromResponse() {
        assert response != null;
        try {
            Document doc = Jsoup.parse(response.body().byteStream(), null, response.request().url().toString());
            // Make sure the ResponseBody is closed so that it doesn't leak.
            response.body().close();
            return new Chapter(story, doc, number);
        } catch (IOException e) {
            Util.loudf(C.PARSE_HTML_FAILED, response.request().url());
            return null;
        }
    }

    /**
     * Get a {@link Chapter} from a File.
     * @return New {@link Chapter}, or null if we had issues.
     */
    private Chapter fromFile() {
        assert file != null;

        // Ensure File is actually a file.
        if (!file.isFile()) {
            Util.logf(C.MISSING_CHAP_FILE, story.getTitle(), number);
            return null;
        }

        // Read the contents of the file into a String.
        Util.loudf(C.READING_CHAP_FILE, file.getName());
        String contents;
        try {
            contents = Files.toString(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            // Issues while reading the file.
            Util.logf(C.MALFORMED_CHAP_FILE, story.getTitle(), number);
            return null;
        }

        if (runThroughParser) {
            // Run the contents through Jsoup to ensure it's valid HTML.
            try {
                contents = Jsoup.parseBodyFragment(contents).body().html().trim();
            } catch (Exception e) {
                // Issue while parsing the HTML string.
                Util.logf(C.MALFORMED_CHAP_FILE, story.getTitle(), number);
                return null;
            }
        }

        // Create a Chapter object using the contents of the file.
        return new Chapter(story, contents, number);
    }

    /**
     * Get the number of a chapter.
     * @return Chapter number.
     * @throws IllegalStateException if we can't get a chapter number.
     */
    private int chapNum() {
        // Don't redo work.
        if (number > 0) return number;

        // If the source is a Response, we can use the request url to get the number by finding its index in the
        // Story's list of urls.
        if (response != null) return story.getChapterUrls().indexOf(response.request().url().toString()) + 1;

        // If the source is a file, we can use the filename to get the number.
        if (file != null) return Integer.parseInt(file.getName().split("\\.")[0]);

        throw new IllegalStateException();
    }
}
