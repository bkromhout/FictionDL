package bkromhout.fdl.rx;

import bkromhout.fdl.Main;
import bkromhout.fdl.ex.InitStoryException;
import bkromhout.fdl.parsing.StoryEntry;
import bkromhout.fdl.stories.Story;
import bkromhout.fdl.util.C;
import bkromhout.fdl.util.Util;
import rx.Observable;
import rx.Subscriber;

import java.lang.reflect.InvocationTargetException;

/**
 * Creates {@link Story}s from {@link StoryEntry}s asynchronously.
 */
public class RxMakeStories implements Observable.Transformer<StoryEntry, Story> {
    private final Class<? extends Story> storyClass;

    /**
     * Create instances of the given {@link Story} subclass.
     * @param storyClass Concrete implementation of Story to make.
     */
    public RxMakeStories(Class<? extends Story> storyClass) {
        this.storyClass = storyClass;
    }

    @Override
    public Observable<Story> call(Observable<StoryEntry> storyEntries) {
        return storyEntries.flatMap(entry -> Observable.create(new CreateStory(entry)));
    }

    /**
     * Creates {@link Story}s from {@link StoryEntry}s.
     */
    private final class CreateStory implements Observable.OnSubscribe<Story> {
        private final StoryEntry entry;

        /**
         * Create a new {@link Story} from the given {@link StoryEntry}.
         * @param entry Story entry.
         */
        private CreateStory(StoryEntry entry) {
            this.entry = entry;
        }

        @Override
        public void call(Subscriber<? super Story> sub) {
            try {
                // Doing a bit of reflection magic here to construct story classes ;)
                sub.onNext(storyClass.getConstructor(StoryEntry.class).newInstance(entry));
                sub.onCompleted();
            } catch (InvocationTargetException e) {
                // Handle the exception.
                if (e.getCause() == null || e.getCause().getMessage() == null)
                    // Who knows what caused it, print the stacktrace.
                    e.printStackTrace();
                else if (e.getCause() instanceof InitStoryException)
                    // If the cause is an InitStoryException, print its message.
                    Util.log(e.getCause().getMessage());
                else
                    // Not an InitStoryException, but it has a message, so print it.
                    Util.logf(C.UNEXP_STORY_ERR, entry.getUrl(), e.getCause().getMessage());
                // We just return null if a story fails.
                sub.onNext(null);
                sub.onCompleted();
            } catch (ReflectiveOperationException e) {
                // Shouldn't hit this at all. Fail hard if it happens.
                e.printStackTrace();
                Main.exit(1);
            }
        }
    }
}
