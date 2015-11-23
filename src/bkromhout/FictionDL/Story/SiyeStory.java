package bkromhout.FictionDL.Story;

import bkromhout.FictionDL.C;
import bkromhout.FictionDL.Downloader.SiyeDL;
import bkromhout.FictionDL.Main;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Model object for a SIYE story. Despite the word "model", this is not an object with a light initialization cost, as
 * it accesses the internet to retrieve story information.
 */
public class SiyeStory {
    // Story ID.
    private String storyId;
    // Story title.
    private String title;
    // Story author.
    private String author;
    // Story summary.
    private String summary;
    // Story word count.
    private int wordCount;
    // Story rating.
    private String rating;
    // List of chapter URLs.
    private ArrayList<String> chapterUrls = new ArrayList<>();

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
        // Figure out the SIYE story ID and author ID link.
        String authorIdLink = findAuthorIdLink(url);
        // Get the HTML at the info url.
        Document doc = Main.downloadHtml(String.format(C.SIYE_AUTHOR_URL, authorIdLink));
        if (doc == null) throw new IOException(String.format(C.STORY_DL_FAILED, SiyeDL.SITE, storyId));
        // Get the story entry from on the author's page.
        Element story = doc.select(String.format("td tr td:has(a[href=\"viewstory.php?sid=%s\"])", storyId)).last();
        // Get title.
        title = story.select(String.format("a[href=\"viewstory.php?sid=%s\"]", storyId)).first().text();
        // Get author.
        author = story.select(String.format("a[href=\"%s\"]", authorIdLink)).first().text();
        // Due to SIYE's *incredibly* crappy HTML, we need to check to see if the story Element we currently have
        // actually has the rest of the parts we need or not. If the story is part of a series, the "row" on SIYE's
        // site is actually split across *two* <tr>s rather than being contained within one.
        if (story.select("div").first() == null) {
            // This story must be part of a series, which means that that the <td> we have doesn't have the rest of
            // the info we need. Starting from the <td> we currently have, we need to go up to the parent <tr>, then
            // over to the next immediate sibling <tr> from the parent <tr>, and then into that sibling <tr>'s last
            // <td> child...yes, this *is* complicated sounding and SIYE *should* have properly structured HTML. Ugh.
            story = story.parent().nextElementSibling().children().last();
            // Oh, and we have to get the summary now too since the index of the TextNode is different.
            summary = story.ownText().trim();
        } else {
            // The <td> we already have has everything, and we can get the summary now.
            summary = story.textNodes().get(4).text().trim();
        }
        // Get details strings to get other stuff.
        ArrayList<String> details = story.select("div").first().textNodes().stream().map(TextNode::text)
                .collect(Collectors.toCollection(ArrayList::new));
        // Get rating.
        rating = details.get(0).trim().replace(" -", "");
        // Get word count.
        String temp = details.get(1).trim();
        wordCount = Integer.parseInt(temp.substring(temp.lastIndexOf(" ") + 1, temp.length()));
        // Get chapter count.
        temp = details.get(2).trim();
        int chapterCount = Integer.parseInt(temp.substring(temp.indexOf("Chapters: ") + 10, temp.indexOf(" - P")));
        // Generate chapter URLs.
        for (int i = 0; i < chapterCount; i++) chapterUrls.add(String.format(C.SIYE_CHAP_URL, storyId, i + 1));
    }

    /**
     * Use a valid SIYE story/chapter URL (not the printable version!) to find the story's Author ID link.
     * @param url Valid SIYE story/chapter URL (The format must be valid, not what it points to).
     * @return Author ID link (looks like "viewuser.php?uid=[authorId]").
     */
    private String findAuthorIdLink(String url) throws IOException {
        // Need to normalize this URL first to be sure we can get the author ID link.
        // Start by getting the story ID from the URL.
        Matcher idMatcher = Pattern.compile(C.SIYE_SID_REGEX).matcher(url);
        idMatcher.find();
        storyId = idMatcher.group(1);
        // Now download the first chapter's HTML.
        Document chDoc = Main.downloadHtml(String.format(C.SIYE_INFO_URL, storyId));
        if (chDoc == null) throw new IOException(String.format(C.STORY_DL_FAILED, SiyeDL.SITE, storyId));
        // Then find the element that lets us get the relative link to the author's page.
        Element aIdElement = chDoc.select("h3 a").first();
        // Throw an exception if we couldn't find the link to the author's page, as it likely means that the URL
        // format was valid but that it doesn't point to a real story/chapter on SIYE.
        if (aIdElement == null) throw new IOException(String.format(C.STORY_DL_FAILED, SiyeDL.SITE, storyId));
        // Now return the author page URL.
        return aIdElement.attr("href");
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

    public String getRating() {
        return rating;
    }

    public ArrayList<String> getChapterUrls() {
        return chapterUrls;
    }
}
