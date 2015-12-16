package bkromhout.fdl.rx;

import bkromhout.fdl.Chapter;
import com.squareup.okhttp.Response;
import rx.Observable;

/**
 * Converts OkHttp Responses to Chapter objects.
 */
public class RxMakeChapters implements Observable.Transformer<Response, Chapter> {

    @Override
    public Observable<Chapter> call(Observable<Response> responses) {
        return responses.flatMap(response -> Observable.just(Chapter.fromResponse(response)));
    }
}
