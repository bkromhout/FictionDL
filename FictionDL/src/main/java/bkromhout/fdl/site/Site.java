package bkromhout.fdl.site;

import bkromhout.fdl.Main;
import bkromhout.fdl.downloaders.Downloader;
import bkromhout.fdl.parsing.ConfigFileParser;
import bkromhout.fdl.parsing.StoryEntry;
import bkromhout.fdl.storys.Story;
import bkromhout.fdl.util.IWorkProducer;

import java.util.HashSet;

/**
 * Represents a supported site, and is in charge of knowing what to call to initiate a site's download process.
 * <p>
 * All instances of this class should be created in the {@link Sites#init()} function to ensure that the methods in the
 * {@link Sites} class will operate on every supported site's implementation.
 * @see Sites
 */
public final class Site implements IWorkProducer {
    /**
     * Human-readable name for this site.
     */
    private final String name;
    /**
     * Base host domain for this site.
     */
    private final String host;
    /**
     * {@link Downloader} class for this site.
     */
    private final Class<? extends Downloader> dlClass;
    /**
     * {@link Story} class for this site.
     */
    private final Class<? extends Story> storyClass;
    /**
     * Whether or not this site supports authentication.
     */
    private final boolean supportsAuth;
    /**
     * List of story entries to download for this site.
     */
    private final HashSet<StoryEntry> storyEntries;

    /**
     * Create a new {@link Site}.
     * @param name       Human-readable site name.
     * @param host       Base host domain.
     * @param dlClass    Specific {@link Downloader} class for this site.
     * @param storyClass Specific {@link Story} class for this site.
     */
    public Site(String name, String host, Class<? extends Downloader> dlClass, Class<? extends Story> storyClass) {
        this(name, host, dlClass, storyClass, false);
    }

    /**
     * Create a new {@link Site}.
     * @param name         Human-readable site name.
     * @param host         Base host domain.
     * @param dlClass      Specific {@link Downloader} class for this site.
     * @param storyClass   Specific {@link Story} class for this site.
     * @param supportsAuth Whether or not this site support authentication.
     */
    public Site(String name, String host, Class<? extends Downloader> dlClass, Class<? extends Story> storyClass,
                boolean supportsAuth) {
        this.name = name;
        this.host = host;
        this.dlClass = dlClass;
        this.storyClass = storyClass;
        this.supportsAuth = supportsAuth;
        this.storyEntries = new HashSet<>();
    }

    /**
     * Starts the download process for this site. This is a no-op if there are no urls in this site's url list.
     * @param config Options parsed from the config file, in case this site needs them.
     */
    public void process(ConfigFileParser.Config config) {
        if (storyEntries.isEmpty()) return;
        try {
            // Create the downloader class.
            Downloader downloader = dlClass.getConstructor().newInstance();
            // Do site auth if it supports it and we have credentials for it.
            if (supportsAuth && config.hasCreds(this)) downloader.doFormAuth(config.getCreds(this));
            // Download stories from site.
            downloader.download();
        } catch (ReflectiveOperationException e) {
            // What a terrible failure.
            e.printStackTrace();
            Main.exit(1);
        }
    }

    /**
     * Get the human-readable site name.
     * @return Site name.
     */
    public String getName() {
        return name;
    }

    /**
     * Get the site's base domain.
     * @return Site domain.
     */
    public String getHost() {
        return host;
    }

    /**
     * Get the {@link Story} class for this site.
     * @return Site-specific {@link Story} class.
     */
    public Class<? extends Story> getStoryClass() {
        return storyClass;
    }

    /**
     * Get this site's list of story entries.
     * @return Story entry list.
     */
    public HashSet<StoryEntry> getStoryEntries() {
        return storyEntries;
    }

    @Override
    public int getWorkCount() {
        return storyEntries.size();
    }
}
