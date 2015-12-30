package bkromhout.fdl.chapter;

import bkromhout.fdl.util.C;
import bkromhout.fdl.util.Util;
import org.jsoup.nodes.Document;

/**
 * Chapter model.
 */
public class Chapter {
    /**
     * Chapter number.
     */
    public final int number;
    /**
     * Chapter title string.
     */
    public String title;
    /**
     * Raw chapter HTML.
     */
    public final Document rawHtml;
    /**
     * Stringified chapter XHTML which will eventually be put into the ePUB.
     */
    public String content;

    /**
     * Create a new {@link Chapter} using raw HTML which can be manipulated in an OO fashion.
     * <p>
     * This constructor purposefully <i>does not</i> call through to the {@code Chapter(Document, String, int)}
     * constructor, in case we do not wish to create the title when creating this {@link Chapter}.
     * @param rawHtml Raw chapter content.
     * @param number  Chapter number.
     */
    Chapter(Document rawHtml, int number) {
        this.rawHtml = rawHtml;
        this.number = number;
    }

    /**
     * Create a new {@link Chapter} using a raw HTML string.
     * <p>
     * Note that this constructor will set {@link #content} to be {@code htmlStr} without putting it into the chapter
     * template first (since we don't know the title yet). Call {@link #wrapContentInTemplate()} later after populating
     * {@link #title} to do so.
     * @param htmlStr Raw chapter content String.
     * @param number  Chapter number.
     */
    Chapter(String htmlStr, int number) {
        this.rawHtml = null;
        this.number = number;
        this.content = htmlStr;
    }

    /**
     * Convenience method which takes the given content string and puts it into the final chapter XHTML template from
     * {@link C#CHAPTER_PAGE}. This is a no-op if {@code contentStr} is null.
     * @param contentStr Chapter html content string.
     * @throws IllegalStateException if {@link #title} is null, since the template needs it.
     */
    public void contentFromString(String contentStr) {
        if (title == null) throw new IllegalStateException();
        if (contentStr == null) return;
        this.content = String.format(C.CHAPTER_PAGE, title, title, contentStr);
    }

    /**
     * Convenience method which will wrap the current value of {@link #content} in {@link C#CHAPTER_PAGE}.
     * @throws IllegalStateException if either {@link #content} or {@link #title} are null, since the template needs
     *                               both.
     */
    public void wrapContentInTemplate() {
        if (content == null || title == null) throw new IllegalStateException();
        contentFromString(content);
    }

    /**
     * Convenience method which runs {@link #content} through {@link Util#cleanHtmlString(String)}.
     */
    public void sanitizeContent() {
        content = Util.cleanHtmlString(content);
    }

    /**
     * Compares the two {@link Chapter Chapters} by their {@link Chapter#number number} fields using {@link
     * Integer#compare(int, int)}.
     * @param c1 One Chapter object.
     * @param c2 Another Chapter object.
     * @return Integer which indicates order of c1 and c2.
     */
    public static int sort(Chapter c1, Chapter c2) {
        return Integer.compare(c1.number, c2.number);
    }
}
