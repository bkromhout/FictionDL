package bkromhout.fdl.downloaders;

import bkromhout.fdl.chapter.Chapter;
import bkromhout.fdl.parsing.StoryEntry;
import bkromhout.fdl.site.Sites;
import bkromhout.fdl.stories.FanFictionStory;
import bkromhout.fdl.stories.FictionHuntStory;
import bkromhout.fdl.stories.Story;
import bkromhout.fdl.util.C;
import bkromhout.fdl.util.Util;

/**
 * Downloader for <a href="http://www.fictionhunt.com">FictionHunt</a> stories.
 */
public class FictionHuntDL extends ParsingDL {

    /**
     * Create a new {@link FictionHuntDL}.
     */
    public FictionHuntDL() {
        super(Sites.FH(), "div.text");
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
            StoryEntry ffnStoryEntry = new StoryEntry(String.format(FanFictionStory.FFN_S_URL, story.getStoryId()));
            ffnStoryEntry.addDetailTags(story.getDetailTags());
            Sites.FFN().getStoryEntries().add(ffnStoryEntry);
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
