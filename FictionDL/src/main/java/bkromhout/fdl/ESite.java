package bkromhout.fdl;

/**
 * Represents a supported site.
 */
public enum ESite {
    /* Sites. Remember */
    FFN("FanFiction.net", "fanfiction.net"),
    FH("FictionHunt", "fictionhunt.com"),
    SIYE("SIYE", "siye.co.uk"),
    MN("MuggleNet", "fanfiction.mugglenet.com"),
    AO3("Ao3", "archiveofourown.org"),
    TBC("The Broom Cupboard", "thebroomcupboard.net");

    private final String name;
    private final String host;

    ESite(String name, String host) {
        this.name = name;
        this.host = host;
    }

    /**
     * Human-readable name for this site.
     * @return Site name.
     */
    public String getName() {
        return name;
    }

    /**
     * Base host domain for this site.
     * @return Site host.
     */
    public String getHost() {
        return host;
    }
}
