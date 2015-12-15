package bkromhout.fdl.downloaders;

import bkromhout.fdl.C;
import bkromhout.fdl.Chapter;
import bkromhout.fdl.FictionDL;
import bkromhout.fdl.storys.FanFictionStory;
import org.jsoup.nodes.Element;
import rx.functions.Action1;

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
        super(fictionDL, FanFictionStory.class, C.NAME_FFN, urls, "div#storytext");
    }

    /**
     * Creates an action which takes a Chapter object and creates a title for it by parsing the real chapter titles from
     * the raw chapter HTML.
     * @return An action which generates chapter titles.
     */
    @Override
    protected Action1<? super Chapter> generateChapTitle() {
        return chapter -> {
            // Try to find a <select> element on the page that has chapter titles.
            Element titleElement = chapter.html.select("select#chap_select > option[selected]").first();
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
                    chapter.title = String.format("Chapter %d", chapter.num);
                }
            } else {
                chapter.title = "Chapter 1";
            }
        };
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
