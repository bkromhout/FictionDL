package bkromhout.fictiondl.Downloader;

import bkromhout.fictiondl.C;
import bkromhout.fictiondl.Chapter;
import bkromhout.fictiondl.FictionDL;
import bkromhout.fictiondl.Story.MuggleNetStory;
import bkromhout.fictiondl.Util;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Downloader for MuggleNet stories.
 */
public class MuggleNetDL extends ParsingDL implements AuthSupport {

    /**
     * Create a new MuggleNet downloader.
     * @param fictionDL FictionDL object which owns this downloader.
     * @param urls      List of MuggleNet URLs.
     */
    public MuggleNetDL(FictionDL fictionDL, HashSet<String> urls) {
        super(fictionDL,MuggleNetStory.class, C.NAME_MN, urls, "div.contentLeft");
    }

    /**
     * Login and get cookies which will be sent with each subsequent request. This will clear the current cookies prior
     * to storing the new cookies, so if an exception is thrown while getting the new cookies, the current cookies will
     * be empty.
     * @param username Username.
     * @param password Password.
     */
    public void addAuth(String username, String password) {
        Util.logf(C.STARTING_SITE_AUTH_PROCESS, siteName);
        // Add form-data elements.
        HashMap<String, String> formData = new HashMap<>();
        formData.put("penname", username);
        formData.put("password", password);
        formData.put("cookiecheck", "1");
        formData.put("submit", "Submit");
        // Clear old cookies.
        cookies.clear();
        try {
            // Get new cookies.
            cookies.putAll(Util.getAuthCookies(C.MN_L_URL, formData));
            Util.logf(C.DONE);
        } catch (IOException e) {
            Util.logf(C.LOGIN_FAILED, siteName);
        }
    }

    /**
     * Generate chapter titles by parsing real titles from chapter HTML.
     * @param chapters List of Chapters.
     */
    @Override
    protected void generateChapTitles(ArrayList<Chapter> chapters) {
        // Parse chapter titles from chapter HTMLs.
        for (Chapter chapter : chapters) {
            // Try to find a <select> element on the page that has chapter titles.
            Element titleElement = chapter.html.select("select[name=\"chapter\"] > option[selected]").first();
            // If the story is chaptered, we'll find the <select> element and can get the chapter title from that (we
            // strip off the leading "#. " part of it). If the story is only one chapter, we just call it "Chapter 1".
            if (titleElement != null) {
                Matcher matcher = Pattern.compile(C.MN_CHAP_TITLE_REGEX).matcher(titleElement.html().trim());
                matcher.matches();
                try {
                    chapter.title = matcher.group(2);
                } catch (IllegalStateException e) {
                    // Apparently, it's possible for there to *not* be a title for a chapter, so the title string may
                    // look like "24. " or something. If that happens, title the chapter "Chapter #".
                    chapter.title = String.format("Chapter %d", chapters.indexOf(chapter) + 1);
                }
            } else {
                chapter.title = "Chapter 1";
            }
        }
    }

    /**
     * MuggleNet needs to have number of elements removed for div.contentLeft, then we'll add <hr />s between story and
     * notes.
     * @param chapter Chapter object.
     * @return Chapter HTML, with chapter text extracted from original and put into template.
     */
    @Override
    protected String extractChapText(Chapter chapter) {
        StringBuilder chapterText = new StringBuilder();
        // First off, we need to drill down to just the div.contentLeft element.
        Element content = chapter.html.select(chapTextSelector).first();
        // Now, we want to strip out any children of div.contentLeft which are not div.notes or div#story, so select
        // all of those and remove them.
        content.select("div.contentLeft > *:not(div.notes, div#story)").remove();
        // Now, we want to insert <hr /> tags between any remaining divs.
        content.children().after("<hr />");
        content.select("hr").last().remove();
        // Now we can finally output the html.
        chapterText.append(content.html());
        return String.format(C.CHAPTER_PAGE, chapter.title, chapter.title, chapterText.toString());
    }
}
