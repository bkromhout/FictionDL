package bkromhout.fdl.downloaders;

import bkromhout.fdl.site.Sites;
import bkromhout.fdl.util.C;

/**
 * Downloader for <a href="http://archiveofourown.org/">Ao3</a> stories.
 */
public class Ao3DL extends EpubDL {

    /**
     * Create a new {@link Ao3DL}.
     */
    public Ao3DL() {
        // Yes, for Ao3 this really is all that we have to do :)
        super(Sites.AO3());
        // We have extra messages that we'll want printed before the download process starts.
        this.extraPreDlMsgs = C.AO3_PRE_DL;
    }
}
