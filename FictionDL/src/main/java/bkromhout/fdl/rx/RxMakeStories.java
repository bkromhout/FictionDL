package bkromhout.fdl.rx;

import bkromhout.fdl.Main;
import bkromhout.fdl.storys.Story;
import bkromhout.fdl.util.Util;
import rx.Observable;
import rx.Subscriber;

import java.lang.reflect.InvocationTargetException;

/**
 * Creates {@link Story}s from story urls asynchronously.
 */
public class RxMakeStories implements Observable.Transformer<String, Story> {
    private final Class<? extends Story> storyClass;

    /**
     * Create instances of the given {@link Story} subclass.
     * @param storyClass Concrete implementation of Story to make.
     */
    public RxMakeStories(Class<? extends Story> storyClass) {
        this.storyClass = storyClass;
    }

    @Override
    public Observable<Story> call(Observable<String> storyUrls) {
        return storyUrls.flatMap(url -> Observable.create(new CreateStory(url)));
    }

    /**
     * Creates {@link Story}s from story urls.
     */
    private final class CreateStory implements Observable.OnSubscribe<Story> {
        private final String url;

        /**
         * Create a new {@link Story} from the given story url.
         * @param url Story url.
         */
        private CreateStory(String url) {
            this.url = url;
        }

        @Override
        public void call(Subscriber<? super Story> sub) {
            try {
                // Doing a bit of reflection magic here to construct story classes ;)
                sub.onNext(storyClass.getConstructor(String.class).newInstance(url));
                sub.onCompleted();
            } catch (InvocationTargetException e) {
                // Now figure out what the heck to put in the log.
                if (e.getCause() == null) e.printStackTrace(); // Who knows what caused it.
                else if (e.getCause().getMessage() == null) e.getCause().printStackTrace(); // Ditto.
                else Util.log(e.getCause().getMessage()); // Probably an InitStoryException, print its message.
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
