package bkromhout.FictionDL.Downloader;

import bkromhout.FictionDL.C;
import bkromhout.FictionDL.FictionDL;
import bkromhout.FictionDL.Story.FictionHuntStory;
import bkromhout.FictionDL.Story.Story;
import bkromhout.FictionDL.Util;
import bkromhout.FictionDL.ex.InitStoryException;

import java.util.ArrayList;

/**
 * Downloader for FictionHunt stories.
 */
public class FictionHuntDL extends ParsingDL {
    public static final String SITE = "FictionHunt";

    /**
     * Create a new FictionHunt downloader.
     * @param fictionDL FictionDL object which owns this downloader.
     * @param urls      List of FictionHunt URLs.
     */
    public FictionHuntDL(FictionDL fictionDL, ArrayList<String> urls) {
        super(fictionDL, urls, "div.text");
    }

    /**
     * Download the stories whose URLs were passed to this instance of the downloader upon creation.
     */
    @Override
    public void download() {
        Util.logf(C.STARTING_SITE_DL_PROCESS, SITE);
        // Create story models from URLs.
        Util.logf(C.FETCH_BUILD_MODELS, SITE);
        ArrayList<FictionHuntStory> stories = new ArrayList<>();
        for (String url : storyUrls) {
            try {
                stories.add(new FictionHuntStory(url));
            } catch (InitStoryException e) {
                storyProcessed(); // Call this, since we have "processed" a story by failing to download it.
                Util.log(e.getMessage());
            }
        }
        // Download and save the stories.
        Util.logf(C.DL_STORIES_FROM_SITE, SITE);
        stories.forEach(this::downloadStory);
    }

    /**
     * Overridden because if this FictionHunt story is still available on FanFiction.net, we'd prefer to download it
     * from there for a number of reasons. If the story isn't available on FanFiction.net anymore, fall back to the
     * regular behavior and parse it from FictionHunt.
     * @param story Story to download.
     */
    @Override
    protected void downloadStory(Story story) {
        if (((FictionHuntStory) story).isOnFfn()) {
            // Story still on FanFiction.net, which is preferable, so we'll add a FFN URL so it gets downloaded later.
            Util.logf(C.FH_STORY_ON_FFN, story.getTitle());
            FictionDL.parser.addFfnUrl(String.format(C.FFN_S_URL, story.getStoryId()));
            // It isn't appropriate to call .storyProcessed() here since FanFictionDL will download the story and
            // call it later.
        } else {
            // Just do the normal thing.
            super.downloadStory(story);
        }
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
        chapterString = chapterString.replace("noshade", "noshade=\"noshade\"");
        return chapterString;
    }
}
