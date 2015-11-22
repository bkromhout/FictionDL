package bkromhout.Story;

import bkromhout.C;
import bkromhout.Main;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Model object for a SIYE story.
 */
public class SiyeStory {
    // Story content URL.
    private String contentUrl;
    // Story ID.
    private String storyId;
    // Story info URL.
    private String authorIdLink;
    // Story title.
    private String title;
    // Story author.
    private String author;
    // Story summary.
    private String summary;
    // Story word count.
    private int wordCount;
    // Story chapter count.
    private int chapterCount;
    // Story rating.
    private String rating;

    /**
     * Create a new SiyeStory object based off of a URL.
     * @param url URL of the story this model represents.
     */
    public SiyeStory(String url) throws IOException {
        contentUrl = normalizeUrl(url);
        populateInfo();
    }

    private void populateInfo() throws IOException {
        // Get the HTML at the info url.
        Document doc = Main.downloadHtml(String.format(C.SIYE_AUTHOR_URL, authorIdLink));
        if (doc == null) throw new IOException(C.ENTRY_PT_DL_FAILED);
        // Get the story entry from on the author's page.
        Element story = doc.select(String.format("td td:has(a[href=\"%s\"])", authorIdLink)).first();
        // Get title.
        title = story.select(String.format("a[href=\"viewstory.php?sid=%s\"]", storyId)).first().text();
        // Get author.
        author = story.select(String.format("a[href\"%s\"]", authorIdLink)).first().text();
        // Get the summary.
        summary = story.textNodes().get(4).text().trim();
        // Get details strings to get other stuff.
        ArrayList<String> details = story.select("div").first().textNodes().stream().map(TextNode::text).collect(
                Collectors.toCollection(ArrayList::new));
        // Get rating.
        rating = details.get(0).trim().replace(" -", "");
        // Get word count.
        String temp = details.get(1).trim();
        wordCount = Integer.parseInt(temp.substring(temp.lastIndexOf(" ") + 1, temp.length()));
        // Get chapter count.
        temp = details.get(2).trim();
        chapterCount = Integer.parseInt(temp.substring(temp.indexOf("Chapters: ") + 10, temp.indexOf(" - P")));

    }

    /**
     * There are a number of different SIYE URLs which are valid, we want a very specific one for downloading the
     * content, so normalize the URL we've got.
     * @param url A valid SIYE URL.
     * @return Normalized content URL.
     */
    private String normalizeUrl(String url) throws IOException {
        // Need to get the story ID from the given URL first.
        Matcher idMatcher = Pattern.compile(C.SIYE_SID_REGEX).matcher(url);
        idMatcher.find();
        storyId = idMatcher.group(1);
        // Then get the author ID.
        authorIdLink = findAuthorIdLink(String.format(C.SIYE_CH_URL, storyId));
        // Then generate the content URL.
        return String.format(C.SIYE_CONTENT_URL, storyId);
    }

    /**
     * Use an SIYE chapter URL to find the Author ID Link (Looks like "viewuser.php?uid=[authorId]").
     * @param chUrl Chapter URL.
     * @return Author ID link.
     */
    private String findAuthorIdLink(String chUrl) throws IOException {
        // Sadly, yes, we have to go through this whole thing. The things I do for consistency...
        // Download chapter HTML.
        Document chDoc = Main.downloadHtml(chUrl);
        if (chDoc == null) throw new IOException(C.ENTRY_PT_DL_FAILED);
        // Then find the element that lets us get the relative link to the author's page.
        Element aIdElement = chDoc.select("h3 a").first();
        // Now return the author page URL.
        return aIdElement.attr("href");
    }

    public String getContentUrl() {
        return contentUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getSummary() {
        return summary;
    }

    public int getWordCount() {
        return wordCount;
    }

    public int getChapterCount() {
        return chapterCount;
    }

    public String getRating() {
        return rating;
    }
}
