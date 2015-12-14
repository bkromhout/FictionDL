package bkromhout.fdl.downloaders;

import bkromhout.fdl.C;
import bkromhout.fdl.Chapter;
import bkromhout.fdl.FictionDL;
import bkromhout.fdl.storys.MuggleNetStory;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.RequestBody;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Downloader for MuggleNet stories.
 */
public class MuggleNetDL extends ParsingDL {
    /**
     * MuggleNet login page link.
     */
    private static final String MN_L_URL = "http://fanfiction.mugglenet.com/user.php?action=login";

    /**
     * Create a new MuggleNet downloader.
     * @param fictionDL FictionDL object which owns this downloader.
     * @param urls      List of MuggleNet URLs.
     */
    public MuggleNetDL(FictionDL fictionDL, HashSet<String> urls) {
        super(fictionDL, MuggleNetStory.class, C.NAME_MN, urls, "div.contentLeft");
    }

    @Override
    protected RequestBody getSiteAuthForm(String u, String p) {
        if (u == null || u.isEmpty() || p == null || p.isEmpty()) return null;
        return new MultipartBuilder().type(MultipartBuilder.FORM)
                                     .addFormDataPart("penname", u)
                                     .addFormDataPart("password", p)
                                     .addFormDataPart("cookiecheck", "1")
                                     .addFormDataPart("submit", "Submit")
                                     .build();
    }

    @Override
    protected String getSiteLoginUrl() {
        return MN_L_URL;
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
                Matcher matcher = Pattern.compile("(\\d+.\\s)(.*)").matcher(titleElement.html().trim());
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
