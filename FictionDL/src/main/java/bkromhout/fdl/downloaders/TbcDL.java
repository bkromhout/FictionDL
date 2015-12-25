package bkromhout.fdl.downloaders;

import bkromhout.fdl.FictionDL;
import bkromhout.fdl.Site;
import bkromhout.fdl.storys.TbcStory;

import java.util.HashSet;

/**
 * Downloader for <a href="http://thebroomcupboard.net">The Broom Cupboard</a> stories.
 */
public class TbcDL extends ParsingDL {
    /**
     * The Broom Cupboard login page url.
     */
    public static final String TBC_L_URL = "http://thebroomcupboard.net/processlogin.php?cid=";

    /**
     * Create a new {@link TbcDL}.
     * @param fictionDL FictionDL object which owns this downloader.
     * @param urls      List of Broom Cupboard urls.
     */
    public TbcDL(FictionDL fictionDL, HashSet<String> urls) {
        super(fictionDL, TbcStory.class, Site.TBC, urls, );
    }
}
