package bkromhout.fdl.downloaders;

import bkromhout.fdl.C;
import bkromhout.fdl.FictionDL;
import bkromhout.fdl.Util;
import bkromhout.fdl.rx.RxMakeStories;
import bkromhout.fdl.storys.Story;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import rx.Observable;

import java.io.IOException;
import java.util.HashSet;

/**
 * Base downloader class. This shouldn't be extended by site-specific downloaders, but rather by classes which provide
 * additional categories of functionality to a group of sites. For example, sites where we parse and generate stories
 * should subclass ParsingDL, which itself is a subclass of this class.
 */
public abstract class Downloader {
    /**
     * The FictionDL instance which owns this downloader.
     */
    protected FictionDL fictionDL;
    /**
     * This is the class of story which this Downloader interacts with. Must extend Story.
     */
    protected Class<? extends Story> storyClass;
    /**
     * Human-readable site name for this downloader.
     */
    protected String siteName;
    /**
     * Story URLs.
     */
    protected HashSet<String> storyUrls;
    /**
     * Any extra messages to print prior to starting the download process.
     */
    protected String extraPreDlMsgs;

    /**
     * Create a new Downloader.
     * @param fictionDL  FictionDL object which owns this downloader.
     * @param storyClass The class of Story which this downloader uses.
     * @param siteName   Human-readable site name for this downloader.
     * @param storyUrls  Set of story URLs to be downloaded.
     */
    protected Downloader(FictionDL fictionDL, Class<? extends Story> storyClass, String siteName,
                         HashSet<String> storyUrls) {
        this.fictionDL = fictionDL;
        this.storyClass = storyClass;
        this.siteName = siteName;
        this.storyUrls = storyUrls;
    }

    /**
     * Download the stories whose URLs were passed to this instance of the downloader upon creation.
     */
    public final void download() {
        // Pre-download logging.
        Util.logf(C.STARTING_SITE_DL_PROCESS, siteName);
        Util.log(extraPreDlMsgs); // This is null unless a subclass has set it to something.
        Util.logf(C.FETCH_BUILD_MODELS, siteName);
        // Create story models from URLs.
        Observable.from(storyUrls)
                  .compose(new RxMakeStories(storyClass, this))
                  .doOnNext(story -> {
                      // Call storyProcessed() if we failed to download a story (AKA, we got a null).
                      if (story == null) storyProcessed();
                  })
                  .doOnSubscribe(() -> Util.logf(C.DL_STORIES_FROM_SITE, siteName))
                  .filter(story -> story != null) // Get rid of failed stories.
                  .toBlocking() // Wait until we have all of the stories...
                  .forEach(this::downloadStory);  // ...then download them.
        // Post-download logging.
        Util.logf(C.FINISHED_WITH_SITE, siteName);
    }

    /**
     * Download a story.
     * <p>
     * For subclasses which choose to override this method: Make sure that if a story has been processed to the point
     * where it won't be touched again, the {@link #storyProcessed()} method is called. This call would not be necessary
     * if, for example, a story is passed to a different downloader which would call {@link #storyProcessed()} itself.
     * @param story Story to download and save.
     */
    protected abstract void downloadStory(Story story);

    /**
     * Login to the site using form-based authentication.
     * @param creds A size 2 array containing [Username, Password]. It is assumed that if this is not null, the username
     *              and password strings are both nonnull and nonempty.
     */
    public final void doFormAuth(String[] creds) {
        if (creds == null) return;
        // Try to get site-specific form-data and login URL
        RequestBody formData = getSiteAuthForm(creds[0], creds[1]);
        String loginUrl = getSiteLoginUrl();
        if (formData == null || loginUrl == null) return;

        try {
            Util.logf(C.STARTING_SITE_AUTH_PROCESS, siteName);
            // Log in.
            Response resp = C.getHttpClient().newCall(new Request.Builder().post(formData).url(loginUrl).build())
                             .execute();
            // Make sure we close the response body so that it doesn't leak.
            resp.body().close();
            // Make sure that login cookies were sent back.
            if (resp.headers().values("Set-Cookie").isEmpty()) throw new IOException();
            Util.log(C.DONE);
        } catch (IOException e) {
            Util.logf(C.LOGIN_FAILED, siteName);
        }
    }

    /**
     * Individual site downloaders should override this if they support form-based authentication, returning an OkHttp
     * RequestBody built using an OkHttp FormEncodingBuilder.
     * @param u Username.
     * @param p Password.
     * @return RequestBody with form data, or null.
     */
    protected RequestBody getSiteAuthForm(String u, String p) {
        return null;
    }

    /**
     * Individual site downloaders should override this if they require a login URL.
     * @return Login URL, or null.
     */
    protected String getSiteLoginUrl() {
        return null;
    }

    /**
     * Called each time a story has finished being processed (either has finished downloading or has failed to be
     * downloaded).
     */
    protected final void storyProcessed() {
        fictionDL.incrProgress();
    }

    /**
     * Get the human-readable name for this downloader's site.
     * @return Site name.
     */
    public String getSiteName() {
        return siteName;
    }
}
