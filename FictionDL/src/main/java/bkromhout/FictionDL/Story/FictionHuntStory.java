package bkromhout.FictionDL.Story;

import bkromhout.FictionDL.C;
import bkromhout.FictionDL.Downloader.ParsingDL;
import bkromhout.FictionDL.Util;
import bkromhout.FictionDL.ex.InitStoryException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * Model object for a FictionHunt story. Despite the word "model", this is not an object with a light initialization
 * cost, as it accesses the internet to retrieve story information.
 */
public class FictionHuntStory extends Story {
    // If the story is still available on FanFiction.net, get its story ID and use p0ody-files to download it.
    private boolean isOnFfn = false;

    /**
     * Create a new FictionHuntStory object based off of a URL.
     * @param ownerDl The downloader which owns this story.
     * @param url URL of the story this model represents.
     */
    public FictionHuntStory(ParsingDL ownerDl, String url) throws InitStoryException {
        super(ownerDl, url);
    }

    /**
     * Populate this model's fields.
     * @throws InitStoryException Thrown for many reasons, but the net result is that we can't build a story model.
     */
    @Override
    protected void populateInfo() throws InitStoryException {
        // Set site.
        hostSite = C.HOST_FH;
        // Get FictionHunt story ID.
        storyId = parseStoryId(url, C.FH_SID_REGEX, 1);
        // Get the HTML at the url we've specified to use as the entry point.
        Document infoDoc = Util.downloadHtml(url);
        if (infoDoc == null) throw initEx();
        // Get title string. Even if the story is on FFN, we want to have this for logging purposes.
        title = infoDoc.select("div.title").first().text();
        // Check if story is on FanFiction.net. If so, just get its FFN story ID.
        isOnFfn = checkIfOnFfn();
        if (isOnFfn) return; // If the story is on FFN, don't bother with the rest!
        // Get author string.
        author = infoDoc.select("div.details > a").first().text();
        // Get the summary. Note that we do this by trying to search FictionHunt for the story title, then parsing
        // the search results. We sort by relevancy, but if the story still doesn't show up on the first page then we
        // just give up and use an apology message as the summary :)
        summary = findSummary();
        // Get details string to extract other bits of information from that.
        String[] details = infoDoc.select("div.details").first().ownText().split(" - ");
        // Get characters.
        characters = details[0].trim();
        // Get word count.
        wordCount = Integer.parseInt(details[1].trim().replace("Words: ", "").replaceAll(",", ""));
        // Get rating.
        rating = details[2].replace("Rated: ", "").trim();
        // Get genres.
        genres = details[4].trim();
        // Get number of chapters.
        int chapCount = Integer.parseInt(details[5].trim().replace("Chapters: ", ""));
        // Get last updated date.
        dateUpdated = details[7].trim().replace("Updated: ", "");
        // Get published date.
        datePublished = details[8].trim().replace("Published: ", "");
        // Get status (it isn't listed if it's incomplete, so just check the length of the details array).
        status = details.length > 9 ? C.STAT_C : C.STAT_I;
        // Generate chapter URLs.
        for (int i = 0; i < chapCount; i++) chapterUrls.add(String.format(C.FH_C_URL, storyId, i + 1));
    }

    /**
     * Parse the entry point for the link to FFN and download the page at that link. If it's a valid story (i.e., it
     * hasn't been taken down), then return its story ID so that we can use p0ody-files to download it later.
     * @return Story ID if on FFN, or null if not.
     */
    private boolean checkIfOnFfn() {
        // FictionHunt has done a very handy thing with their URLs, their story IDs correspond to the original FFN
        // story IDs, which makes generating an FFN link easy to do. First, create a FFN link and download the
        // resulting page.
        Document ffnDoc = Util.downloadHtml(String.format(C.FFN_S_URL, storyId));
        if (ffnDoc == null) {
            // It really doesn't matter if we can't get the page from FFN since we can still get it from FictionHunt.
            Util.log(C.FH_FFN_CHECK_FAILED);
            return false;
        }
        // Now check the resulting FFN HTML to see if the warning panel which indicates that the story isn't
        // available is present. If it is present, the story isn't on FFN anymore, so return a null; otherwise, the
        // story is still up, return the real story ID.
        return ffnDoc.select("span.gui_warning").first() == null;
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

    /**
     * Is this story still available on FanFiction.net?
     * @return True if yes, false if no.
     */
    public boolean isOnFfn() {
        return isOnFfn;
    }
}
