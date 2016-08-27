package bkromhout.fdl.stories;

import bkromhout.fdl.ex.InitStoryException;
import bkromhout.fdl.parsing.StoryEntry;
import bkromhout.fdl.site.Sites;
import bkromhout.fdl.util.C;
import bkromhout.fdl.util.Util;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;

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
     * Create a new {@link SiyeStory}.
     * @param storyEntry Story entry with details from the input file.
     * @throws InitStoryException if we can't create this story object for some reason.
     */
    public SiyeStory(StoryEntry storyEntry) throws InitStoryException {
        super(storyEntry, Sites.SIYE());
    }

    @Override
    protected void populateInfo() throws InitStoryException {
        // Get story ID and use it to normalize the url, then download the url so that we can parse story info.
        storyId = parseStoryId(url, "sid=(\\d*)", 1);
        Document infoDoc = Util.getHtml(String.format(SIYE_C_URL, storyId, 1));
        // By some magic unbeknown to me, SIYE will give us what "looks like" a valid story page even if it's an
        // invalid story link. That is, it doesn't contain the div.warning element like it does if you were to visit
        // the same link in a web browser. Due to this, we have to check the title to see if it's empty in order to
        // determine if the story link is invalid or not... Just another day in the life of an SIYE site parser...
        if (infoDoc == null || infoDoc.title().isEmpty())
            throw new InitStoryException(C.STORY_DL_FAILED, site.getName(), storyId);

        // Get story info element from story page to get some of the details.
        Element storyInfoElem = infoDoc.select("td[align=\"left\"][valign=\"top\"]").last();
        if (storyInfoElem == null) throw new InitStoryException(C.STORY_DL_FAILED, site.getName(), storyId);

        // Get summary.
        if (hasDetailTag(C.J_SUMMARY))
            summary = detailTags.get(C.J_SUMMARY);
        else {
            int summaryStartIdx = storyInfoElem.select("b:contains(Summary:)").first().siblingIndex() + 1;
            int summaryEndIdx = storyInfoElem.select("b:contains(Hitcount:)").first().siblingIndex();
            summary = Util.cleanHtmlString(
                    Util.divFromChildCopies(storyInfoElem, summaryStartIdx, summaryEndIdx).html().trim());
        }

        // Get characters.
        if (hasDetailTag(C.J_CHARACTERS))
            characters = detailTags.get(C.J_CHARACTERS);
        else {
            Element charsLabel = storyInfoElem.select("b:contains(Characters:)").first();
            characters = ((TextNode) storyInfoElem.childNodes().get(charsLabel.siblingIndex() + 1)).text().trim();
        }

        // Figure out the SIYE story ID and author ID link, because we'll get the rest of the general details from
        // the story entry on the author's page after downloading it.
        String authorIdLink = findAuthorIdLink(infoDoc);
        Document doc = Util.getHtml(String.format(SIYE_A_URL, authorIdLink));
        if (doc == null) throw new InitStoryException(C.STORY_DL_FAILED, site.getName(), storyId);
        // Get the story entry on the author's page.
        Element storyRow = doc.select(String.format("td tr td:has(a[href=\"viewstory.php?sid=%s\"])", storyId)).last();

        title = hasDetailTag(C.J_TITLE) ? detailTags.get(C.J_TITLE)
                : storyRow.select(String.format("a[href=\"viewstory.php?sid=%s\"]", storyId)).first().html();

        author = hasDetailTag(C.J_AUTHOR) ? detailTags.get(C.J_AUTHOR)
                : storyRow.select(String.format("a[href=\"%s\"]", authorIdLink)).first().text();

        /* Due to SIYE's *incredibly* crappy HTML, we need to check to see if the story Element we currently have
        actually has the rest of the parts we need or not. If the story is part of a series, the "row" on SIYE's
        site is actually split across *two* <tr>s rather than being contained within one. */
        if (storyRow.select("div").first() == null) {
            /* This story must be part of a series, which means that that the <td> we have doesn't have the rest of
            the info we need. Starting from the <td> we currently have, we need to go up to the parent <tr>, then
            over to the next immediate sibling <tr> from the parent <tr>, and then into that sibling <tr>'s last
            <td> child...yes, this *is* complicated sounding and SIYE *should* have properly structured HTML. Ugh. */
            // Reassign storyRow so that we can keep getting the story details.
            storyRow = storyRow.parent().nextElementSibling().children().last();
        }

        // Get details strings to parse the other details from.
        String[] details = storyRow.select("div").first().text().replace("Completed:", "- Completed:").split(" - ");

        rating = hasDetailTag(C.J_RATING) ? detailTags.get(C.J_RATING) : details[0].trim();
        ficType = hasDetailTag(C.J_FIC_TYPE) ? detailTags.get(C.J_FIC_TYPE) : details[1].trim(); // SIYE category.
        genres = hasDetailTag(C.J_GENRES) ? detailTags.get(C.J_GENRES) : details[2].trim();
        warnings = hasDetailTag(C.J_WARNINGS) ? detailTags.get(C.J_WARNINGS)
                : details[3].replace("Warnings: ", "").trim();
        wordCount = Integer.parseInt(details[4].replace("Words: ", "").trim());
        status = details[5].replace("Completed: ", "").trim().equals("Yes") ? C.STAT_C : C.STAT_I;
        int chapCount = Integer.parseInt(details[6].replace("Chapters: ", "").trim());
        datePublished = details[7].replace("Published: ", "").trim();
        dateUpdated = details[8].replace("Updated: ", "").trim();

        // Detail tags which the site doesn't support.
        if (hasDetailTag(C.J_SERIES)) series = detailTags.get(C.J_SERIES);

        // Generate chapter urls.
        for (int i = 0; i < chapCount; i++) chapterUrls.add(String.format(SIYE_C_URL, storyId, i + 1));
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
        if (aIdElement == null) throw new InitStoryException(C.STORY_DL_FAILED, site.getName(), storyId);
        // Now return the author page url.
        return aIdElement.attr("href");
    }
}
