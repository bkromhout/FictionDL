package bkromhout.fdl.storys;

import bkromhout.fdl.downloaders.ParsingDL;
import bkromhout.fdl.ex.InitStoryException;
import bkromhout.fdl.util.C;
import bkromhout.fdl.util.Sites;
import bkromhout.fdl.util.Util;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.parser.Tag;

import java.util.List;

/**
 * Model object for a <a href="http://siye.co.uk">Sink Into Your Eyes</a> story.
 */
public class SiyeStory extends Story {
    /**
     * SIYE story chapter link template, just needs the story ID string and chapter number substituted into it.
     */
    private static final String SIYE_C_URL = "http://siye.co.uk/viewstory.php?sid=%s&chapter=%d";
    /**
     * SIYE author page link template, just needs relative author link string substituted into it.
     */
    private static final String SIYE_A_URL = "http://siye.co.uk/%s";

    /**
     * Create a new {@link SiyeStory} based off of a url.
     * @param ownerDl The parsing downloader which owns this story.
     * @param url     url of the story this model represents.
     * @throws InitStoryException if we can't create this story object for some reason.
     */
    public SiyeStory(ParsingDL ownerDl, String url) throws InitStoryException {
        super(ownerDl, url, Sites.SIYE());
    }

    @Override
    protected void populateInfo() throws InitStoryException {
        // Get SIYE story ID.
        storyId = parseStoryId(url, "sid=(\\d*)", 1);
        // Get chapter 1 HTML first.
        Document infoDoc = getInfoPage();
        Element storyInfoElem = infoDoc.select("td[align=\"left\"][valign=\"top\"]").last();
        if (storyInfoElem == null) throw initEx();
        // Get summary.
        int summaryStartIdx = storyInfoElem.select("b:contains(Summary:)").first().siblingIndex() + 1;
        int summaryEndIdx = storyInfoElem.select("b:contains(Hitcount:)").first().siblingIndex();
        summary = Util.cleanHtmlString(parseSummary(storyInfoElem, summaryStartIdx, summaryEndIdx));
        // Get characters.
        Element charsLabel = storyInfoElem.select("b:contains(Characters:)").first();
        characters = ((TextNode) storyInfoElem.childNodes().get(charsLabel.siblingIndex() + 1)).text().trim();
        // Figure out the SIYE story ID and author ID link, because we'll get the rest of the general details from
        // there.
        String authorIdLink = findAuthorIdLink(infoDoc);
        // Get the HTML at the author url.
        Document doc = Util.downloadHtml(String.format(SIYE_A_URL, authorIdLink));
        if (doc == null) throw initEx();
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
        // Get genres.
        genres = details[2].trim();
        // Get warnings.
        warnings = details[3].replace("Warnings: ", "").trim();
        // Get word count.
        wordCount = Integer.parseInt(details[4].replace("Words: ", "").trim());
        // Get status.
        status = details[5].replace("Completed: ", "").trim().equals("Yes") ? C.STAT_C : C.STAT_I;
        // Get chapter count to generate chapter urls.
        int chapCount = Integer.parseInt(details[6].replace("Chapters: ", "").trim());
        // Get date published.
        datePublished = details[7].replace("Published: ", "").trim();
        // Get date last updated.
        dateUpdated = details[8].replace("Updated: ", "").trim();
        // Generate chapter urls.
        for (int i = 0; i < chapCount; i++) chapterUrls.add(String.format(SIYE_C_URL, storyId, i + 1));
    }

    /**
     * Get the info page for this story, which in SIYE's case is Chapter 1.
     * @return Chapter 1 HTML Document.
     */
    private Document getInfoPage() throws InitStoryException {
        // Download the first chapter's HTML.
        Document chDoc = Util.downloadHtml(String.format(SIYE_C_URL, storyId, 1));
        if (chDoc == null) throw initEx();
        return chDoc;
    }

    /**
     * Use a valid SIYE story/chapter url to find the story's Author ID link.
     * @param chDoc Story's chapter 1 HTML.
     * @return Author ID link (looks like "viewuser.php?uid=[authorId]").
     */
    private String findAuthorIdLink(Document chDoc) throws InitStoryException {
        // Then find the element that lets us get the relative link to the author's page.
        Element aIdElement = chDoc.select("h3 a").first();
        // Throw an exception if we couldn't find the link to the author's page, as it likely means that the url
        // format was valid but that it doesn't point to a real story/chapter on SIYE.
        if (aIdElement == null) throw initEx();
        // Now return the author page url.
        return aIdElement.attr("href");
    }

    /**
     * Get the story summary from the story details element based on indices of child nodes.
     * @param parent   Element to copy nodes from.
     * @param startIdx Index to start copying nodes from (inclusive).
     * @param endIdx   Index to copy nodes to (exclusive).
     * @return Summary HTML string.
     */
    private String parseSummary(Element parent, int startIdx, int endIdx) {
        // Parameter checks.
        if (parent == null || startIdx < 0 || startIdx >= parent.childNodes().size() || endIdx <= startIdx ||
                endIdx > parent.childNodes().size()) return null;
        // Copy parent's child nodes.
        List<Node> nodeCopies = parent.childNodesCopy();
        // Create the new div.
        Element summary = new Element(Tag.valueOf("div"), "");
        // Loop through the copied nodes, starting at the startIdx and up to but not including the endIdx, and append
        // those nodes to the new div.
        for (int i = startIdx; i < endIdx; i++) summary.appendChild(nodeCopies.get(i));
        // Return the summary HTML.
        return summary.html().trim();
    }
}
