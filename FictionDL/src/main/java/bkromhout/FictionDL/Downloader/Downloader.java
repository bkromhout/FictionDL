package bkromhout.FictionDL.Downloader;

import bkromhout.FictionDL.C;
import bkromhout.FictionDL.FictionDL;
import bkromhout.FictionDL.Main;
import bkromhout.FictionDL.Story.Story;
import bkromhout.FictionDL.Util;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Base downloader class. This shouldn't be extended by site-specific downloaders, but rather by classes which provide
 * additional categories of functionality to a group of sites. For example, sites where we parse and generate stories
 * should subclass ParsingDL, which itself is a subclass of this class.
 */
public abstract class Downloader {
    /**
     * This is the class of story which this Downloader interacts with. Must extend Story.
     */
    protected Class<? extends Story> storyClass;
    /**
     * The FictionDL instance which owns this downloader.
     */
    protected FictionDL fictionDL;
    /**
     * Human-readable site name for this downloader.
     */
    protected String siteName;
    /**
     * Story URLs.
     */
    protected HashSet<String> storyUrls;
    /**
     * Cookies to send with every request this downloader makes. Empty by default.
     */
    protected HashMap<String, String> cookies = new HashMap<>();

    /**
     * Create a new Downloader.
     * @param fictionDL  FictionDL object which owns this downloader.
     * @param storyClass The class of Story which this downloader uses.
     * @param siteName   Human-readable site name for this downloader.
     * @param storyUrls  Set of story URLs to be downloaded.
     */
    public Downloader(FictionDL fictionDL, Class<? extends Story> storyClass, String siteName,
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
        printPreDlMsgs();
        // Create story models from URLs.
        ArrayList<Story> stories = new ArrayList<>();
        for (String url : storyUrls) {
            try {
                // Doing a bit of reflection magic here to construct story classes ;)
                stories.add(storyClass.getConstructor(Downloader.class, String.class).newInstance(this, url));
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
        printPostDlMsgs();
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
     * Prints log messages, first call of .download().
     */
    protected void printPreDlMsgs() {
        Util.logf(C.STARTING_SITE_DL_PROCESS, siteName);
        Util.logf(C.FETCH_BUILD_MODELS, siteName);
    }

    /**
     * Prints log messages, last call of .download().
     */
    protected void printPostDlMsgs() {
        Util.logf(C.FINISHED_WITH_SITE, siteName);
    }

    /**
     * Get the human-readable name for this downloader's site.
     * @return Site name.
     */
    public String getSiteName() {
        return siteName;
    }

    /**
     * Get the cookies to send with any request to this downloader's site.
     * @return Cookies.
     */
    public HashMap<String, String> getCookies() {
        return cookies;
    }
}
