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
    // List of chapter URLs.
    private ArrayList<String> chapterUrls;
    // List of real chapter names, or null if not found.
    private ArrayList<String> chapterNames;

    /**
     * Create a new FictionHuntStory object based off of a Document that contains one of the chapters of the story.
     * @param entryPointDoc Some chapter in the story which this model should represent.
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

        // Generate chapter names from FanFiction.net, or fall back to "Chapter #" if the story isn't available there.
        tryGetRealChapterNames();
    }

    /**
     * Attempt to follow the link to FanFiction.net to find out what the real chapter names are. If the story has been
     * removed, then just fall back to "Chapter #" format.
     */
    private void tryGetRealChapterNames() {
        chapterNames = new ArrayList<>();
        for (int i = 0; i < chapterUrls.size(); i++) {
            // TODO make this actually useful lol.
            chapterNames.add(String.format("Chapter %d", i + 1));
        }
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

    public ArrayList<String> getChapterNames() {
        return chapterNames;
    }

    @Override
    public String toString() {
        return String.format("Title: %s\nAuthor: %s\nWord Count: %d\nRating: %s\n", title, author, wordCount, rating);
    }
}
