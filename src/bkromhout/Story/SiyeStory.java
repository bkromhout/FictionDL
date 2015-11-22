package bkromhout.Story;

import java.util.ArrayList;

/**
 * Model object for a SIYE story.
 */
public class SiyeStory {
    // Story URL.
    private String url;
    // Story title.
    private String title;
    // Story author.
    private String author;
    // Story word count.
    private int wordCount;
    // Story rating.
    private String rating;
    // List of chapter URLs.
    private ArrayList<String> chapterUrls = new ArrayList<>();

    /**
     *
     * @param url
     */
    public SiyeStory(String url) {

    }

    public String getUrl() {
        return url;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public int getWordCount() {
        return wordCount;
    }

    public String getRating() {
        return rating;
    }

    public ArrayList<String> getChapterUrls() {
        return chapterUrls;
    }
}
