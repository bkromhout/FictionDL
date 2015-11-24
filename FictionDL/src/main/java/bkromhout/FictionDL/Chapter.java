package bkromhout.FictionDL;

import org.jsoup.nodes.Document;

import java.nio.charset.StandardCharsets;

/**
 * Chapter model. Very simple, just holds the chapter title and content.
 */
public class Chapter {
    // Chapter title.
    public String title;
    // Chapter HTML.
    public Document html;
    // A clean chapter HTML, ready to be used in an ePUB.
    public String cleanedHtml;

    /**
     * Create a new Chapter model (just for convenience, since instance members are public).
     * @param title The chapter's title.
     * @param html  The chapter's HTML content.
     */
    public Chapter(String title, Document html) {
        this.title = title;
        this.html = html;
    }

    /**
     * Take this chapter's HTML and return it as a UTF-8 encoded byte array.
     * @return Chapter HTML as byte[].
     */
    public byte[] getContentBytes() {
        return cleanedHtml.getBytes(StandardCharsets.UTF_8);
    }
}
