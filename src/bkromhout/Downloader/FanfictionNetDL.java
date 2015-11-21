package bkromhout.Downloader;

import bkromhout.C;

import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Downloader for Fanfiction.net. Uses http://www.p0ody-files.com/ff_to_ebook/ for a number of reasons, not the least of
 * which is because I have no desire to reinvent the wheel.
 */
public class FanfictionNetDL {
    public static final String SITE = "Fanfiction.net";
    // Fanfiction.net URLs. May be null if this instance of the downloaded is being used by another site's downloader.
    private ArrayList<String> urls;

    /**
     * Create a new Fanfiction.net downloader to use for downloading single stories at a time. Probably only used by
     * downloaders for other sites.
     */
    public FanfictionNetDL() {
        this(null);
    }

    /**
     * Create a new Fanfiction.net downloader. This downloader is basically a shim which used p0ody-files, so it's
     * minimal in its code.
     * @param urls List of Fanfiction.net URLs to download stories for.
     */
    public FanfictionNetDL(ArrayList<String> urls) {
        this.urls = urls;
    }

    /**
     * Download the stories whose URLs were passed to this instance of the downloader upon creation.
     */
    public void download() {
        // If we don't have any URLs, don't do anything.
        if (urls == null || urls.isEmpty()) {
            // Only speak up if, for some very odd reason, this gets called when urls == null.
            if (urls == null) System.out.println(C.NO_URLS_FFNDL);
            return;
        }
        System.out.printf(C.STARTING_SITE_DL_PROCESS, SITE);
        // Convert FFN story URLs to FFN story IDs.
        ArrayList<String> storyIds = new ArrayList<>(
                urls.stream().map(this::storyIdFromFfnUrl).collect(Collectors.toCollection(ArrayList::new)));
        // Then download stories from p0ody-files.
        System.out.printf(C.DL_STORIES_FROM_SITE, "Fanfiction.net (through p0ody-files.com/ff_to_ebook/)");
        storyIds.forEach(this::downloadByStoryId);
    }

    /**
     * Download a story based on its ID.
     * @param storyId The ID of the story.
     */
    public void downloadByStoryId(String storyId) {
        //TODO:this
    }

    /**
     * Takes a Fanfiction.net URL and extracts the story ID from it. Assumes that the URL string given is a valid
     * Fanfiction.net story URL.
     * @param ffnUrl A valid Fanfiction.net story URL.
     * @return The story ID from the given URL.
     */
    private String storyIdFromFfnUrl(String ffnUrl) {
        return Pattern.compile(C.ffnRegex).matcher(ffnUrl).group(4);
    }
}
