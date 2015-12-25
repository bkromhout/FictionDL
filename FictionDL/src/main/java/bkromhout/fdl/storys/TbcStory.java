package bkromhout.fdl.storys;

import bkromhout.fdl.Site;
import bkromhout.fdl.downloaders.ParsingDL;
import bkromhout.fdl.ex.InitStoryException;

/**
 * Model object for a <a href="http://thebroomcupboard.net">The Broom Cupboard</a> story.
 */
public class TbcStory extends Story {

    /**
     * Create a new {@link TbcStory} based off of a url.
     * @param ownerDl The parsing downloader which owns this story.
     * @param url     url of the story this model represents.
     * @throws InitStoryException if we can't create this story object for some reason.
     */
    public TbcStory(ParsingDL ownerDl, String url) throws InitStoryException {
        super(ownerDl, url, Site.TBC);
    }

    @Override
    protected void populateInfo() throws InitStoryException {

    }
}
