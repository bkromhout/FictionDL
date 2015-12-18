package bkromhout.fdl.downloaders;

import bkromhout.fdl.C;
import bkromhout.fdl.Chapter;
import bkromhout.fdl.FictionDL;
import bkromhout.fdl.Site;
import bkromhout.fdl.storys.SiyeStory;
import org.jsoup.nodes.Element;

import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Downloader for siye.co.uk ("Sink Into Your Eyes") stories.
 */
public class SiyeDL extends ParsingDL {

    /**
     * Create a new SIYE downloader.
     * @param fictionDL FictionDL object which owns this downloader.
     * @param urls      List of SIYE urls.
     */
    public SiyeDL(FictionDL fictionDL, HashSet<String> urls) {
        super(fictionDL, SiyeStory.class, Site.SIYE, urls, null);
        extraPreDlMsgs = C.SIYE_PRE_DL;
    }

    /**
     * Creates a title for a chapter by parsing the actual title form the raw chapter HTML in the given Chapter object.
     * @param chapter Chapter object.
     */
    @Override
    protected void generateChapTitle(Chapter chapter) {
        // Try to find a <select> element on the page that has chapter titles.
        Element titleElement = chapter.rawHtml.select("select[name=\"chapter\"] > option[selected]").first();
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
     * For SIYE, we can't capture both the author's notes and the chapter content from the raw chapter HTML with a
     * single selector string, so we have to use two and concatenate the contents of each.
     * @param chapter Chapter object.
     */
    @Override
    protected void extractChapText(Chapter chapter) {
        StringBuilder chapterText = new StringBuilder();
        // So, we need to get a number of things here. First off, we must grab the author's notes (if there are any).
        Element anElement = chapter.rawHtml.select("div#notes").first();
        if (anElement != null) chapterText.append(anElement.html()).append("<hr /><br />");
        // Then, we have to get the actual chapter text itself.
        chapterText.append(chapter.rawHtml.select("td[colspan=\"2\"] span").first().html());
        chapter.content = String.format(C.CHAPTER_PAGE, chapter.title, chapter.title, chapterText.toString());
    }
}
