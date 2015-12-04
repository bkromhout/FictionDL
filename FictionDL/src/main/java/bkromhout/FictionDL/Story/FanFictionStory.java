package bkromhout.FictionDL.Story;

import bkromhout.FictionDL.C;
import bkromhout.FictionDL.Downloader.FanFictionDL;
import bkromhout.FictionDL.Util;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

/**
 * Model object for a FanFiction.net story. Despite the word "model", this is not an object with a light initialization
 * cost, as it accesses the internet to retrieve story information.
 */
public class FanFictionStory extends Story {

    /**
     * Create a new FanFictionStory object based off of a URL.
     * @param url URL of the story this model represents.
     */
    public FanFictionStory(String url) throws IOException {
        this.url = url;
        populateInfo();
    }

    /**
     * Populate this model's fields.
     * @throws IOException Throw for many reasons, but the net result is that we can't build a story model for this.
     */
    private void populateInfo() throws IOException {
        // Set site.
        site = C.HOST_FFN;
        // Get story ID first.
        storyId = parseStoryId(url, C.FFN_SID_REGEX, 1);
        // Normalize the URL, since there are many valid FFN URL formats.
        url = String.format(C.FFN_S_URL, storyId);
        // Get the first chapter in order to parse the story info.
        Document infoDoc = Util.downloadHtml(url);
        // Make sure that we got a Document and that this is a valid story.
        if (infoDoc == null || infoDoc.select("span.gui_warning").first() != null)
            throw new IOException(String.format(C.STORY_DL_FAILED, FanFictionDL.SITE, storyId));
        // Get the title.
        title = infoDoc.select("div#profile_top b").first().html().trim();
        // Get the author.
        author = infoDoc.select("div#profile_top a[href~=" + C.FFN_AUTHOR_LINK_REGEX + "]").first().html().trim();
        // Get the summary.
        summary = infoDoc.select("div#profile_top > div").first().html().trim();
        // Get the fic type.
        ficType = parseFicType(infoDoc);
        // Get the details string and split it up to help get the other details.
        Element detailElem = infoDoc.select("div#profile_top > span").last();
        String[] details = detailElem.text().split(" - ");
        // Get the rating.
        rating = details[0].replace("Rated: ", "").trim();
        // Get the chapter count, remember its index for later.
        int chapCntIdx = findDetailsStringIdx(details, "Chapters: ");
        int chapCount = chapCntIdx == -1 ? 1 : Integer.parseInt(details[chapCntIdx].replace("Chapters: ", "")
                .replace(",", "").trim());
        // Get the word count.
        int wordCntIdx = findDetailsStringIdx(details, "Words: ");
        wordCount = Integer.parseInt(details[wordCntIdx].replace("Words: ", "").replace(",", "").trim());
        // Get the genre(s).
        genres = parseGenres(details, chapCntIdx != -1 ? chapCntIdx : wordCntIdx);
        // Get the characters.
        characters = details[genres.equals(C.NO_GENRE) ? 2 : 3].trim();
        // Get the dates.
        Elements dates = detailElem.select("span > span");
        // Get the date published.
        datePublished = Util.dateFromFfnTime(dates.last().attr("data-xutime"));
        // Get the date last updated. If there isn't one, use the date published.
        dateUpdated = dates.size() > 1 ? Util.dateFromFfnTime(dates.first().attr("data-xutime")) : datePublished;
        // Get the status.
        status = findDetailsStringIdx(details, "Status: Complete") != -1 ? C.STAT_C : C.STAT_I;
        // Generate chapter URLs.
        for (int i = 0; i < chapCount; i++) chapterUrls.add(String.format(C.FFN_C_URL, storyId, i + 1));
    }

    /**
     * Parse the fic type.
     * @param infoDoc The info HTML.
     * @return The fic type.
     */
    private String parseFicType(Document infoDoc) {
        // We need to see if this is a crossover or not.
        Element element = infoDoc.select("img[src=\"/static/fcons/arrow-switch.png\"]").first();
        if (element == null) {
            // Regular, non-crossover fic.
            element = infoDoc.select("span.lc-left").first();
            StringBuilder ficType = new StringBuilder();
            for (Element part : element.select("a")) ficType.append(part.text()).append(" > ");
            return ficType.delete(ficType.length() - 3, ficType.length()).toString();
        } else {
            // Crossover fic.
            return element.nextElementSibling().text().trim();
        }
    }

    /**
     * Parse the story genre(s).
     * @param details The details string array.
     * @param relIdx  Either the index of the chapter count in the details string, or if that doesn't exist, the index
     *                of the word count.
     * @return The story genre(s).
     */
    private String parseGenres(String[] details, int relIdx) {
        // If the relative index is 3, then there wasn't a specific genre string. We'll call it "None/Gen"
        if (relIdx == 3) return C.NO_GENRE;
        // If the relative index is 4, then we return the details string at index 2.
        return details[2].trim();
    }

    /**
     * Find the index of the details string that has the search string in it.
     * @param details The details string array.
     * @param search  The search string.
     * @return The index of the details string containing the search string, or -1.
     */
    private int findDetailsStringIdx(String[] details, String search) {
        for (int i = 0; i < details.length; i++) if (details[i].contains(search)) return i;
        return -1;
    }
}
