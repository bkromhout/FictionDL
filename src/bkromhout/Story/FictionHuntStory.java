package bkromhout.Story;

import bkromhout.Main;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Model object for a story.
 */
public class FictionHuntStory {
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
     * Create a new FictionHuntStory object based off of a URL.
     * @param url URL of the story this model represents.
     */
    public FictionHuntStory(String url) throws IOException {
        this.url = url;
        populateInfo();
    }

    /**
     * Populate fields.
     */
    private void populateInfo() throws IOException {
        // Don't do this again if we already did it.
        if (title != null) return;
        // Get the HTML at the url we've specified to use as the entry point.
        Document doc = Main.downloadHtml(url);
        if (doc == null) throw new IOException("Couldn't download story entry point!");
        // Get title string.
        title = doc.select("div.title").first().text();
        // Get author string.
        author = doc.select("div.details > a").first().text();
        // Get details string to extract other bits of information from that.
        String[] details = doc.select("div.details").first().ownText().split(" - ");
        // Get word count.
        wordCount = Integer.parseInt(details[1].replace("Words: ", "").replaceAll(",", ""));
        // Get rating.
        rating = details[2].replace("Rated: ", "");
        // Get number of chapters.
        int numChapters = Integer.parseInt(details[5].replace("Chapters: ", ""));
        // Generate chapter URLs.
        String baseUrl = url.substring(0, url.lastIndexOf('/') + 1);
        for (int i = 0; i < numChapters; i++) chapterUrls.add(baseUrl + String.valueOf(i + 1));
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

    @Override
    public String toString() {
        return String.format("Title: %s\nAuthor: %s\nWord Count: %d\nRating: %s\n", title, author, wordCount, rating);
    }
}
