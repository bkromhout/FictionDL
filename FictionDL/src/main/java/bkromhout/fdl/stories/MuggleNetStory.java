package bkromhout.fdl.stories;

import bkromhout.fdl.ex.InitStoryException;
import bkromhout.fdl.parsing.StoryEntry;
import bkromhout.fdl.site.Sites;
import bkromhout.fdl.util.C;
import bkromhout.fdl.util.Util;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Model object for a <a href="http://fanfiction.mugglenet.com">MuggleNet</a> story.
 */
public class MuggleNetStory extends Story {
    /**
     * MuggleNet chapter link template, needs story ID and chapter number and warning bypass part substituted into it.
     */
    private static final String MN_C_URL = "http://fanfiction.mugglenet.com/viewstory.php?sid=%s&chapter=%d%s";
    /**
     * MuggleNet story info link template, just needs story ID and warning bypass part substituted into it.
     */
    private static final String MN_S_URL = "http://fanfiction.mugglenet.com/viewstory.php?sid=%s%s";
    /**
     * MuggleNet url fragment, adds a warning bypass for "Professors"-rated stories/chapters.
     */
    private static final String MN_PART_WARN_3 = "&warning=3";
    /**
     * MuggleNet url fragment, adds a warning bypass for "6th-7th Year"-rated stories/chapters.
     */
    private static final String MN_PART_WARN_5 = "&warning=5";
    /**
     * Message displayed in place of a Professors-rated story/chapter if logged in, but the warning integer isn't 3.
     */
    private static final String MN_NEEDS_WARN_3 = "This fic may contain language or imagery unsuitable for persons " +
            "under the age of 17. You must be logged in to read this fic.";
    /**
     * Message displayed in place of a "6th-7th Years"-rated story/chapter if the warning integer isn't 3.
     */
    private static final String MN_NEEDS_WARN_5 = "This story may contain some sexuality, violence and or profanity " +
            "not suitable for younger readers.";

    /**
     * Create a new {@link MuggleNetStory}.
     * @param storyEntry Story entry with details from the input file.
     * @throws InitStoryException if we can't create this story object for some reason.
     */
    public MuggleNetStory(StoryEntry storyEntry) throws InitStoryException {
        super(storyEntry, Sites.MN());
    }

    @Override
    protected void populateInfo() throws InitStoryException {
        // Set warning bypass to empty initially.
        String warnBypass = "";
        // Get story ID and use it to normalize the url, then download the url so that we can parse story info.
        storyId = parseStoryId(url, "sid=(\\d*)", 1);
        url = String.format(MN_S_URL, storyId, warnBypass);
        Document infoDoc = Util.getHtml(url);
        if (infoDoc == null) throw new InitStoryException(C.STORY_DL_FAILED, site.getName(), storyId);

        // Figure out if we need to change the warning bypass or possible just give up.
        Element errorElem = infoDoc.select("div.errorText").first();
        if (errorElem != null) {
            String errorText = errorElem.ownText().trim();
            switch (errorText) {
                case "Access denied. This story has not been validated by the adminstrators of this site.":
                    // Story is unavailable. ("adminstrators" is a typo on the actual site)
                    throw new InitStoryException(C.STORY_DL_FAILED, site.getName(), storyId);
                case MN_NEEDS_WARN_3:
                    // We're logged in, but need to change our warning bypass to 3 for this story and retry.
                    warnBypass = MN_PART_WARN_3;
                    url = String.format(MN_S_URL, storyId, warnBypass);
                    infoDoc = Util.getHtml(url);
                    if (infoDoc == null) throw new InitStoryException(C.STORY_DL_FAILED, site.getName(), storyId);
                    break;
                case MN_NEEDS_WARN_5:
                    // We're logged in, but need to change our warning bypass to 5 for this story and retry.
                    warnBypass = MN_PART_WARN_5;
                    url = String.format(MN_S_URL, storyId, warnBypass);
                    infoDoc = Util.getHtml(url);
                    if (infoDoc == null) throw new InitStoryException(C.STORY_DL_FAILED, site.getName(), storyId);
                    break;
                case "Registered Users Only":
                    // We're not logged in in the first place, so we cannot download this story.
                    throw new InitStoryException(C.MUST_LOGIN, site.getName(), storyId);
                default:
                    // Some site error we haven't accounted for, most likely.
                    throw new InitStoryException(C.UNEXP_SITE_ERR, site.getName(), errorText);
            }
        }

        // Get the element that has the title and author of the story in it, then parse the title and author from it.
        Elements taElem = infoDoc.select("h1>a");
        if (taElem == null) throw new InitStoryException(C.STORY_DL_FAILED, site.getName(), storyId);
        title = hasDetailTag(C.J_TITLE) ? detailTags.get(C.J_TITLE)
                : taElem.first().html().trim();
        author = hasDetailTag(C.J_AUTHOR) ? detailTags.get(C.J_AUTHOR)
                : taElem.last().html().trim();

        // Get the element that has the other details in it, and a list of the detail label elements to help parse
        // the details.
        Element details = infoDoc.select("div.listbox").first();
        if (details == null) throw new InitStoryException(C.STORY_DL_FAILED, site.getName(), storyId);
        Elements labels = details.select("b,span.label");

        summary = hasDetailTag(C.J_SUMMARY) ? detailTags.get(C.J_SUMMARY)
                : Util.cleanHtmlString(makeDetailDivForLabel(details, labels, "Summary", "Rating").html().trim());

        if (hasDetailTag(C.J_RATING))
            rating = detailTags.get(C.J_RATING);
        else {
            rating = makeDetailDivForLabel(details, labels, "Rating", "Category").text().trim();
            rating = rating.substring(0, rating.indexOf('[')).trim(); // Strip the reviews count from the rating text.
        }

        ficType = hasDetailTag(C.J_FIC_TYPE) ? detailTags.get(C.J_FIC_TYPE)
                : makeDetailDivForLabel(details, labels, "Category", "Characters").text().trim(); // MN category.

        if (hasDetailTag(C.J_CHARACTERS))
            characters = detailTags.get(C.J_CHARACTERS);
        else {
            String temp = makeDetailDivForLabel(details, labels, "Characters", "Warnings").text().trim();
            characters = temp.equals("None") ? null : temp;
        }

        if (hasDetailTag(C.J_WARNINGS))
            warnings = detailTags.get(C.J_WARNINGS);
        else {
            String temp = makeDetailDivForLabel(details, labels, "Warnings", "Challenge").text().trim();
            warnings = temp.equals("None") ? null : temp;
        }

        if (hasDetailTag(C.J_SERIES))
            series = detailTags.get(C.J_SERIES);
        else {
            String temp = makeDetailDivForLabel(details, labels, "Serie", "Chapters").text().trim(); // Yes, "Serie".
            series = temp.equals("None") ? null : temp;
        }

        String temp = makeDetailDivForLabel(details, labels, "Completed", "Wordcount").text().trim();
        status = temp.equals("Yes") ? C.STAT_C : C.STAT_I;

        int chapCount = Integer.parseInt(
                makeDetailDivForLabel(details, labels, "Chapters", "Completed?").text().trim());
        wordCount = Integer.parseInt(makeDetailDivForLabel(details, labels, "Wordcount", "Viewcount").text().trim());
        datePublished = makeDetailDivForLabel(details, labels, "Published", "Updated").text().trim();
        dateUpdated = makeDetailDivForLabel(details, labels, "Updated", null).text().trim();

        // Detail tags which the site doesn't support.
        if (hasDetailTag(C.J_GENRES)) genres = detailTags.get(C.J_GENRES);

        // Generate chapter urls.
        for (int i = 0; i < chapCount; i++) chapterUrls.add(String.format(MN_C_URL, storyId, i + 1, warnBypass));
    }

    /**
     * Return a new div element which contains the child nodes from the parent element which are between two label
     * elements.
     * @param parent             Element to copy nodes from.
     * @param labels             List of label elements from the parent.
     * @param startLabelContains Substring of label which precedes the elements we're interested in.
     * @param endLabelContains   Substring of label which follows the elements we're interested in (can be null).
     * @return A new div element, or null if the parameters passed aren't valid.
     */
    private Element makeDetailDivForLabel(Element parent, Elements labels, String startLabelContains,
                                          String endLabelContains) {
        // Parameter checks.
        if (parent == null || startLabelContains == null || startLabelContains.isEmpty()) return null;
        // Sometimes MuggleNet wraps the details in a <p> tag, which we don't really want, so be sure to handle that.
        if (parent.childNodes().size() < 15) parent.children().last().unwrap();
        // Figure out start and end indices for copying nodes from parent based on the index of labels[labelIdx] in
        // the parent.
        // Start index should be one higher than the index of the label in the parent (we don't need the actual label).
        int startLabelIdx = findLabelIdx(labels, startLabelContains);
        int endLabelIdx = endLabelContains != null ? findLabelIdx(labels, endLabelContains) : -1;
        if (startLabelIdx == -1) return null;
        int startIdx = parent.childNodes().indexOf(labels.get(startLabelIdx)) + 1;
        int endIdx = endLabelIdx != -1
                // If we have an endLabelIdx, then this should be the index of that element in the parent.
                ? parent.childNodes().indexOf(labels.get(endLabelIdx))
                // If we don't, and startLabelIdx isn't the last one, use the next one.
                : (startLabelIdx != labels.size() - 1
                ? parent.childNodes().indexOf(labels.get(startLabelIdx + 1))
                //Otherwise, the end index should be equal to the capacity of the parent's list of child nodes.
                : parent.childNodes().size());
        // Return the new div.
        return Util.divFromChildCopies(parent, startIdx, endIdx);
    }

    /**
     * Given a list of label elements, return the index of the one which contains {@code labelNameContains}.
     * @param labels            List of label elements.
     * @param labelNameContains Label name substring to look for.
     * @return Index of label with substring, or -1 if not found or {@code labelNameContains} is {@code null} or the
     * empty string.
     */
    private int findLabelIdx(Elements labels, String labelNameContains) {
        if (labels == null || labelNameContains == null || labelNameContains.isEmpty()) return -1;
        for (int i = 0; i < labels.size(); i++)
            if (labels.get(i).text().trim().contains(labelNameContains)) return i;
        return -1;
    }
}
