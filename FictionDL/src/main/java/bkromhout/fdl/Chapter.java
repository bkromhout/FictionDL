package bkromhout.fdl;

import bkromhout.fdl.util.C;
import bkromhout.fdl.util.Util;
import com.squareup.okhttp.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

/**
 * Chapter model.
 */
public class Chapter {
    /**
     * Chapter url string.
     */
    public final String url;
    /**
     * Chapter number.
     */
    public int number;
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
     * Create a new {@link Chapter}.
     */
    private Chapter(Response response, int number) throws IOException {
        if (response == null) throw new IOException();
        this.url = response.request().url().toString();
        this.rawHtml = Jsoup.parse(response.body().byteStream(), null, url);
        this.number = number;
        // Make sure the ResponseBody is closed so that it doesn't leak.
        response.body().close();
    }

    /**
     * Convenience method which takes the given content string and puts it into the final chapter XHTML template at
     * {@link C#CHAPTER_PAGE}. This is a no-op if the given string is null.
     * @param contentStr Chapter html content string.
     */
    public void contentFromString(String contentStr) {
        if (contentStr == null) return;
        this.content = String.format(C.CHAPTER_PAGE, title, title, contentStr);
    }

    /**
     * Create a new {@link Chapter} using an OkHttp Response obtained from the chapter url.
     * @param response Response containing chapter page HTML.
     * @param number   Chapter number.
     * @return New {@link Chapter}, or null if there are problems.
     */
    public static Chapter fromResponse(Response response, int number) {
        try {
            return new Chapter(response, number);
        } catch (IOException e) {
            Util.loudf(C.PARSE_HTML_FAILED, response.request().urlString());
            return null;
        }
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
