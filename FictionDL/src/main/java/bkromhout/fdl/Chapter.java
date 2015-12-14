package bkromhout.fdl;

import com.squareup.okhttp.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

/**
 * Chapter model. Very simple, just holds the chapter title and content.
 */
public class Chapter {
    // Chapter url.
    public String url;
    // Chapter title.
    public String title;
    // Chapter HTML.
    public Document html;
    // The chapter's actual content.
    public String content;

    /**
     * Create a new Chapter model.
     */
    private Chapter(Response response) throws IOException {
        if (response == null) throw new IOException();
        this.url = response.request().urlString();
        this.html = Jsoup.parse(response.body().byteStream(), null, url);
    }

    /**
     * Create a new Chapter using an OkHttp Response obtained from the chapter URL.
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
