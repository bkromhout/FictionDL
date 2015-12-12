package bkromhout.fdl.parsers;

import bkromhout.fdl.C;
import bkromhout.fdl.Util;

import java.io.File;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses the input file.
 */
public class LinkFileParser extends FileParser {
    /**
     * Regex for extracting host name from a URL.
     */
    private static Pattern hostRegex = Pattern.compile("^(http[s]?://)?([^:/\\s]+)(/.*)?$");
    /**
     * FictionHunt URLs.
     */
    private HashSet<String> fictionHuntUrls = new HashSet<>();
    /**
     * FanFiction.net URLs.
     */
    private HashSet<String> ffnUrls = new HashSet<>();
    /**
     * SIYE URLs.
     */
    private HashSet<String> siyeUrls = new HashSet<>();
    /**
     * MuggleNet URLs.
     */
    private HashSet<String> mnUrls = new HashSet<>();
    /**
     * Ao3 URLs.
     */
    private HashSet<String> ao3Urls = new HashSet<>();

    /**
     * Parse the file, populating the various URL lists for the different sites.
     * @param storiesFile Link file.
     */
    public LinkFileParser(File storiesFile) {
        super("URLs", storiesFile);
    }

    /**
     * Processes a line from the input file, attempting to parse a story site URL and assign it to one of the URL lists.
     * Won't put any invalid or repeat lines in the URL lists.
     * @param line Line from the input file.
     */
    @Override
    protected void processLine(String line) throws IllegalStateException {
        // Try to match this line to a URL so that we can extract the host.
        Matcher hostMatcher = hostRegex.matcher(line);
        if (!hostMatcher.matches()) {
            if (!line.trim().isEmpty()) Util.loudf(C.PROCESS_LINE_FAILED, type, line);
            return;
        }
        String hostString = hostMatcher.group(2).toLowerCase();
        // Add story URL to a set, or none of them if it wasn't valid.
        if (hostString.contains(C.HOST_FH)) fictionHuntUrls.add(line);
        else if (hostString.contains(C.HOST_FFN)) ffnUrls.add(line);
        else if (hostString.contains(C.HOST_SIYE)) siyeUrls.add(line);
        else if (hostString.contains(C.HOST_MN)) mnUrls.add(line);
        else if (hostString.contains(C.HOST_AO3)) ao3Urls.add(line);
        else Util.logf(C.PROCESS_LINE_FAILED, type, line); // Malformed or unsupported site URL.
    }

    /**
     * Adds a FanFiction.net URL to that list. Useful for when we decide to download a FictionHunt story from
     * FanFiction.net instead.
     * @param ffnUrl FanFiction.net URL.
     */
    public void addFfnUrl(String ffnUrl) {
        ffnUrls.add(ffnUrl);
    }

    /**
     * Get the list of FictionHunt URLs that were parsed.
     * @return FictionHunt URLs.
     */
    public HashSet<String> getFictionHuntUrls() {
        return fictionHuntUrls;
    }

    /**
     * Get the list of FanFiction.net URLs that were parsed.
     * @return FanFiction.net URLs.
     */
    public HashSet<String> getFfnUrls() {
        return ffnUrls;
    }

    /**
     * Get the list of SIYE URLs that were parsed.
     * @return SIYE URLs.
     */
    public HashSet<String> getSiyeUrls() {
        return siyeUrls;
    }

    /**
     * Get the list of MuggleNet URLs that were parsed.
     * @return MuggleNet URLs.
     */
    public HashSet<String> getMnUrls() {
        return mnUrls;
    }

    /**
     * Get the list of Ao3 URLs that were parsed.
     * @return Ao3 URLs.
     */
    public HashSet<String> getAo3Urls() {
        return ao3Urls;
    }

    /**
     * Total number of stories which are to be downloaded across all sites. Note that it is best to find this value
     * right after this LinkFileParser is initialized since counts in individual lists may change later.
     * @return Number of stories.
     */
    public int getTotalNumStories() {
        return fictionHuntUrls.size() + ffnUrls.size() + siyeUrls.size() + mnUrls.size() + ao3Urls.size();
    }
}
