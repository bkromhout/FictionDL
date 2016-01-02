package bkromhout.fdl.downloaders;

import bkromhout.fdl.chapter.Chapter;
import bkromhout.fdl.site.Sites;
import org.jsoup.nodes.Element;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Downloader for <a href="https://www.fanfiction.net">FanFiction.net</a> stories.
 */
public class FanFictionDL extends ParsingDL {

    /**
     * Create a new {@link FanFictionDL}.
     */
    public FanFictionDL() {
        super(Sites.FFN(), "div#storytext");
    }

    /**
     * Creates a title for a chapter by parsing the actual title from {@link Chapter#rawHtml}.
     * @param chapter Chapter object.
     */
    @Override
    protected void generateChapTitle(Chapter chapter) {
        // Try to find a <select> element on the page that has chapter titles.
        Element titleElement = chapter.rawHtml.select("select#chap_select > option[selected]").first();

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
                chapter.title = String.format("Chapter %d", chapter.number);
            }
        } else {
            chapter.title = "Chapter 1";
        }
    }

    /**
     * FanFiction.net's chapter content HTML can have some odd stuff which is illegal in ePUB XHTML, so we fix those
     * issues here.
     * @param chapter Chapter object.
     */
    @Override
    protected void sanitizeChap(Chapter chapter) {
        // Ensure that any "noshade" attributes are non-boolean, XHTML doesn't like boolean attributes.
        chapter.content = chapter.content.replace("noshade", "noshade=\"noshade\"");
    }
}
