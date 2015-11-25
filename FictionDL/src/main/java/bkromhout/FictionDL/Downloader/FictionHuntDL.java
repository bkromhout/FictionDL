package bkromhout.FictionDL.Downloader;

import bkromhout.FictionDL.*;
import bkromhout.FictionDL.Story.FictionHuntStory;
import bkromhout.FictionDL.Story.Story;
import org.jsoup.nodes.Document;

import java.io.*;
import java.util.ArrayList;

/**
 * Downloader for FictionHunt stories.
 */
public class FictionHuntDL extends ParsingDL {
    public static final String SITE = "FictionHunt";
    // List of story URLs
    private ArrayList<String> urls;
    // Instance of an FFN downloader, created the first time it's needed, to easily download FFN stories.
    private FanfictionNetDL ffnDownloader = null;

    /**
     * Create a new FictionHunt downloader.
     * @param urls List of FictionHunt URLs.
     */
    public FictionHuntDL(ArrayList<String> urls) {
        this.urls = urls;
        this.chapTextSelector = "div.text";
    }

    /**
     * Download the stories whose URLs were passed to this instance of the downloader upon creation.
     */
    public void download() {
        System.out.printf(C.STARTING_SITE_DL_PROCESS, SITE);
        // Create story models from URLs.
        System.out.printf(C.FETCH_BUILD_MODELS, SITE);
        ArrayList<FictionHuntStory> stories = new ArrayList<>();
        for (String url : urls) {
            try {
                stories.add(new FictionHuntStory(url));
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
        // Download and save the stories.
        System.out.printf(C.DL_STORIES_FROM_SITE, SITE);
        stories.forEach(this::downloadStory);
    }

    /**
     * Download the chapters of a story. If the story is still active on Fanfiction.net, an ePUB file will be downloaded
     * from p0ody-files. If not, it will be downloaded from FictionHunt by scraping and sanitizing the chapters.
     * @param story Story to download.
     */
    @Override
    protected void downloadStory(Story story) {
        System.out.printf(C.DL_CHAPS_FOR, story.getTitle());
        if (((FictionHuntStory) story).getFfnStoryId() != null) {
            // Story is still on Fanfiction.net, which is preferable since we can use p0ody-files to download the ePUB.
            if (ffnDownloader == null) ffnDownloader = new FanfictionNetDL(); // Get a FFN downloader instance.
            System.out.printf(C.FH_STORY_ON_FFN, story.getTitle());
            ffnDownloader.downloadByStoryId(((FictionHuntStory) story).getFfnStoryId(), story.getTitle());
        } else {
            // Just do the normal thing.
            super.downloadStory(story);
        }
    }

    /**
     * Download the chapters for a story.
     * @param story Story to download chapters for.
     * @return ArrayList of Chapter objects.
     */
    @Override
    protected ArrayList<Chapter> downloadChapters(Story story) {
        ArrayList<Chapter> chapters = super.downloadChapters(story);
        // Generate chapter titles generically.
        for (int i = 0; i < chapters.size(); i++) chapters.get(i).title = String.format("Chapter %d", i + 1);
        return chapters;
    }


    /**
     * Takes chapter HTML from a FictionHunt chapter and cleans it up, before putting it into the xhtml format required
     * for an ePUB.
     * @param chapterString Chapter's text content HTML for a FictionHunt story chapter.
     * @return Cleaned HTML.
     */
    @Override
    protected String sanitizeChapter(String chapterString) {
        // Do some FictionHunt specific cleaning.
        return chapterString.replaceAll("<hr size=\"1\" noshade>", "<hr size=\"1\" noshade=\"noshade\" />");
    }
}
