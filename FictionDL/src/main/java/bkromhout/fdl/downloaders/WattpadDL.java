package bkromhout.fdl.downloaders;

import bkromhout.fdl.chapter.Chapter;
import bkromhout.fdl.site.Sites;
import bkromhout.fdl.util.C;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Downloader for <a href="http://www.wattpad.com">Wattpad</a> stories.
 */
public class WattpadDL extends ParsingDL {
    /**
     * Create a new {@link WattpadDL}.
     */
    public WattpadDL() {
        super(Sites.WP(), "p");
    }

    /**
     * Creates a title for a chapter by parsing the actual title from {@link Chapter#rawHtml}.
     * @param chapter Chapter object.
     */
    @Override
    void generateChapTitle(Chapter chapter) {
        // Get chapter title stored earlier in story detail tags map.
        chapter.title = chapter.story.getDetailTags().get(
                String.format(C.CHAP_TITLE_KEY_TEMPLATE, chapter.number));
    }

    @Override
    void extractChapText(Chapter chapter) {
        StringBuilder chapterText = new StringBuilder();

        // Get all of the chapter text elements, which are (thankfully) stored in <p> elements.
        Elements pElements = chapter.rawHtml.select(chapTextSelector);
        // Then strip the data-p-id attribute, and append to the chapterText string.
        for (Element p : pElements) {
            p.removeAttr("data-p-id");
            chapterText.append(p.outerHtml());
        }

        // Set the chapter's text.
        chapter.contentFromString(chapterText.toString());
    }
}
