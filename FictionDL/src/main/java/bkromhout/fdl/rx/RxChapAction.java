package bkromhout.fdl.rx;

import bkromhout.fdl.chapter.Chapter;
import rx.Observable;
import rx.functions.Action1;

/**
 * Apply an action to {@link Chapter}s asynchronously using .flatmap().
 */
public class RxChapAction implements Observable.Transformer<Chapter, Chapter> {
    private final Action1<? super Chapter> chapAction;

    /**
     * Create a new {@link RxChapAction} to execute asynchronously.
     * @param chapAction Action to take on a {@link Chapter}.
     */
    public RxChapAction(Action1<? super Chapter> chapAction) {
        this.chapAction = chapAction;
    }

    @Override
    public Observable<Chapter> call(Observable<Chapter> chapters) {
        return chapters.flatMap(chapter -> Observable.just(chapter).doOnNext(chapAction));
    }
}
