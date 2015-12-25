package bkromhout.fdl.downloaders;

import bkromhout.fdl.*;
import bkromhout.fdl.storys.FanFictionStory;
import bkromhout.fdl.storys.FictionHuntStory;
import bkromhout.fdl.storys.Story;
import bkromhout.fdl.util.C;
import bkromhout.fdl.util.Util;

import java.util.HashSet;

/**
 * Downloader for <a href="http://www.fictionhunt.com">FictionHunt</a> stories.
 */
public class FictionHuntDL extends ParsingDL {

    /**
     * Create a new {@link FictionHuntDL}.
     * @param fictionDL FictionDL object which owns this downloader.
     * @param urls      List of FictionHunt urls.
     */
    public FictionHuntDL(FictionDL fictionDL, HashSet<String> urls) {
        super(fictionDL, FictionHuntStory.class, ESite.FH, urls, "div.text");
    }

    /**
     * Overridden because if this FictionHunt story is still available on FanFiction.net, we'd prefer to download it
     * from there for a number of reasons.
     * <p>
     * If the story isn't available on FanFiction.net anymore, fall back to the regular behavior and parse it from
     * FictionHunt.
     * @param story Story to download.
     * @see FanFictionDL
     */
    @Override
    protected void downloadStory(Story story) {
        if (((FictionHuntStory) story).isOnFfn()) {
            // Story still on FanFiction.net, which is preferable, so we'll add a FFN url so it gets downloaded later.
            Util.logf(C.FH_ON_FFN, story.getTitle());
            FictionDL.getLinkFileParser().addFfnUrl(String.format(FanFictionStory.FFN_S_URL, story.getStoryId()));
        } else {
            // Just do the normal thing.
            super.downloadStory(story);
        }
    }

    /**
     * FictionHunt's chapter content HTML can have some odd stuff which is illegal in ePUB XHTML, so we fix those issues
     * here.
     * @param chapter Chapter object.
     */
    @Override
    protected void sanitizeChap(Chapter chapter) {
        // Ensure that any "noshade" attributes are non-boolean, XHTML doesn't like boolean attributes.
        chapter.content = chapter.content.replace("noshade", "noshade=\"noshade\"");
    }
}
