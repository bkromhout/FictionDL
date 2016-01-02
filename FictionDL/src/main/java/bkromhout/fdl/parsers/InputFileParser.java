package bkromhout.fdl.parsers;

import bkromhout.fdl.events.AddLSDirNameEvent;
import bkromhout.fdl.site.Site;
import bkromhout.fdl.site.Sites;
import bkromhout.fdl.util.C;
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
    private static final Pattern hostRegex = Pattern.compile("^(http[s]?://)?([^:/\\s]+)(/.*)?$");
    /**
     * Regex for matching lines which point to local story directories.
     * <p>
     * Will match a line if, ignoring leading whitespace, it starts with "@fdl:ls=". (There can be whitespace before the
     * "=", but anything after it will be part of group 1).
     * <p>
     * If a line matches, group 1 will contain the rest of the line following "=". This text is intended to be used as
     * the name of a directory that is relative to the folder that the input file in in.
     */
    private static final Pattern localStoryRegex = Pattern.compile("^\\s*@fdl:ls\\s*=(.*)$");

    /**
     * Create a new {@link InputFileParser} to parse the given file.
     * <p>
     * This class should be used as a oneshot call since it parses the file upon creation and exposes no public
     * members.
     * @param inputFile Input file.
     */
    public InputFileParser(File inputFile) {
        super(FileType.INPUT, inputFile);
    }

    @Override
    protected void init() {
        // Do nothing.
    }

    @Override
    protected void processLine(String line) throws IllegalStateException {
        // Check if this line specifies a local story.
        Matcher lsMatcher = localStoryRegex.matcher(line);
        if (lsMatcher.matches()) {
            // Get the directory name from the line, stripping any leading/trailing whitespace.
            String dirName = lsMatcher.group(1).trim();
            // Add the directory name to the list of local story directories in the local story processor if non-empty.
            if (!dirName.isEmpty()) C.getEventBus().post(new AddLSDirNameEvent(dirName));
            return;
        }

        // Try to match this line as an http(s) url so that we can extract the host.
        Matcher hostMatcher = hostRegex.matcher(line);
        if (hostMatcher.matches()) {
            String hostString = hostMatcher.group(2).toLowerCase();
            // Try to add the line (which we now know is a url) to a Site's list, and return immediately if there is
            // a site which can handle it.
            if (tryAddUrlToSomeSite(hostString, line)) return;
        }

        // Verbose log this line if we got here and it's non-empty, we couldn't process it.
        if (!line.trim().isEmpty()) Util.loudf(C.PROCESS_LINE_FAILED, type, line);
    }

    /**
     * Using the given host string, figure out which site we can add the given url string to and add it.
     * @param hostString String parsed from the line that should contain a substring which is equal to some {@link
     *                   Site#host} value.
     * @param url        The url to add to some {@link Site Site}'s {@link Site#urls url list}.
     * @return True if a {@link Site} was found to add the url to, otherwise false. Returns false immediately if either
     * parameter is null or empty.
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
