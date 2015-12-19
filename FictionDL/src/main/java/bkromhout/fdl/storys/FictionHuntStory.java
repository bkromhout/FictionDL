package bkromhout.fdl.storys;

import bkromhout.fdl.util.C;
import bkromhout.fdl.Site;
import bkromhout.fdl.util.Util;
import bkromhout.fdl.downloaders.ParsingDL;
import bkromhout.fdl.ex.InitStoryException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * Model object for a <a href="http://www.fictionhunt.com">FictionHunt</a> story.
 */
public class FictionHuntStory extends Story {
    /**
     * For FictionHunt stories, used in place of a summary if we can't find the story on the first page of search
     * results (which is what we do to try and get summaries from FictionHunt).
     */
    private static final String FH_NO_SUMMARY = "(Couldn't get summary from FictionHunt, sorry!)";

    // If the story is still available on FanFiction.net, get its story ID and use p0ody-files to download it.
    private boolean isOnFfn;

    /**
     * Create a new {@link FictionHuntStory} based off of a url.
     * @param ownerDl The parsing downloader which owns this story.
     * @param url     url of the story this model represents.
     * @throws InitStoryException if we can't create this story object for some reason.
     */
    public FictionHuntStory(ParsingDL ownerDl, String url) throws InitStoryException {
        super(ownerDl, url, Site.FH);
    }

    @Override
    protected void populateInfo() throws InitStoryException {
        // Get FictionHunt story ID.
        storyId = parseStoryId(url, "/read/(\\d*)", 1);
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
        // Generate chapter urls.
        for (int i = 0; i < chapCount; i++)
            chapterUrls.add(
                    String.format("http://fictionhunt.com/read/%s/%d", storyId, i + 1));
    }

    /**
     * Parse the entry point for the link to FFN and download the page at that link. If it's a valid story (i.e., it
     * hasn't been taken down), then we'll download it from FFN instead of FictionHunt.
     * @return Story ID if on FFN, or null if not.
     */
    private boolean checkIfOnFfn() {
        // FictionHunt has done a very handy thing with their urls, their story IDs correspond to the original FFN
        // story IDs, which makes generating an FFN link easy to do. First, create a FFN link and download the
        // resulting page.
        Document ffnDoc = Util.downloadHtml(String.format(FanFictionStory.FFN_S_URL, storyId));
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
        // Generate a FictionHunt search url using the title.
        String fhSearchUrl = String.format("http://fictionhunt.com/5/0/0/0/0/0/0/0/0/0/0/%s/1", title);
        // Download search page.
        Document fhSearch = Util.downloadHtml(fhSearchUrl);
        if (fhSearch == null) return FH_NO_SUMMARY;
        // Get summary.
        Element summaryElement = fhSearch.select(
                String.format("li:has(a[href=\"http://fanfiction.net/s/%s\"]) div.ficContent", storyId)).first();
        return summaryElement != null ? summaryElement.text() : FH_NO_SUMMARY;
    }

    /**
     * Is this story still available on FanFiction.net?
     * @return True if yes, false if no.
     */
    public boolean isOnFfn() {
        return isOnFfn;
    }
}
