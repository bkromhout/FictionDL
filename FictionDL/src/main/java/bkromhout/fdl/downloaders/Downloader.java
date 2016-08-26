package bkromhout.fdl.downloaders;

import bkromhout.fdl.parsing.StoryEntry;
import bkromhout.fdl.rx.RxMakeStories;
import bkromhout.fdl.site.Site;
import bkromhout.fdl.storys.Story;
import bkromhout.fdl.util.C;
import bkromhout.fdl.util.ProgressHelper;
import bkromhout.fdl.util.Util;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import rx.Observable;
import rx.schedulers.Schedulers;

import java.io.IOException;
import java.util.ArrayList;
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
     * This is the specific {@link Story} subclass whose constructor will be called which creating stories.
     */
    private final Class<? extends Story> storyClass;
    /**
     * {@link Site} that this downloader services.
     */
    private final Site site;
    /**
     * Story urls.
     */
    private final HashSet<StoryEntry> storyEntries;
    /**
     * Any extra messages to print prior to starting the download process. Can be set by a subclass at some point after
     * initialization.
     */
    String extraPreDlMsgs;

    /**
     * Create a new {@link Downloader}.
     * @param site Site that this downloader services.
     */
    Downloader(Site site) {
        this.site = site;
        this.storyClass = site.getStoryClass();
        this.storyEntries = site.getStoryEntries();
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
        ArrayList<Story> stories = (ArrayList<Story>) Observable
                .from(storyEntries)
                .subscribeOn(Schedulers.computation())
                .compose(new RxMakeStories(storyClass))
                .doOnNext(story -> {
                    // If a story failed, we just add one completed work unit.
                    if (story == null) ProgressHelper.storyFailed(0L);
                        // Otherwise, update the total work count by adding the number of chapters that will be
                        // downloaded for this story.
                    else ProgressHelper.recalcUnitWorth(story.getChapterUrlCount());
                })
                .filter(story -> story != null) // Get rid of failed stories.
                .toList()
                .observeOn(Schedulers.immediate())
                .toBlocking().single(); // Put all of the stories into a List.

        // Download the stories. (Note that this is the JDK 8 Iterable.forEach() method, because we want our RxJava
        // flow to finish creating the story models before we download any of them.)
        if (!stories.isEmpty()) {
            // In the case where we no longer have any stories because they all failed before now, we don't want to log.
            Util.logf(C.DL_STORIES_FROM_SITE, site.getName());
            stories.forEach(this::downloadStory);
        }

        //Post-download logging.
        Util.logf(C.FINISHED_WITH_SITE, site.getName());
    }

    /**
     * Download a story.
     * <p>
     * <u>For subclasses which choose to override this method</u>:<br/>Make sure that the appropriate static methods in
     * {@link ProgressHelper} are called as needed.
     * @param story Story to download and save.
     */
    protected abstract void downloadStory(Story story);

    /**
     * Log in to the site using form-based authentication.
     * @param creds An array containing ["Username", "Password"]. It is assumed that if this is non-null, the username
     *              and password strings are both non-null and non-empty.
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
            // Make the ResponseBody is closed so that it doesn't leak.
            resp.body().close();
            // Make sure that login cookies were sent back.
            if (resp.headers().values("Set-Cookie").isEmpty())
                // (Also check prior response headers, for sites which redirect after auth completes.)
                if (resp.priorResponse() == null || resp.priorResponse().headers().values("Set-Cookie").isEmpty())
                    throw new IOException();
            Util.log(C.DONE);
        } catch (IOException e) {
            Util.logf(C.LOGIN_FAILED, site.getName());
        }
    }

    /**
     * Individual site downloaders should override this if they support form-based authentication.
     * @param u Username.
     * @param p Password.
     * @return RequestBody with form data, or null.
     */
    RequestBody getSiteAuthForm(String u, String p) {
        return null;
    }

    /**
     * Individual site downloaders should override this to supply a login url.
     * @return Login url, or null.
     */
    String getSiteLoginUrl() {
        return null;
    }
}
