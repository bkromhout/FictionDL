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
        Util.logf(C.PARSE_FILE, C.FTYPE_CFG);
        // Try to read lines from file into the url list
        try (BufferedReader br = new BufferedReader(new FileReader(cfgFile))) {
            String line = br.readLine();
            while (line != null) {
                try {
                    // Process the line.
                    processLine(line.trim());
                } catch (IllegalStateException e) {
                    // Do nothing, we don't care if config lines are bad.
                }
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
        if (line == null || line.isEmpty() || line.startsWith(C.CFG_LS_HASH) || !line.contains("=") ||
                line.indexOf('=') == line.length() - 1) return;
        // Do different things based on what the line starts with (the option prefix).
        String prefix = line.substring(0, line.indexOf('='));
        switch (prefix) {
            case C.CFG_LS_SITE: {
                // Check to make sure this is a valid site.
                String siteRegex = Util.buildOrRegex(C.NAME_FFN, C.NAME_FH, C.NAME_SIYE, C.NAME_MN);
                Matcher siteMatcher = Pattern.compile(siteRegex).matcher(line);
                // Let's be nice, if a site is misspelled or something, don't potentially overwrite a previous site's
                // info later. If it is a valid site name, then update the current site.
                if (!siteMatcher.find()) currSite = null;
                else currSite = siteMatcher.group();
                break;
            }
            case C.CFG_LS_U:
            case C.CFG_LS_P: {
                // Save a username or password for the current site.
                if (currSite == null) break; // Ignore this if it comes before a valid site.
                // We take the rest of the line wholesale, so any spaces will be kept.
                config.options.put(currSite + prefix, line.substring(line.indexOf('=') + 1));
                break;
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

        /*
        MuggleNet
         */
        /**
         * @return MuggleNet username.
         */
        public String mnUsername() {
            return options.get(C.NAME_MN + C.CFG_LS_U);
        }

        /**
         * @return MuggleNet password.
         */
        public String mnPassword() {
            return options.get(C.NAME_MN + C.CFG_LS_P);
        }

        /**
         * Do we have all we need to try and log in to MuggleNet?
         * @return True if we have a non-empty username and password for MuggleNet, otherwise false.
         */
        public boolean hasMnAuth() {
            String username = options.get(C.NAME_MN + C.CFG_LS_U);
            String password = options.get(C.NAME_MN + C.CFG_LS_P);
            return username != null && !username.isEmpty() && password != null && !password.isEmpty();
        }
    }
}
