package bkromhout.FictionDL;

import org.jsoup.nodes.Document;

/**
 * Chapter model. Very simple, just holds the chapter title and content.
 */
public class Chapter {
    // Chapter title.
    public String title;
    // Chapter HTML.
    public Document html;
    // The chapter's actual content.
    public String content;

    /**
     * Create a new Chapter model (just for convenience, since instance members are public).
     * @param title The chapter's title.
     * @param html  The chapter's HTML content.
     */
    public Chapter(String title, Document html) {
        this.title = title;
        this.html = html;
    }
}
