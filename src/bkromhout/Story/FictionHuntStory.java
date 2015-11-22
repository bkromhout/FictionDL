package bkromhout.Story;

import bkromhout.C;
import bkromhout.Downloader.FictionHuntDL;
import bkromhout.Main;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Model object for a FictionHunt story. Despite the word "model", this is not an object with a light initialization
 * cost, as it accesses the internet to retrieve story information.
 */
public class FictionHuntStory {
    // Story URL.
    private String url;
    // Story ID (FictionHunt).
    private String storyId;
    // Story title.
    private String title;
    // Story author.
    private String author;
    // Story Summary.
    private String summary;
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
        // Get FictionHunt story ID.
        storyId = getStoryId();
        // Get the HTML at the url we've specified to use as the entry point.
        Document doc = Main.downloadHtml(url);
        if (doc == null) throw new IOException(String.format(C.STORY_DL_FAILED, FictionHuntDL.SITE, storyId));
        // Get title string. Even if the story is on FFN, we want to have this for logging purposes.
        title = doc.select("div.title").first().text();
        // Check if story is on Fanfiction.net. If so, just get its FFN story ID.
        ffnStoryId = tryGetFfnStoryId();
        if (ffnStoryId != null) return; // If the story is on FFN, don't bother with the rest!
        // Get author string.
        author = doc.select("div.details > a").first().text();
        // Get details string to extract other bits of information from that. TODO use regex for this bc yay.
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
     * Parses the FictionHunt story ID from the FictionHunt URL.
     * @return FictionHunt story ID.
     */
    private String getStoryId() {
        Matcher matcher = Pattern.compile(C.FICTIONHUNT_REGEX).matcher(url);
        matcher.find();
        return storyId = matcher.group(1);
    }

    /**
     * Parse the entry point for the link to FFN and download the page at that link. If it's a valid story (i.e., it
     * hasn't been taken down), then return its story ID so that we can use p0ody-files to download it later.
     * @return Story ID if on FFN, or null if not.
     */
    private String tryGetFfnStoryId() {
        // FictionHunt has done a very handy thing with their URLs, their story IDs correspond to the original FFN
        // story IDs, which makes generating an FFN link easy to do. First, create a FFN link and download the
        // resulting page.
        Document ffnDoc = Main.downloadHtml(String.format(C.FFN_URL, storyId));
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

    /**
     * Uses the FictionHunt search to attempt to find the story summary.
     * @return Story summary.
     */
    private String getSummary() {
        // TODO.
        return null;
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
}
