package bkromhout.FictionDL.Story;

import bkromhout.FictionDL.C;
import bkromhout.FictionDL.Downloader.ParsingDL;
import bkromhout.FictionDL.Util;
import bkromhout.FictionDL.ex.InitStoryException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;

import java.util.ArrayList;

/**
 * Model object for a MuggleNet story.
 */
public class MuggleNetStory extends Story {

    /**
     * Create a new MuggleNetStory object based off of a URL.
     * @param ownerDl The parsing downloader which owns this story.
     * @param url     URL of the story this model represents.
     * @throws InitStoryException if we can't create this story object for some reason.
     */
    public MuggleNetStory(ParsingDL ownerDl, String url) throws InitStoryException {
        super(ownerDl, url);
    }

    @Override
    protected void populateInfo() throws InitStoryException {
        String warnBypass = "";
        // Set site.
        hostSite = C.HOST_MN;
        // Get story ID first.
        storyId = parseStoryId(url, C.MN_SID_REGEX, 1);
        // Normalize the URL, since there are many valid MN URL formats.
        url = String.format(C.MN_S_URL, storyId, warnBypass);
        // Get the story page in order to parse the story info.
        Document infoDoc = Util.downloadHtml(url, ownerDl.getCookies());
        // Make sure that we got a Document, that this is a valid story, and that we don't need to login.
        if (infoDoc == null) throw initEx();
        Element errorText = infoDoc.select("div.errorText").first();
        // Figure out if we need to change the warning bypass.
        if (errorText != null && errorText.ownText().trim().equals(C.MN_NEEDS_WARN_3)) {
            // We're logged in, but need to change our warning bypass to 3 for this story.
            warnBypass = C.MN_PART_WARN_3;
            // Now get the story page again.
            url = String.format(C.MN_S_URL, storyId, warnBypass);
            infoDoc = Util.downloadHtml(url, ownerDl.getCookies());
        } else if (errorText != null && errorText.ownText().trim().equals(C.MN_NEEDS_WARN_5)) {
            // We're logged in, but need to change our warning bypass to 5 for this story.
            warnBypass = C.MN_PART_WARN_5;
            // Now get the story page again.
            url = String.format(C.MN_S_URL, storyId, warnBypass);
            infoDoc = Util.downloadHtml(url, ownerDl.getCookies());
        } else if (errorText != null) throw initEx(errorText.ownText().trim()); // Just throw some exception
        // Get the element that has the title and author of the story in it.
        Elements taElem = infoDoc.select("div#pagetitle a");
        if (taElem == null) throw initEx();
        // Get the title.
        title = taElem.first().html().trim();
        // Get the author.
        author = taElem.last().html().trim();
        // Get the element that has the details in it, and the detail label span element.
        Element details = infoDoc.select("div.content").first();
        if (details == null) throw initEx();
        Elements labels = details.select("span.label");
        // Get summary.
        summary = Util.cleanHtmlString(makeDetailDivForLabel(details, labels, 0).html().trim());
        // Get rating.
        rating = makeDetailDivForLabel(details, labels, 1).text().trim();
        // Get fic type (categories).
        ficType = makeDetailDivForLabel(details, labels, 2).text().trim();
        // Get characters. Ignore if "None".
        String temp = makeDetailDivForLabel(details, labels, 3).text().trim();
        characters = temp.equals("None") ? null : temp;
        // Get warnings.
        temp = makeDetailDivForLabel(details, labels, 4).text().trim();
        warnings = temp.equals("None") ? null : temp;
        // Get series. Ignore if "None".
        temp = makeDetailDivForLabel(details, labels, 6).text().trim();
        series = temp.equals("None") ? null : temp;
        // Get chapter count to generate chapter URLs.
        int chapCount = Integer.parseInt(makeDetailDivForLabel(details, labels, 7).text().trim());
        // Get status.
        temp = makeDetailDivForLabel(details, labels, 8).text().trim();
        status = temp.equals("Yes") ? C.STAT_C : C.STAT_I;
        // Get word count.
        wordCount = Integer.parseInt(makeDetailDivForLabel(details, labels, 9).text().trim());
        // Get date published.
        datePublished = makeDetailDivForLabel(details, labels, 11).text().trim();
        // Get date last updated.
        dateUpdated = makeDetailDivForLabel(details, labels, 12).text().trim();
        // Generate chapter URLs.
        for (int i = 0; i < chapCount; i++) chapterUrls.add(String.format(C.MN_C_URL, storyId, i + 1, warnBypass));
    }

    /**
     * Return a new div element which contains the child nodes from the parent that correspond to a particular label
     * element.
     * @param parent   Element to copy nodes from.
     * @param labels   List of label elements from the parent.
     * @param labelIdx Index of label (in labels, not parent!!) which we want to make a div for.
     * @return A new div element, or null if the parameters passed aren't valid.
     */
    private Element makeDetailDivForLabel(Element parent, Elements labels, int labelIdx) {
        // Parameter checks.
        if (parent == null || labelIdx < 0 || labelIdx >= labels.size()) return null;
        // Copy parent's child nodes.
        ArrayList<Node> nodeCopies = (ArrayList<Node>) parent.childNodesCopy();
        // Sometimes MuggleNet wraps the details in a <p> tag, which we don't really want, so we'll unwrap its children.
        if (nodeCopies.size() == 2) {
            Node wrapper = nodeCopies.remove(1);
            nodeCopies.addAll(wrapper.childNodesCopy());
        }
        // Figure out start and end indices for copying nodes from parent based on the index of labels[labelIdx] in
        // the parent.
        // Start index should be
        int startIdx = nodeCopies.indexOf(labels.get(labelIdx)) + 1;
        int endIdx = labelIdx != labels.size() - 1 ? nodeCopies.indexOf(labels.get(labelIdx + 1)) : nodeCopies.size();
        // Create the new div.
        Element newDiv = new Element(Tag.valueOf("div"), "");
        // Loop through the copied nodes, starting at the startIdx and up to but not including the endIdx, and append
        // those nodes to the new div.
        for (int i = startIdx; i < endIdx; i++) newDiv.appendChild(nodeCopies.get(i));
        // Return the new div.
        return newDiv;
    }
}
