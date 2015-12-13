package bkromhout.fdl;

import com.squareup.okhttp.OkHttpClient;
import org.jsoup.nodes.Document;
import rx.Observable;

/**
 * Chapter model. Very simple, just holds the chapter title and content.
 */
public class Chapter {
    // Chapter index, for sorting.
    private long index;
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
    public Chapter(long index, String url) {
        this.index = index;
        this.url = url;
    }

    /**
     * Get the original index of this chapter, useful for sorting if chapters were downloaded out of order.
     * @return Chapter index.
     */
    public long getIndex() {
        return index;
    }

    public Observable<Chapter> getChapter(OkHttpClient httpClient) {

    }
}
