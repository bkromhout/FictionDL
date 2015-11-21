package bkromhout;

import org.jsoup.nodes.Document;

/**
 * Chapter model.
 */
public class Chapter {
    // Chapter title.
    public String title;
    // Chapter HTML.
    public Document html;

    public Chapter(String title, Document html) {
        this.title = title;
        this.html = html;
    }
}
