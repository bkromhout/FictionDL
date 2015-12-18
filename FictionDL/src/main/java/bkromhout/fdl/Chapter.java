package bkromhout.fdl;

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
     * Create a new Chapter model.
     */
    private Chapter(Response response) throws IOException {
        if (response == null) throw new IOException();
        this.url = response.request().urlString();
        this.rawHtml = Jsoup.parse(response.body().byteStream(), null, url);
    }

    /**
     * Create a new Chapter using an OkHttp Response obtained from the chapter url.
     * @param response Response containing chapter page HTML.
     * @return New Chapter, or null if there are problems.
     */
    public static Chapter fromResponse(Response response) {
        try {
            return new Chapter(response);
        } catch (IOException e) {
            Util.loudf(C.PARSE_HTML_FAILED, response.request().urlString());
            return null;
        }
    }
}
