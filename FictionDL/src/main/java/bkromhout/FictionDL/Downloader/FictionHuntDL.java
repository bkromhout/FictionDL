package bkromhout.FictionDL.Downloader;

import bkromhout.FictionDL.C;
import bkromhout.FictionDL.FictionDL;
import bkromhout.FictionDL.Story.FictionHuntStory;
import bkromhout.FictionDL.Story.Story;
import bkromhout.FictionDL.Util;

import java.util.HashSet;

/**
 * Downloader for FictionHunt stories.
 */
public class FictionHuntDL extends ParsingDL {

    /**
     * Create a new FictionHunt downloader.
     * @param fictionDL FictionDL object which owns this downloader.
     * @param urls      List of FictionHunt URLs.
     */
    public FictionHuntDL(FictionDL fictionDL, HashSet<String> urls) {
        super(fictionDL, FictionHuntStory.class, C.NAME_FH, urls, "div.text");
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
