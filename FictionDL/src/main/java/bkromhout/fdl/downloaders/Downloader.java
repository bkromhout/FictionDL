package bkromhout.fdl.downloaders;

import bkromhout.fdl.C;
import bkromhout.fdl.FictionDL;
import bkromhout.fdl.Site;
import bkromhout.fdl.Util;
import bkromhout.fdl.rx.RxMakeStories;
import bkromhout.fdl.storys.Story;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import rx.Observable;
import rx.schedulers.Schedulers;

import java.io.IOException;
import java.util.HashSet;

/**
 * Base for all downloader classes.
 * <p>
 * This class is meant to be a very broad base, and should be extended to create more specific "base" classes that
 * site-specific downloaders can extend. For example, sites where we need to parse raw HTML to generate stories should
 * subclass {@link ParsingDL}, which is a subclass of this class and provides logic that is common to any site
 * downloader which has to download and parse HTML.
 */
public abstract class Downloader {
    /**
     * The FictionDL instance which owns this Downloader.
     */
    protected FictionDL fictionDL;
    /**
     * This is the specific {@link Story} subclass whose constructor will be called which creating stories.
     */
    protected Class<? extends Story> storyClass;
    /**
     * {@link bkromhout.fdl.Site} that this downloader services.
     */
    protected Site site;
    /**
     * Story urls.
     */
    protected HashSet<String> storyUrls;
    /**
     * Any extra messages to print prior to starting the download process. Can be set by a subclass at some point after
     * initialization.
     */
    protected String extraPreDlMsgs;

    /**
     * Create a new Downloader.
     * @param fictionDL  FictionDL object which owns this downloader.
     * @param storyClass The class of Story which this downloader uses.
     * @param site       Site that this downloader services.
     * @param storyUrls  Set of story urls to be downloaded.
     */
    protected Downloader(FictionDL fictionDL, Class<? extends Story> storyClass, Site site, HashSet<String> storyUrls) {
        this.fictionDL = fictionDL;
        this.storyClass = storyClass;
        this.site = site;
        this.storyUrls = storyUrls;

    }

    /**
     * Download the stories whose urls were passed to this downloader upon creation.
     */
    public final void download() {
        // Pre-download logging.
        Util.logf(C.STARTING_SITE_DL_PROCESS, site.getName());
        Util.log(extraPreDlMsgs); // This is null unless a subclass has set it to something.
        Util.logf(C.FETCH_BUILD_MODELS, site.getName());

        // Use RxJava to handle the logic.
        Observable.from(storyUrls)
                  .subscribeOn(Schedulers.computation())
                  .compose(new RxMakeStories(storyClass, this))
                  .doOnNext(story -> {
                      if (story == null) storyProcessed();
                  })
                  .filter(story -> story != null) // Get rid of failed stories.
                  .toList()
                  .doOnCompleted(() -> Util.logf(C.DL_STORIES_FROM_SITE, site.getName()))
                  .observeOn(Schedulers.immediate())
                  .toBlocking().single() // Put all of the stories into a List.
                  // Download the stories. (Note that this is the JDK 8 Iterable.forEach() method, because we want
                  // our RxJava flow to finish creating the story models before we download any of them.)
                  .forEach(this::downloadStory);

        //Post-download logging.
        Util.logf(C.FINISHED_WITH_SITE, site.getName());
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
        // Try to get site-specific form-data and login url
        RequestBody formData = getSiteAuthForm(creds[0], creds[1]);
        String loginUrl = getSiteLoginUrl();
        if (formData == null || loginUrl == null) return;

        try {
            Util.logf(C.STARTING_SITE_AUTH_PROCESS, site.getName());
            // Log in.
            Response resp = C.getHttpClient().newCall(new Request.Builder().post(formData).url(loginUrl).build())
                             .execute();
            // Make sure we close the response body so that it doesn't leak.
            resp.body().close();
            // Make sure that login cookies were sent back.
            if (resp.headers().values("Set-Cookie").isEmpty()) throw new IOException();
            Util.log(C.DONE);
        } catch (IOException e) {
            Util.logf(C.LOGIN_FAILED, site.getName());
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
     * Individual site downloaders should override this if they require a login url.
     * @return Login url, or null.
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
}
