package bkromhout.fdl.stories;

import bkromhout.fdl.ex.InitStoryException;
import bkromhout.fdl.parsing.StoryEntry;
import bkromhout.fdl.site.Sites;
import bkromhout.fdl.util.C;
import bkromhout.fdl.util.ImageHelper;
import bkromhout.fdl.util.Util;
import nl.siegmann.epublib.domain.Resource;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.util.List;

/**
 * Model object for a <a href="http://www.harrypotterfanfiction.com">Harry Potter FanFiction</a> story.
 */
public class HpffStory extends Story {
    /**
     * HPFF chapter link template, needs chapter number substituted into it.
     */
    private static final String HPFF_C_URL = "http://www.harrypotterfanfiction.com/viewstory2.php?chapterid=%s";
    /**
     * HPFF story info link template, just needs story ID substituted into it.
     */
    private static final String HPFF_S_URL = "http://www.harrypotterfanfiction.com/viewstory.php?psid=%s";

    /**
     * Create a new {@link HpffStory}.
     * @param storyEntry Story entry with details from the input file.
     * @throws InitStoryException if we can't create this story object for some reason.
     */
    public HpffStory(StoryEntry storyEntry) throws InitStoryException {
        super(storyEntry, Sites.HPFF());
    }

    @Override
    protected void populateInfo() throws InitStoryException {
        // Get story ID and create story URL, then download page.
        storyId = parseStoryId(url, "psid=(\\d*)", 1);
        url = String.format(HPFF_S_URL, storyId);
        Document infoDoc = Util.getHtml(url);
        if (infoDoc == null) throw new InitStoryException(C.STORY_DL_FAILED, site.getName(), storyId);

        // Ensure valid story.
        if (infoDoc.select("div#mainpage2").text().contains("ERROR locating story meta"))
            throw new InitStoryException(C.STORY_DL_FAILED, site.getName(), storyId);

        // Get title and author elements.
        Elements taElements = infoDoc.select("center:has(a[href^=viewuser]) > a");
        title = hasDetailTag(C.J_TITLE) ? detailTags.get(C.J_TITLE) : taElements.get(0).ownText().trim();
        author = hasDetailTag(C.J_AUTHOR) ? detailTags.get(C.J_AUTHOR) : taElements.get(1).ownText().trim();

        // Get details table, then use to get details.
        Element detailsTable = infoDoc.select("table.storymaininfo").first();

        rating = hasDetailTag(C.J_RATING) ? detailTags.get(C.J_RATING)
                : getDetailContentFromLabel(detailsTable, "Rating");

        wordCount = Integer.parseInt(getDetailContentFromLabel(detailsTable, "Words"));

        if (hasDetailTag(C.J_CHARACTERS))
            characters = detailTags.get(C.J_CHARACTERS);
        else {
            // Include both characters and pairings.
            String c = getDetailContentFromLabel(detailsTable, "Characters");
            String p = getDetailContentFromLabel(detailsTable, "Pairings");
            characters = p == null ? c : c + "; " + p;
        }

        genres = hasDetailTag(C.J_GENRES) ? detailTags.get(C.J_GENRES)
                : getDetailContentFromLabel(detailsTable, "Genre(s)");

        // "Era" for HPFF.
        ficType = hasDetailTag(C.J_FIC_TYPE) ? detailTags.get(C.J_FIC_TYPE)
                : getDetailContentFromLabel(detailsTable, "Era");

        warnings = hasDetailTag(C.J_WARNINGS) ? detailTags.get(C.J_WARNINGS)
                : getDetailContentFromLabel(detailsTable, "Advisory");

        String temp = getDetailContentFromLabel(detailsTable, "Status");
        status = temp.equals("Completed") ? C.STAT_C : C.STAT_I;

        datePublished = getDetailContentFromLabel(detailsTable, "First Published");
        dateUpdated = getDetailContentFromLabel(detailsTable, "Last Updated");

        // Get summary.
        if (hasDetailTag(C.J_SUMMARY))
            summary = detailTags.get(C.J_SUMMARY);
        else {
            // Make sure that we take care of any banner images in the summary.
            Element summaryElem = infoDoc.select("table.storysummary td").first();
            String baseResourceName = "summary_img_";
            imageResources.addAll(new ImageHelper(summaryElem, baseResourceName).getImageResources());
            summary = Util.cleanHtmlString(summaryElem.html().trim());
        }

        // Detail tags which the site doesn't support.
        if (hasDetailTag(C.J_SERIES)) series = detailTags.get(C.J_SERIES);

        // Generate chapter URLs and save chapter names.
        Elements chapRows = infoDoc.select("table.text tr[id]");
        for (int i = 0; i < chapRows.size(); i++) {
            Element chapRow = chapRows.get(i);
            // Generate chapter URL.
            chapterUrls.add(String.format(HPFF_C_URL, chapRow.id()));
            // Save chapter name.
            String chapName = chapRow.select("b").first().text().trim();
            detailTags.put(String.format(C.CHAP_TITLE_KEY_TEMPLATE, i + 1), chapName);
        }
    }

    /**
     * Returns the string content for a detail based on its label.
     * @param parent        A parent (might be indirect) of the label and detail nodes.
     * @param labelContains String contained in the label we want the detail content for.
     * @return Detail content for label.
     */
    private String getDetailContentFromLabel(Element parent, String labelContains) {
        if (labelContains == null || labelContains.isEmpty()) return null;
        // Find label element.
        Element label = parent.select("b:contains(" + labelContains + ")").first();
        if (label == null) return null;
        // Get the text node from the parent's list of child nodes based on the index of the label node.
        Node detail = label.nextSibling();
        if (!(detail instanceof TextNode)) return null;
        return ((TextNode) detail).text().trim();
    }
}
