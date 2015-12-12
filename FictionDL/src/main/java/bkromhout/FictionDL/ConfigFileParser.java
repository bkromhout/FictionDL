package bkromhout.FictionDL;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses the config file.
 */
public class ConfigFileParser {
    /**
     * The type of file this parser handles.
     */
    private static final String TYPE = "config";
    /* Valid Line Starters */
    private static final String CFG_LS_P = "p";
    private static final String CFG_LS_U = "u";
    private static final String CFG_LS_SITE = "site";
    private static final String CFG_LS_HASH = "#";

    // Config instance.
    private Config config = new Config();
    // Site which will be given any site-specific preferences.
    private String currSite = null;

    /**
     * Parse the file and create a configuration.
     * @param cfgFile Config file.
     */
    public ConfigFileParser(File cfgFile) {
        parse(cfgFile);
    }

    private void parse(File cfgFile) {
        Util.logf(C.PARSE_FILE, TYPE);
        // Try to read lines from file into the url list
        try (BufferedReader br = new BufferedReader(new FileReader(cfgFile))) {
            String line = br.readLine();
            while (line != null) {
                // Process the line.
                processLine(line.trim());
                line = br.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Util.log(C.DONE);
    }

    /**
     * Processes a line from the config file, doing different things depending on what the line starts with. Ignores
     * lines if they don't start with a valid line starter or are malformed.
     * @param line Line from the config file.
     */
    private void processLine(String line) {
        // Sometimes we don't care...
        if (line == null || line.isEmpty() || line.startsWith(CFG_LS_HASH) || !line.contains("=") ||
                line.indexOf('=') == line.length() - 1) return;
        // Do different things based on what the line starts with (the option prefix).
        String prefix = line.substring(0, line.indexOf('='));
        switch (prefix) {
            case CFG_LS_SITE: {
                // Check to make sure this is a valid site.
                String siteRegex = Util.buildOrRegex(C.NAME_FFN, C.NAME_FH, C.NAME_SIYE, C.NAME_MN);
                Matcher siteMatcher = Pattern.compile(siteRegex).matcher(line);
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
                Util.loudf(C.PROCESS_LINE_FAILED, TYPE, line);
            }
        }
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
         * Get the username that a user has supplied for a specific site.
         * @param siteName Human-readable site name to get supplied username for.
         * @return Username that the user supplied for the given site, or null if it wasn't supplied.
         */
        public String getUsername(String siteName) {
            return options.get(siteName + CFG_LS_U);
        }

        /**
         * Get the password that a user has supplied for a specific site.
         * @param siteName Human-readable site name to get supplied password for.
         * @return Password that the user supplied for the given site, or null if it wasn't supplied.
         */
        public String getPassword(String siteName) {
            return options.get(siteName + CFG_LS_P);
        }

        /**
         * Convenience method to check if we have all we need to try and log in to MuggleNet.
         * @return True if we have a non-empty username and password for MuggleNet, otherwise false.
         */
        public boolean hasMnAuth() {
            String username = getUsername(C.NAME_MN);
            String password = getPassword(C.NAME_MN);
            return username != null && !username.isEmpty() && password != null && !password.isEmpty();
        }
    }
}
