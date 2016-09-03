package bkromhout.fdl.downloaders;

import bkromhout.fdl.chapter.Chapter;
import bkromhout.fdl.site.Sites;
import bkromhout.fdl.util.C;

/**
 * Downloader for <a href="http://www.harrypotterfanfiction.com">Harry Potter FanFiction</a> stories.
 */
public class HpffDL extends ParsingDL {
    /**
     * Create a new {@link HpffDL}.
     */
    public HpffDL() {
        super(Sites.HPFF(), "div#fluidtext");
    }

    @Override
    void generateChapTitle(Chapter chapter) {
        // Get chapter title stored earlier in story detail tags map.
        chapter.title = chapter.story.getDetailTags().get(
                String.format(C.CHAP_TITLE_KEY_TEMPLATE, chapter.number));
    }
}
