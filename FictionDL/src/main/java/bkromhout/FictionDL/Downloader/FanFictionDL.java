package bkromhout.fictiondl.Downloader;

import bkromhout.fictiondl.C;
import bkromhout.fictiondl.Chapter;
import bkromhout.fictiondl.FictionDL;
import bkromhout.fictiondl.Story.FanFictionStory;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Downloader for FanFiction.net stories.
 */
public class FanFictionDL extends ParsingDL {

    /**
     * Create a new FanFiction.net downloader.
     * @param fictionDL FictionDL object which owns this downloader.
     * @param urls      List of FanFiction.net URLs.
     */
    public FanFictionDL(FictionDL fictionDL, HashSet<String> urls) {
        super(fictionDL,FanFictionStory.class, C.NAME_FFN, urls, "div#storytext");
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
            Element titleElement = chapter.html.select("select#chap_select > option[selected]").first();
            // If the story is chaptered, we'll find the <select> element and can get the chapter title from that (we
            // strip off the leading "#. " part of it). If the story is only one chapter, we just call it "Chapter 1".
            if (titleElement != null) {
                Matcher matcher = Pattern.compile(C.FFN_CHAP_TITLE_REGEX).matcher(titleElement.html().trim());
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
     * Takes chapter HTML from a FanFiction.net chapter and cleans it up, before putting it into the xhtml format
     * required for an ePUB.
     * @param chapterString Chapter's text content HTML for a FanFiction.net story chapter.
     * @return Cleaned HTML.
     */
    @Override
    protected String sanitizeChapter(String chapterString) {
        // Do some FanFiction.net specific cleaning.
        chapterString = chapterString.replace("noshade", "noshade=\"noshade\"");
        return chapterString;
    }
}
