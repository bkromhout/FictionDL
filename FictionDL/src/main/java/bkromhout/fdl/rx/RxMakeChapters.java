package bkromhout.fdl.rx;

import bkromhout.fdl.Chapter;
import bkromhout.fdl.storys.Story;
import com.squareup.okhttp.Response;
import rx.Observable;

/**
 * Converts OkHttp Responses to {@link Chapter Chapters}.
 * @see Chapter#fromResponse(Response, int)
 */
public class RxMakeChapters implements Observable.Transformer<Response, Chapter> {
    /**
     * {@link Story} to make chapters for.
     */
    private final Story story;

    /**
     * Make {@link Chapter}s for the given {@link Story} using OkHttp Responses.
     * @param story Story to make chapters for.
     */
    public RxMakeChapters(Story story) {
        this.story = story;
    }

    /**
     * Get the number of a chapter based on the index of its url in {@link Story#chapterUrls}.
     * @param response Response object which we can get the chapter url from.
     * @return Chapter number.
     */
    private int chapNumFromResponse(Response response) {
        return story.getChapterUrls().indexOf(response.request().url().toString()) + 1;
    }

    @Override
    public Observable<Chapter> call(Observable<Response> responses) {
        return responses.flatMap(r -> Observable.just(Chapter.fromResponse(r, chapNumFromResponse(r))));
    }
}
