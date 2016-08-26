package bkromhout.fdl.rx;

import bkromhout.fdl.chapter.Chapter;
import bkromhout.fdl.chapter.ChapterSource;
import bkromhout.fdl.stories.Story;
import rx.Observable;

/**
 * Converts {@link ChapterSource}s to {@link Chapter}s.
 */
public class RxMakeChapters implements Observable.Transformer<ChapterSource, Chapter> {
    /**
     * {@link Story} to make {@link Chapter}s for.
     */
    private final Story story;

    /**
     * Make {@link Chapter}s for the given {@link Story}.
     * @param story Story.
     */
    public RxMakeChapters(Story story) {
        this.story = story;
    }

    @Override
    public Observable<Chapter> call(Observable<ChapterSource> sources) {
        return sources.flatMap(s -> Observable.just(s.toChapter(story)));
    }
}
