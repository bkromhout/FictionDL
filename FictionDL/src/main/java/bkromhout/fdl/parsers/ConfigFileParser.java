package bkromhout.fdl.parsers;

import bkromhout.fdl.C;
import bkromhout.fdl.Site;
import bkromhout.fdl.Util;
import rx.Observable;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
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
     * Parse the file and create a configuration.
     * @param cfgFile Config file.
     */
    public ConfigFileParser(File cfgFile) {
        super("config", cfgFile);
    }

    @Override
    protected void init() {
        config = new Config();
        siteNameRegex = buildSitesOrRegex(Site.values());
    }

    /**
     * Processes a line from the config file, doing different things depending on what the line starts with. Ignores
     * lines if they don't start with a valid line starter or are malformed.
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
                // Check to make sure this is a valid site.
                Matcher siteMatcher = Pattern.compile(siteNameRegex).matcher(line);
                // Let's be nice, if a site is misspelled or something, don't potentially overwrite a previous site's
                // info later. If it is a valid site name, then update the current site.
                if (!siteMatcher.find()) currSite = null;
                else currSite = siteMatcher.group();
                break;
            }
            case CFG_LS_U:
            case CFG_LS_P: {
                // Save a username or password for the current site.
                if (currSite == null) break; // Ignore this if it comes before a valid site.
                // We take the rest of the line wholesale, so any spaces will be kept.
                config.options.put(currSite + prefix, line.substring(line.indexOf('=') + 1));
                break;
            }
            default: {
                Util.loudf(C.PROCESS_LINE_FAILED, type, line);
            }
        }
    }

    /**
     * Translates an array of {@link Site Sites} to an OR Regex which uses the sites' names.
     * @param sites Array of Sites.
     * @return OR Regex using sites' names.
     */
    private String buildSitesOrRegex(Site[] sites) {
        // Map Sites to site names.
        ArrayList<String> siteNames = (ArrayList<String>) Observable.from(sites)
                                                                    .map(Site::getName)
                                                                    .toList()
                                                                    .toBlocking()
                                                                    .single();
        return Util.buildOrRegex((String[]) siteNames.toArray());
    }

    /**
     * Get the configuration options parsed from the config file.
     * @return Config options.
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
        protected HashMap<String, String> options = new HashMap<>();

        /**
         * Gets a String array like ["Username", "Password"], so long as both exist and are non-empty for the given
         * site.
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
         * Check if there are credentials for the given site.
         * @param site Site to check credentials for.
         * @return True if the user supplied us with a non-empty username and password, otherwise false.
         */
        public boolean hasCreds(Site site) {
            return getCreds(site) != null;
        }
    }
}
