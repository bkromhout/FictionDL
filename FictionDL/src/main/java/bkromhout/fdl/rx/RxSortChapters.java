package bkromhout.fdl.rx;

import bkromhout.fdl.Chapter;
import rx.Observable;
import rx.functions.Func2;

/**
 * Transformation which reorders the order of the Chapters in an observable (this is, it sorts them) by using
 * .toSortedList() followed by .flatmap() which uses Observable.from() to separate them again.
 */
public class RxSortChapters implements Observable.Transformer<Chapter, Chapter> {
    private final Func2<? super Chapter, ? super Chapter, Integer> sortFunction;

    /**
     * Sort Chapter objects using the given sort function.
     * @param sortFunction Sort function.
     */
    public RxSortChapters(Func2<? super Chapter, ? super Chapter, Integer> sortFunction) {
        this.sortFunction = sortFunction;
    }

    @Override
    public Observable<Chapter> call(Observable<Chapter> chapters) {
        return chapters.toSortedList(sortFunction).flatMap(Observable::from);
    }
}
