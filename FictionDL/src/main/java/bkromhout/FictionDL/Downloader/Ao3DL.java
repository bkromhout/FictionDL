package bkromhout.FictionDL.Downloader;

import bkromhout.FictionDL.C;
import bkromhout.FictionDL.FictionDL;
import bkromhout.FictionDL.Story.Ao3Story;

import java.util.HashSet;

/**
 * Downloader for Ao3 stories.
 */
public class Ao3DL extends EpubDL {

    /**
     * Create a new Ao3 downloader.
     * @param fictionDL FictionDL object which owns this downloader.
     * @param urls      List of Ao3 URLs.
     */
    public Ao3DL(FictionDL fictionDL, HashSet<String> urls) {
        // Yes, for Ao3 this really is all that we have to do :)
        super(fictionDL, Ao3Story.class, C.NAME_AO3, urls);
    }
}
