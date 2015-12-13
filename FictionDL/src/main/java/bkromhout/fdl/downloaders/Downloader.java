package bkromhout.fdl.downloaders;

import bkromhout.fdl.C;
import bkromhout.fdl.FictionDL;
import bkromhout.fdl.Main;
import bkromhout.fdl.Util;
import bkromhout.fdl.storys.Story;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import org.apache.commons.lang3.reflect.ConstructorUtils;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.ArrayList;
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
     * OkHttpClient for downloading things.
     */
    protected OkHttpClient httpClient;
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
        init();
    }

    /**
     * Initialize this Downloader.
     */
    private void init() {
        // Set up OkHttpClient.
        httpClient = new OkHttpClient();
        httpClient.setCookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
        // TODO may want to bump the number of connections? Not sure.
    }

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
            // Get cookies from the login page first.
            httpClient.newCall(new Request.Builder().url(loginUrl).build()).execute();
            // Then log in.
            Response loginResponse = httpClient.newCall(new Request.Builder().post(formData).url(loginUrl).build())
                                               .execute();
            // Make sure that login cookies were sent back.
            if (loginResponse.headers().values("Set-Cookie").isEmpty()) throw new IOException();
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
     * Download the stories whose URLs were passed to this instance of the downloader upon creation.
     */
    public final void download() {
        // Pre-download logging.
        Util.logf(C.STARTING_SITE_DL_PROCESS, siteName);
        Util.log(extraPreDlMsgs); // This is null unless a subclass has set it to something.
        Util.logf(C.FETCH_BUILD_MODELS, siteName);
        // Create story models from URLs.
        ArrayList<Story> stories = new ArrayList<>();
        for (String url : storyUrls) {
            try {
                // Doing a bit of reflection magic here to construct story classes ;)
                stories.add(ConstructorUtils.invokeConstructor(storyClass, this, url));
            } catch (InvocationTargetException e) {
                storyProcessed(); // Call this, since we have "processed" a story by failing to download it.
                // Now figure out what the heck to put in the log.
                if (e.getCause() == null) e.printStackTrace();
                else if (e.getCause().getMessage() == null) e.getCause().printStackTrace();
                else Util.log(e.getCause().getMessage());
            } catch (ReflectiveOperationException e) {
                // Shouldn't hit this at all.
                e.printStackTrace();
                Main.exit(1);
            }
        }
        // Download and save the stories.
        Util.logf(C.DL_STORIES_FROM_SITE, siteName);
        stories.forEach(this::downloadStory);
        // Post-download logging.
        Util.logf(C.FINISHED_WITH_SITE, siteName);
    }

    /**
     * Download a story.
     * @param story Story to download and save.
     */
    protected abstract void downloadStory(Story story);

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
