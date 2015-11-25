package bkromhout.FictionDL.Story;

import bkromhout.FictionDL.C;
import bkromhout.FictionDL.Downloader.FictionHuntDL;
import bkromhout.FictionDL.Util;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Model object for a FictionHunt story. Despite the word "model", this is not an object with a light initialization
 * cost, as it accesses the internet to retrieve story information.
 */
public class FictionHuntStory extends Story {
    // Story URL.
    private String url;
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
        Document doc = Util.downloadHtml(url);
        if (doc == null) throw new IOException(String.format(C.STORY_DL_FAILED, FictionHuntDL.SITE, storyId));
        // Get title string. Even if the story is on FFN, we want to have this for logging purposes.
        title = doc.select("div.title").first().text();
        // Check if story is on Fanfiction.net. If so, just get its FFN story ID.
        ffnStoryId = tryGetFfnStoryId();
        if (ffnStoryId != null) return; // If the story is on FFN, don't bother with the rest!
        // Get author string.
        author = doc.select("div.details > a").first().text();
        // Get the summary. Note that we do this by trying to search FictionHunt for the story title, then parsing
        // the search results. We sort by relevancy, but if the story still doesn't show up on the first page then we
        // just give up and use an apology message as the summary :)
        summary = findSummary();
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
        Document ffnDoc = Util.downloadHtml(String.format(C.FFN_URL, storyId));
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
    private String findSummary() {
        // Generate a FictionHunt search URL using the title.
        String fhSearchUrl = String.format(C.FH_SEARCH_URL, title);
        // Download search page.
        Document fhSearch = Util.downloadHtml(fhSearchUrl);
        if (fhSearch == null) return C.FH_NO_SUMMARY;
        // Get summary.
        Element summaryElement = fhSearch.select(
                String.format("li:has(a[href=\"http://fanfiction.net/s/%s\"]) div.ficContent", storyId)).first();
        return summaryElement != null ? summaryElement.text() : C.FH_NO_SUMMARY;
    }

    public String getFfnStoryId() {
        return ffnStoryId;
    }
}
