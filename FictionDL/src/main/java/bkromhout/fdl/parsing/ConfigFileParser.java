package bkromhout.fdl.parsing;

import bkromhout.fdl.site.Site;
import bkromhout.fdl.site.Sites;
import bkromhout.fdl.util.C;
import bkromhout.fdl.util.Util;
import rx.Observable;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses the config file.
 */
public class ConfigFileParser extends FileParser {
    /* Valid Line Starters */
    private static final String CFG_LS_P = "p";
    private static final String CFG_LS_U = "u";
    private static final String CFG_LS_SITE = "site";
    private static final String CFG_LS_HASH = "#";

    // Config instance.
    private Config config;
    // Site name regex.
    private String siteNameRegex;
    // Site which will be given any site-specific preferences.
    private String currSite;

    /**
     * Parses the config file and create a {@link Config}.
     * @param cfgFile Config file.
     */
    public ConfigFileParser(File cfgFile) {
        super(FileType.CONFIG, cfgFile);
    }

    @Override
    protected void init() {
        config = new Config();
        siteNameRegex = buildSitesOrRegex(Sites.all());
    }

    /**
     * Processes a line from the config file, doing different things depending on what the line starts with.
     * <p>
     * Ignores lines if they don't start with a valid line starter or are malformed.
     * @param line Line from the config file.
     */
    @Override
    protected void processLine(String line) {
        // Sometimes we don't care...
        if (line == null || line.isEmpty() || line.startsWith(CFG_LS_HASH) || !line.contains("=") ||
                line.indexOf('=') == line.length() - 1) return;

        // Do different things based on what the line starts with (the option prefix).
        String prefix = line.substring(0, line.indexOf('='));
        switch (prefix) {
            case CFG_LS_SITE: {
                Matcher siteMatcher = Pattern.compile(siteNameRegex).matcher(line); // Ensure valid site.
                // Let's be nice, if a site is misspelled or something, don't potentially overwrite a previous site's
                // info later. If it is a valid site name, then update the current site.
                currSite = siteMatcher.find() ? siteMatcher.group() : null;
                break;
            }
            case CFG_LS_U:
            case CFG_LS_P: {
                // Save a username or password for the current site, unless the current site is unset.
                if (currSite != null) config.options.put(currSite + prefix, line.substring(line.indexOf('=') + 1));
                break;
            }
            default: {
                Util.loudf(C.PROCESS_LINE_FAILED, type, line);
            }
        }
    }

    /**
     * Translates an array of {@link Site Sites} to an OR Regex which uses the sites' names.
     * @param sites List of Sites.
     * @return OR Regex using sites' names.
     */
    private String buildSitesOrRegex(List<Site> sites) {
        // Map Sites to site names.
        ArrayList<String> siteNames = (ArrayList<String>) Observable.from(sites)
                                                                    .map(Site::getName)
                                                                    .toList()
                                                                    .toBlocking()
                                                                    .single();
        return Util.buildOrRegex(siteNames.toArray(new String[siteNames.size()]));
    }

    /**
     * Get the configuration options parsed from the config file.
     * @return Config options. Never null.
     */
    public Config getConfig() {
        return config;
    }

    /**
     * Simple class which holds configuration values.
     */
    public class Config {
        /**
         * Config options storage. Keys for options are of the format "[Human-readable Site Name][Option prefix]".
         */
        private final HashMap<String, String> options = new HashMap<>();

        /**
         * Gets a String array like ["Username", "Password"], so long as both exist and are non-empty for the given
         * {@link Site}.
         * @param site Site to get credentials for.
         * @return Credentials String array, or null.
         */
        public String[] getCreds(Site site) {
            String u = options.get(site.getName() + CFG_LS_U);
            String p = options.get(site.getName() + CFG_LS_P);
            if (u == null || u.isEmpty() || p == null || p.isEmpty()) return null;
            return new String[] {u, p};
        }

        /**
         * Check if there are credentials for the given {@link Site}.
         * @param site Site to check credentials for.
         * @return True if the user supplied us with a non-empty username and password, otherwise false.
         */
        public boolean hasCreds(Site site) {
            return getCreds(site) != null;
        }
    }
}
