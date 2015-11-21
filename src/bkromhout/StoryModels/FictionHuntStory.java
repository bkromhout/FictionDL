package bkromhout.StoryModels;

import org.jsoup.nodes.Document;

import java.util.ArrayList;

/**
 * Model object for a story.
 */
public class FictionHuntStory {
    // Story title.
    private String title;
    // Story author.
    private String author;
    // Story word count.
    private int wordCount;
    // Story rating.
    private String rating;
    // List of chapter URLs
    private ArrayList<String> chapterUrls;

    /**
     * Create a new FictionHuntStory object based off of a Document that contains one of the chapters of the story.
     * @param entryPointDoc Some chapter in the story which
     */
    public FictionHuntStory(Document entryPointDoc) {
        populateInfo(entryPointDoc);
    }

    /**
     * Populate fields.
     * @param doc Document of some chapter of the story.
     */
    private void populateInfo(Document doc) {
        // TODO: this
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
