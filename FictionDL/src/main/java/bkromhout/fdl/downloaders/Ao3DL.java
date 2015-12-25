package bkromhout.fdl.downloaders;

import bkromhout.fdl.ESite;
import bkromhout.fdl.util.C;
import bkromhout.fdl.FictionDL;
import bkromhout.fdl.storys.Ao3Story;

import java.util.HashSet;

/**
 * Downloader for <a href="http://archiveofourown.org/">Ao3</a> stories.
 */
public class Ao3DL extends EpubDL {

    /**
     * Create a new {@link Ao3DL}.
     * @param fictionDL FictionDL object which owns this downloader.
     * @param urls      List of Ao3 urls.
     */
    public Ao3DL(FictionDL fictionDL, HashSet<String> urls) {
        // Yes, for Ao3 this really is all that we have to do :)
        super(fictionDL, Ao3Story.class, ESite.AO3, urls);
        // We have extra messages that we'll want printed before the download process starts.
        this.extraPreDlMsgs = C.AO3_PRE_DL;
    }
}
