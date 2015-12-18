package bkromhout.fdl.rx;

import bkromhout.fdl.Main;
import bkromhout.fdl.Util;
import bkromhout.fdl.downloaders.Downloader;
import bkromhout.fdl.storys.Story;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import rx.Observable;
import rx.Subscriber;

import java.lang.reflect.InvocationTargetException;

/**
 * Creates Story objects from Story urls asynchronously.
 */
public class RxMakeStories implements Observable.Transformer<String, Story> {
    private final Class<? extends Story> storyClass;
    private final Downloader downloader;

    /**
     * Create Story objects of the given type which will be downloaded by the given Downloader.
     * @param storyClass Concrete implementation of Story to make.
     * @param downloader Downloader which will own the created stories.
     */
    public RxMakeStories(Class<? extends Story> storyClass, Downloader downloader) {
        this.storyClass = storyClass;
        this.downloader = downloader;
    }

    @Override
    public Observable<Story> call(Observable<String> storyUrls) {
        return storyUrls.flatMap(url -> Observable.create(new CreateStory(url)));
    }

    /**
     * Creates Stories from story urls.
     */
    private final class CreateStory implements Observable.OnSubscribe<Story> {
        private final String url;

        /**
         * Create a new Story from the given story url.
         * @param url Story url.
         */
        public CreateStory(String url) {
            this.url = url;
        }

        @Override
        public void call(Subscriber<? super Story> sub) {
            try {
                // Doing a bit of reflection magic here to construct story classes ;)
                sub.onNext(ConstructorUtils.invokeConstructor(storyClass, downloader, url));
                sub.onCompleted();
            } catch (InvocationTargetException e) {
                // Now figure out what the heck to put in the log.
                if (e.getCause() == null) e.printStackTrace();
                else if (e.getCause().getMessage() == null) e.getCause().printStackTrace();
                else Util.log(e.getCause().getMessage());
                // We'll return null so that Downloader knows to call .storyProcessed() for us.
                sub.onNext(null);
                sub.onCompleted();
            } catch (ReflectiveOperationException e) {
                // Shouldn't hit this at all. Fail hard if it happens.
                e.printStackTrace();
                Main.exit(1);
                //sub.onError(e);
            }
        }
    }
}
