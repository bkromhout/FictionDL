package bkromhout.fdl.site;

import bkromhout.fdl.downloaders.*;
import bkromhout.fdl.stories.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * {@link Site} utility class.
 * <p>
 * Provides access to the various supported sites' {@link Site} instances, as well as providing the ability to iterate
 * over all of them at once.
 * <p>
 * All supported sites' {@link Site} instances should be initialized in this class' {@link #init()} function.
 */
public class Sites {
    /**
     * List which holds all {@link Site}s.
     */
    private static ArrayList<Site> all;
    /**
     * Supported {@link Site}s.
     */
    private static Site FH, FFN, SIYE, MN, AO3, TBC, WP;

    /**
     * Create all {@link Site} classes and init this class. Be careful not to call this more than once per {@link
     * bkromhout.fdl.FictionDL} run.
     */
    public static void init() {
        all = new ArrayList<>();
        // Create sites and add them to the list of sites.
        all.add(FH = new Site("FictionHunt", "fictionhunt.com", FictionHuntDL.class, FictionHuntStory.class));
        all.add(FFN = new Site("FanFiction.net", "fanfiction.net", FanFictionDL.class, FanFictionStory.class));
        all.add(SIYE = new Site("SIYE", "siye.co.uk", SiyeDL.class, SiyeStory.class));
        all.add(MN = new Site("MuggleNet", "fanfiction.mugglenet.com", MuggleNetDL.class, MuggleNetStory.class, true));
        all.add(AO3 = new Site("Ao3", "archiveofourown.org", Ao3DL.class, Ao3Story.class));
        all.add(TBC = new Site("The Broom Cupboard", "thebroomcupboard.net", TbcDL.class, TbcStory.class, true));
        all.add(WP = new Site("Wattpad", "wattpad.com", WattpadDL.class, WattpadStory.class));
    }

    /**
     * Get an immutable list of all supported {@link Site}s.
     * @return Immutable list of {@link Site}s.
     */
    public static List<Site> all() {
        return Collections.unmodifiableList(all);
    }

    public static Site FFN() {
        return FFN;
    }

    public static Site FH() {
        return FH;
    }

    public static Site SIYE() {
        return SIYE;
    }

    public static Site MN() {
        return MN;
    }

    public static Site AO3() {
        return AO3;
    }

    public static Site TBC() {
        return TBC;
    }

    public static Site WP() {
        return WP;
    }
}
