package bkromhout.Downloader;

import bkromhout.C;
import bkromhout.Main;
import bkromhout.Story.SiyeStory;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Downloader for siye.co.uk ("Sink Into Your Eyes").
 */
public class SiyeDL {
    public static final String SITE = "SIYE";
    // List of SIYE URLs.
    ArrayList<String> urls;

    /**
     * Create a new SIYE downloader.
     * @param urls List of SIYE URLs.
     */
    public SiyeDL(ArrayList<String> urls) {
        this.urls = urls;
    }

    /**
     * Download the stories whose URLs were passed to this instance of the downloader upon creation..
     */
    public void download() {
        // Do nothing if we have no URLs.
        if (urls.isEmpty()) return;
        System.out.printf(C.STARTING_SITE_DL_PROCESS, SITE);
        // Create story models from URLs.
        System.out.printf(C.FETCH_BUILD_MODELS, SITE);
        ArrayList<SiyeStory> stories = new ArrayList<>();
        for (String url : urls) {
            try {
                stories.add(new SiyeStory(url));
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }
        // Download and save the stories.
        System.out.printf(C.DL_STORIES_FROM_SITE, SITE);

    }

    /**
     * Download chapters of a story.
     * @param story The story to download.
     */
    private void downloadStory(SiyeStory story) {
        // Download the whole story text.
        System.out.printf(C.DL_CHAPS_FOR, story.getTitle());
        Document storyDoc = Main.downloadHtml(story.getContentUrl());
        if (storyDoc == null) {
            System.out.println(C.SOME_CHAPS_FAILED);
            return;
        }

    }


}
