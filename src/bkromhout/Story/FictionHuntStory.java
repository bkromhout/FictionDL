package bkromhout.Story;

import bkromhout.C;
import bkromhout.Main;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Pattern;

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
    // If the story is still available on Fanfiction.net, get its story ID and use p0ody-files to download it.
    private String ffnStoryId = null;

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
        if (doc == null) throw new IOException(C.ENTRY_PT_DL_FAILED);
        // Check if story is on Fanfiction.net. If so, just get its FFN story ID.
        ffnStoryId = tryGetFfnStoryId();
        if (ffnStoryId != null) return; // If the story is on FFN, don't bother with the rest!
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

    /**
     * Parse the entry point for the link to FFN and download the page at that link. If it's a valid story (i.e., it
     * hasn't been taken down), then return its story ID so that we can use p0ody-files to download it later.
     * @return Story ID if on FFN, or -1 if not.
     */
    private String tryGetFfnStoryId() {
        // FictionHunt has done a very handy thing with their URLs, their story IDs correspond to the original FFN
        // story IDs, which makes generating an FFN link easy to do. First, we need to get the story ID from the
        // FictionHunt URL.
        String storyId = Pattern.compile(C.FICTIONHUNT_REGEX).matcher(url).group(5);
        // The create a FFN link and download the resulting page.
        Document ffnDoc = Main.downloadHtml(String.format(C.FFN_LINK, storyId));
        if (ffnDoc == null) {
            // It really doesn't matter if we can't get the page from FFN since we can still get it from FictionHunt.
            System.out.println(C.FH_FFN_CHECK_FAILED);
            return null;
        }
        // Now check the resulting FFN HTML to see if the warning panel which indicates that the story isn't
        // available is present. If it is present, the story isn't on FFN anymore, so return a null; otherwise, the
        // story is still up, return the real story ID.
        return ffnDoc.select("span.gui_warning").first() != null ? null : storyId;
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

    public String getFfnStoryId() {
        return ffnStoryId;
    }

    @Override
    public String toString() {
        return String.format("Title: %s\nAuthor: %s\nWord Count: %d\nRating: %s\n", title, author, wordCount, rating);
    }
}
