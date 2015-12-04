package bkromhout.FictionDL.Story;

import bkromhout.FictionDL.C;
import bkromhout.FictionDL.Downloader.MuggleNetDL;
import bkromhout.FictionDL.Util;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.List;

/**
 * Model object for a MuggleNet story. Despite the word "model", this is not an object with a light initialization cost,
 * as it accesses the internet to retrieve story information.
 */
public class MuggleNetStory extends Story {

    /**
     * Create a new MuggleNetStory object based off of a URL.
     * @param url URL of the story this model represents.
     */
    public MuggleNetStory(String url) throws IOException {
        this.url = url;
        populateInfo();
    }

    /**
     * Populate this model's fields.
     * @throws IOException Throw for many reasons, but the net result is that we can't build a story model for this.
     */
    private void populateInfo() throws IOException {
        // Set site.
        site = C.HOST_MN;
        // Get story ID first.
        storyId = parseStoryId(url, C.MN_SID_REGEX, 1);
        // Normalize the URL, since there are many valid MN URL formats.
        url = String.format(C.MN_S_URL, storyId);
        // Get the story page in order to parse the story info.
        Document infoDoc = Util.downloadHtml(url);
        // Make sure that we got a Document and that this is a valid story.
        if (infoDoc == null || infoDoc.select("div.errorText").first() != null)
            throw new IOException(String.format(C.STORY_DL_FAILED, MuggleNetDL.SITE, storyId));
        // Get the element that has the title and author of the story in it.
        Elements taElem = infoDoc.select("div#pagetitle a");
        if (taElem == null) throw new IOException(String.format(C.STORY_DL_FAILED, MuggleNetDL.SITE, storyId));
        // Get the title.
        title = taElem.first().html().trim();
        // Get the author.
        author = taElem.last().html().trim();
        // Get the element that has the details in it, and the detail label span element.
        Element details = infoDoc.select("div.content").first();
        if (details == null) throw new IOException(String.format(C.STORY_DL_FAILED, MuggleNetDL.SITE, storyId));
        Elements labels = details.select("span.label");
        // Get summary.
        summary = makeDetailDivForLabel(details, labels, 0).html().trim();
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
        for (int i = 0; i < chapCount; i++) chapterUrls.add(String.format(C.MN_C_URL, storyId, i + 1));
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
        List<Node> nodeCopies = parent.childNodesCopy();
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
