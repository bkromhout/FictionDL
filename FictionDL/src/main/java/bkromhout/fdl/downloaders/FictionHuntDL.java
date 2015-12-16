package bkromhout.fdl.downloaders;

import bkromhout.fdl.C;
import bkromhout.fdl.Chapter;
import bkromhout.fdl.FictionDL;
import bkromhout.fdl.Util;
import bkromhout.fdl.storys.FanFictionStory;
import bkromhout.fdl.storys.FictionHuntStory;
import bkromhout.fdl.storys.Story;
import rx.functions.Action1;

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
            Util.logf(C.FH_ON_FFN, story.getTitle());
            FictionDL.parser.addFfnUrl(String.format(FanFictionStory.FFN_S_URL, story.getStoryId()));
            // It isn't appropriate to call .storyProcessed() here since FanFictionDL will download the story and
            // call it later.
        } else {
            // Just do the normal thing.
            super.downloadStory(story);
        }
    }

    /**
     * Creates an action which takes a Chapter objects and cleans the String in the {@link Chapter#content content}
     * field.
     * <p>
     * FictionHunt's chapter content HTML can have some odd stuff which is illegal in ePUB XHTML.
     * @return An action which cleans the string in the {@link Chapter#content content} field of the given Chapter.
     * @see Chapter
     */
    @Override
    protected Action1<? super Chapter> sanitizeChap() {
        return chapter -> chapter.content = chapter.content.replace("noshade", "noshade=\"noshade\"");
    }
}
