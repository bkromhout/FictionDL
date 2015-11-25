package bkromhout.FictionDL.Story;

import bkromhout.FictionDL.C;
import bkromhout.FictionDL.Downloader.SiyeDL;
import bkromhout.FictionDL.Util;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Model object for a SIYE story. Despite the word "model", this is not an object with a light initialization cost, as
 * it accesses the internet to retrieve story information.
 */
public class SiyeStory extends Story {

    /**
     * Create a new SiyeStory object based off of a URL.
     * @param url URL of the story this model represents.
     */
    public SiyeStory(String url) throws IOException {
        populateInfo(url);
    }

    /**
     * Populate this model's fields.
     * @param url An SIYE story/chapter URL.
     * @throws IOException Thrown if there are issues connecting to SIYE
     */
    private void populateInfo(String url) throws IOException {
        // Get chapter 1 HTML first.
        Document infoDoc = getInfoPage(url);
        // Get summary.

        // Get characters.

        // Figure out the SIYE story ID and author ID link, because we'll get the rest of the general details from
        // there.
        String authorIdLink = findAuthorIdLink(infoDoc);
        // Get the HTML at the author URL.
        Document doc = Util.downloadHtml(String.format(C.SIYE_AUTHOR_URL, authorIdLink));
        if (doc == null) throw new IOException(String.format(C.STORY_DL_FAILED, SiyeDL.SITE, storyId));
        // Get the story row from on the author's page.
        Element storyRow = doc.select(String.format("td tr td:has(a[href=\"viewstory.php?sid=%s\"])", storyId)).last();
        // Get title.
        title = storyRow.select(String.format("a[href=\"viewstory.php?sid=%s\"]", storyId)).first().html();
        // Get author.
        author = storyRow.select(String.format("a[href=\"%s\"]", authorIdLink)).first().text();
        // Due to SIYE's *incredibly* crappy HTML, we need to check to see if the story Element we currently have
        // actually has the rest of the parts we need or not. If the story is part of a series, the "row" on SIYE's
        // site is actually split across *two* <tr>s rather than being contained within one.
        if (storyRow.select("div").first() == null) {
            // This story must be part of a series, which means that that the <td> we have doesn't have the rest of
            // the info we need. Starting from the <td> we currently have, we need to go up to the parent <tr>, then
            // over to the next immediate sibling <tr> from the parent <tr>, and then into that sibling <tr>'s last
            // <td> child...yes, this *is* complicated sounding and SIYE *should* have properly structured HTML. Ugh.
            storyRow = storyRow.parent().nextElementSibling().children().last();
        }
        // Get details strings to get other stuff.
        String[] details = storyRow.select("div").first().text().replace("Completed:", "- Completed:").split(" - ");
        // Get rating.
        rating = details[0].trim();
        // Get fic type (category).
        ficType = details[1].trim();
        // Get genre(s).
        genres = details[2].trim();
        // Get warnings.
        warnings = details[3].replace("Warnings: ", "").trim();
        // Get word count.
        wordCount = Integer.parseInt(details[4].replace("Words: ", "").trim());
        // Get status.
        status = details[5].replace("Completed: ", "").trim().equals("Yes") ? C.STAT_C : C.STAT_I;
        // Get chapter count to generate chapter URLs.
        int chapterCount = Integer.parseInt(details[6].replace("Chapters: ", "").trim());
        for (int i = 0; i < chapterCount; i++) chapterUrls.add(String.format(C.SIYE_CHAP_URL, storyId, i + 1));
        // Get date published.
        datePublished = details[7].replace("Published: ", "").trim();
        // Get date last updated.
        dateUpdated = details[8].replace("Updated: ", "").trim();
    }

    /**
     * Get the info page for our story, which in SIYE's case is the normal version of Chapter 1 (though we actually also
     * pull info from both the author page and the printed version of the chapters).
     * @param url Story URL, may not be normalized.
     * @return Chapter 1 HTML Document.
     */
    private Document getInfoPage(String url) throws IOException {
        // Need to normalize this URL first to be sure we can get the author ID link.
        // Start by getting the story ID from the URL.
        Matcher idMatcher = Pattern.compile(C.SIYE_SID_REGEX).matcher(url);
        idMatcher.find();
        storyId = idMatcher.group(1);
        // Now download the first chapter's HTML.
        Document chDoc = Util.downloadHtml(String.format(C.SIYE_INFO_URL, storyId));
        if (chDoc == null) throw new IOException(String.format(C.STORY_DL_FAILED, SiyeDL.SITE, storyId));
        return chDoc;
    }

    /**
     * Use a valid SIYE story/chapter URL (not the printable version!) to find the story's Author ID link.
     * @param chDoc Story's chapter 1 HTML.
     * @return Author ID link (looks like "viewuser.php?uid=[authorId]").
     */
    private String findAuthorIdLink(Document chDoc) throws IOException {
        // Then find the element that lets us get the relative link to the author's page.
        Element aIdElement = chDoc.select("h3 a").first();
        // Throw an exception if we couldn't find the link to the author's page, as it likely means that the URL
        // format was valid but that it doesn't point to a real story/chapter on SIYE.
        if (aIdElement == null) throw new IOException(String.format(C.STORY_DL_FAILED, SiyeDL.SITE, storyId));
        // Now return the author page URL.
        return aIdElement.attr("href");
    }
}
