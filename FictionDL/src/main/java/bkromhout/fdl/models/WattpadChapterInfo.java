package bkromhout.fdl.models;

import com.google.gson.annotations.SerializedName;

/**
 * Holds information about a Wattpad story chapter retrieved from the Wattpad API.
 */
public class WattpadChapterInfo {
    /**
     * Chapter ID.
     */
    private long id;
    /**
     * Chapter title.
     */
    private String title;
    /**
     * Chapter word count.
     */
    private int wordCount;
    /**
     * Chapter text url object.
     */
    @SerializedName("text_url")
    private WattpadChapterTextUrl chapterTextUrl;

    /**
     * Get the chapter ID.
     * @return Chapter ID.
     */
    public long getId() {
        return id;
    }

    /**
     * Get the chapter title.
     * @return Chapter title.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Get the chapter word count.
     * @return Chapter word count.
     */
    public int getWordCount() {
        return wordCount;
    }

    /**
     * Get the url of the chapter's text content.
     * @return Chapter text url.
     */
    public String getChapterTextUrl() {
        return chapterTextUrl.chapterTextUrl;
    }

    /**
     * Holds the link to the raw HTML content of a Wattpad story chapter.
     */
    private static class WattpadChapterTextUrl {
        /**
         * Url of the chapter's actual content.
         */
        @SerializedName("text")
        private String chapterTextUrl;
    }
}
