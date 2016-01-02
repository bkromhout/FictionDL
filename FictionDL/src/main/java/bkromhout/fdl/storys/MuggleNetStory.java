package bkromhout.fdl.storys;

import bkromhout.fdl.ex.InitStoryException;
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
     * MuggleNet url fragment, adds a warning bypass for "6th-7th Year"-rated stories/chapters.
     */
    private static final String MN_PART_WARN_5 = "&warning=5";
    /**
     * MuggleNet url fragment, adds a warning bypass for "Professors"-rated stories/chapters.
     */
    private static final String MN_PART_WARN_3 = "&warning=3";
    /**
     * Error message displayed by MuggleNet when attempting to access a "Professors" rated story while not logged in.
     */
    static final String MN_REG_USERS_ONLY = "Registered Users Only";
    /**
     * Message displayed in place of a "6th-7th Years"-rated story/chapter if the warning integer isn't 3.
     */
    private static final String MN_NEEDS_WARN_5 = "This story may contain some sexuality, violence and or profanity " +
            "not suitable for younger readers.";
    /**
     * Message displayed in place of a Professors-rated story/chapter if logged in, but the warning integer isn't 3.
     */
    private static final String MN_NEEDS_WARN_3 = "This fic may contain language or imagery unsuitable for persons " +
            "under the age of 17. You must be logged in to read this fic.";

    /**
     * Create a new {@link MuggleNetStory} based off of a url.
     * @param url url of the story this model represents.
     * @throws InitStoryException if we can't create this story object for some reason.
     */
    public MuggleNetStory(String url) throws InitStoryException {
        super(url, Sites.MN());
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

        // Figure out if we need to change the warning bypass.
        Element errorText = infoDoc.select("div.errorText").first();
        if (errorText != null && errorText.ownText().trim().equals(MN_NEEDS_WARN_3)) {
            // We're logged in, but need to change our warning bypass to 3 for this story and re-download the info page.
            warnBypass = MN_PART_WARN_3;
            url = String.format(MN_S_URL, storyId, warnBypass);
            infoDoc = Util.getHtml(url);
            if (infoDoc == null) throw new InitStoryException(C.STORY_DL_FAILED, site.getName(), storyId);
        } else if (errorText != null && errorText.ownText().trim().equals(MN_NEEDS_WARN_5)) {
            // We're logged in, but need to change our warning bypass to 5 for this story and re-download the info page.
            warnBypass = MN_PART_WARN_5;
            url = String.format(MN_S_URL, storyId, warnBypass);
            infoDoc = Util.getHtml(url);
            if (infoDoc == null) throw new InitStoryException(C.STORY_DL_FAILED, site.getName(), storyId);
        } else if (errorText != null && errorText.ownText().trim().equals(MN_REG_USERS_ONLY)) {
            // We're not logged in in the first place, so we cannot download this story.
            throw new InitStoryException(C.MUST_LOGIN, site.getName(), storyId);
        } else if (errorText != null) {
            // Some site error we haven't accounted for, most likely.
            throw new InitStoryException(C.UNEXP_SITE_ERR, site.getName(), errorText.ownText().trim());
        }

        // Get the element that has the title and author of the story in it, then parse the title and author from it.
        Elements taElem = infoDoc.select("div#pagetitle a");
        if (taElem == null) throw new InitStoryException(C.STORY_DL_FAILED, site.getName(), storyId);
        title = taElem.first().html().trim();
        author = taElem.last().html().trim();

        // Get the element that has the other details in it, and a list of the detail label elements to help parse
        // the details.
        Element details = infoDoc.select("div.content").first();
        if (details == null) throw new InitStoryException(C.STORY_DL_FAILED, site.getName(), storyId);
        Elements labels = details.select("span.label");

        summary = Util.cleanHtmlString(makeDetailDivForLabel(details, labels, 0).html().trim());
        rating = makeDetailDivForLabel(details, labels, 1).text().trim();
        ficType = makeDetailDivForLabel(details, labels, 2).text().trim(); // MN category.

        String temp = makeDetailDivForLabel(details, labels, 3).text().trim();
        characters = temp.equals("None") ? null : temp;

        temp = makeDetailDivForLabel(details, labels, 4).text().trim();
        warnings = temp.equals("None") ? null : temp;

        temp = makeDetailDivForLabel(details, labels, 6).text().trim();
        series = temp.equals("None") ? null : temp;

        temp = makeDetailDivForLabel(details, labels, 8).text().trim();
        status = temp.equals("Yes") ? C.STAT_C : C.STAT_I;

        int chapCount = Integer.parseInt(makeDetailDivForLabel(details, labels, 7).text().trim());
        wordCount = Integer.parseInt(makeDetailDivForLabel(details, labels, 9).text().trim());
        datePublished = makeDetailDivForLabel(details, labels, 11).text().trim();
        dateUpdated = makeDetailDivForLabel(details, labels, 12).text().trim();

        // Generate chapter urls.
        for (int i = 0; i < chapCount; i++) chapterUrls.add(String.format(MN_C_URL, storyId, i + 1, warnBypass));
    }

    /**
     * Return a new div element which contains the child nodes from the parent element that corresponds to a particular
     * label element.
     * @param parent   Element to copy nodes from.
     * @param labels   List of label elements from the parent.
     * @param labelIdx Index of label (in labels parameter, not parent!!) which we want to make a div for.
     * @return A new div element, or null if the parameters passed aren't valid.
     */
    private Element makeDetailDivForLabel(Element parent, Elements labels, int labelIdx) {
        // Parameter checks.
        if (parent == null || labelIdx < 0 || labelIdx >= labels.size()) return null;
        // Sometimes MuggleNet wraps the details in a <p> tag, which we don't really want, so be sure to handle that.
        Element realParent = parent.childNodes().size() != 2 ? parent : parent.child(1);
        // Figure out start and end indices for copying nodes from parent based on the index of labels[labelIdx] in
        // the parent.
        // Start index should be one higher than the index of the label in the parent (we don't need the actual label).
        int startIdx = realParent.childNodes().indexOf(labels.get(labelIdx)) + 1;
        int endIdx = labelIdx != labels.size() - 1
                // If labelIdx *doesn't* point to the last label in the labels list, the end index should be the
                // index (in the parent's list of child nodes) of the next label after the one pointed to by labelIdx.
                ? realParent.childNodes().indexOf(labels.get(labelIdx + 1))
                // Otherwise, the end index should be equal to the capacity of the parent's list of child nodes.
                : realParent.childNodes().size();
        // Return the new div.
        return Util.divFromChildCopies(realParent, startIdx, endIdx);
    }
}
