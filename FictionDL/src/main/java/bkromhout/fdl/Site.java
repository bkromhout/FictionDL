package bkromhout.fdl;

import bkromhout.fdl.downloaders.Downloader;
import bkromhout.fdl.parsers.ConfigFileParser;
import bkromhout.fdl.storys.Story;
import org.apache.commons.lang3.reflect.ConstructorUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;

/**
 * Represents a supported site.
 * @see bkromhout.fdl.util.Sites
 */
public class Site {
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
     * List of story urls for this site.
     */
    private HashSet<String> urls;

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
        this.urls = new HashSet<>();
    }

    /**
     * Starts the download process for this site. This is a no-op if there are no urls in this site's url list.
     * @param fictionDL Instance of {@link FictionDL} to use when creating this site's {@link Downloader} class.
     * @param config    Options parsed from the config file, in case this site needs them.
     */
    public void download(FictionDL fictionDL, ConfigFileParser.Config config) {
        if (urls.isEmpty()) return;
        try {
            // Create the downloader class.
            Downloader downloader = ConstructorUtils.invokeConstructor(dlClass, fictionDL);
            // Do site auth if it supports it and we have credentials for it.
            if (supportsAuth && config.hasCreds(this)) downloader.doFormAuth(config.getCreds(this));
            // Download stories from site.
            downloader.download();
        } catch (InvocationTargetException e) {
            // Not really sure why we'd ever hit this. TODO find out.
            e.printStackTrace();
            Main.exit(1);
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
     * Get this site's list of story urls.
     * @return Story url list.
     */
    public HashSet<String> getUrls() {
        return urls;
    }
}
