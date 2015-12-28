package bkromhout.fdl.parsers;

import bkromhout.fdl.Site;
import bkromhout.fdl.util.C;
import bkromhout.fdl.util.Sites;
import bkromhout.fdl.util.Util;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses the input file.
 */
public class InputFileParser extends FileParser {
    /**
     * Regex for extracting host name from a url.
     */
    private static Pattern hostRegex = Pattern.compile("^(http[s]?://)?([^:/\\s]+)(/.*)?$");
    /**
     * Regex for matching lines which point to local fic directories.
     */
    private static Pattern localFicRegex = Pattern.compile(""); // TODO create regex to match "@fdl#dirbook=[something]"

    /**
     * Parse the file, populating the various url lists for the different sites.
     * @param storiesFile Link file.
     */
    public InputFileParser(File storiesFile) {
        super(FileType.URLS, storiesFile);
    }

    @Override
    protected void init() {
        // Do nothing.
    }

    /**
     * Processes a line from the input file, attempting to parse a story site url and assign it to one of the url lists.
     * Won't put any invalid or repeat lines in the url lists.
     * @param line Line from the input file.
     */
    @Override
    protected void processLine(String line) throws IllegalStateException {
        // TODO make this support local fics too!


        // Try to match this line to a url so that we can extract the host.
        Matcher hostMatcher = hostRegex.matcher(line);
        if (!hostMatcher.matches()) {
            if (!line.trim().isEmpty()) Util.loudf(C.PROCESS_LINE_FAILED, type, line);
            return;
        }
        String hostString = hostMatcher.group(2).toLowerCase();
        // Add story url to a Site's list, or log it if none of them could take it (malformed or unsupported site url).
        if (!tryAddUrlToSomeSite(hostString, line)) Util.logf(C.PROCESS_LINE_FAILED, type, line);
    }

    /**
     * Using the given host string, figure out which site we can add the given url string to and add it.
     * @param hostString String parsed from the line that should contain a substring which is equal to some {@link
     *                   bkromhout.fdl.Site#host} value.
     * @param url        The url to add to some {@link bkromhout.fdl.Site Site}'s {@link bkromhout.fdl.Site#urls url
     *                   list}.
     * @return True if a {@link bkromhout.fdl.Site} was found to add the url to, otherwise false. Returns false
     * immediately if either parameter is null or empty.
     */
    private boolean tryAddUrlToSomeSite(String hostString, String url) {
        if (hostString == null || hostString.isEmpty() || url == null || url.isEmpty()) return false;
        // Iterate through the list of sites to find one to add the url to.
        for (Site site : Sites.all()) {
            if (hostString.contains(site.getHost())) {
                // Found a site which we can add this URL to.
                site.getUrls().add(url);
                return true;
            }
        }
        // No site found to add the url to.
        return false;
    }
}
