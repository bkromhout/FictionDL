package bkromhout.fdl.stories;

import bkromhout.fdl.ex.InitStoryException;
import bkromhout.fdl.parsing.StoryEntry;
import bkromhout.fdl.site.Sites;
import bkromhout.fdl.util.C;
import bkromhout.fdl.util.Util;
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
    /**
     * Whether or not the story is still available on FanFiction.net.
     */
    private boolean isOnFfn;

    /**
     * Create a new {@link FictionHuntStory}.
     * @param storyEntry Story entry with details from the input file.
     * @throws InitStoryException if we can't create this story object for some reason.
     */
    public FictionHuntStory(StoryEntry storyEntry) throws InitStoryException {
        super(storyEntry, Sites.FH());
    }

    @Override
    protected void populateInfo() throws InitStoryException {
        // Get story ID, then download the url so that we can parse story info.
        storyId = parseStoryId(url, "/read/(\\d*)", 1);
        Document infoDoc = Util.getHtml(url);
        if (infoDoc == null) throw new InitStoryException(C.STORY_DL_FAILED, site.getName(), storyId);

        title = hasDetailTag(C.J_TITLE) ? detailTags.get(C.J_TITLE)
                : infoDoc.select("div.title").first().text();

        // Check if story is on FanFiction.net. If so, we'll download it from there, so we're done here.
        if (isOnFfn = checkIfOnFfn()) return;

        author = hasDetailTag(C.J_AUTHOR) ? detailTags.get(C.J_AUTHOR)
                : infoDoc.select("div.details > a").first().text();

        summary = hasDetailTag(C.J_SUMMARY) ? detailTags.get(C.J_SUMMARY)
                : findSummary();

        // Get details string to extract other bits of information from that.
        String[] details = infoDoc.select("div.details").first().ownText().split(" - ");

        characters = hasDetailTag(C.J_CHARACTERS) ? detailTags.get(C.J_CHARACTERS)
                : details[0].trim();

        wordCount = Integer.parseInt(details[1].trim().replace("Words: ", "").replace(",", ""));

        rating = hasDetailTag(C.J_RATING) ? detailTags.get(C.J_RATING)
                : details[2].replace("Rated: ", "").trim();

        genres = hasDetailTag(C.J_GENRES) ? detailTags.get(C.J_GENRES)
                : details[4].trim();

        int chapCount = Integer.parseInt(details[5].trim().replace("Chapters: ", ""));
        dateUpdated = details[7].trim().replace("Updated: ", "");
        datePublished = details[8].trim().replace("Published: ", "");
        // Get status (it isn't listed if it's incomplete, so just check the length of the details array).
        status = details.length > 9 ? C.STAT_C : C.STAT_I;

        // Detail tags which the site doesn't support.
        if (hasDetailTag(C.J_FIC_TYPE)) ficType = detailTags.get(C.J_FIC_TYPE);
        if (hasDetailTag(C.J_SERIES)) series = detailTags.get(C.J_SERIES);
        if (hasDetailTag(C.J_WARNINGS)) warnings = detailTags.get(C.J_WARNINGS);

        // Generate chapter urls.
        for (int i = 0; i < chapCount; i++)
            chapterUrls.add(String.format("http://fictionhunt.com/read/%s/%d", storyId, i + 1));
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
        Document ffnDoc = Util.getHtml(String.format(FanFictionStory.FFN_S_URL, storyId));
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
     * <p>
     * We sort the search results by relevancy, but if the story still doesn't show up on the first page then we just
     * give up and use an apology message as the summary.
     * @return Story summary, or apology message.
     */
    private String findSummary() {
        // Generate a FictionHunt search url using the title.
        String fhSearchUrl = String.format("http://fictionhunt.com/5/0/0/0/0/0/0/0/0/0/0/%s/1", title);
        // Download search page.
        Document fhSearch = Util.getHtml(fhSearchUrl);
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
