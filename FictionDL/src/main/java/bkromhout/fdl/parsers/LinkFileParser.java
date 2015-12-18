package bkromhout.fdl.parsers;

import bkromhout.fdl.C;
import bkromhout.fdl.Site;
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
     * Regex for extracting host name from a url.
     */
    private static Pattern hostRegex = Pattern.compile("^(http[s]?://)?([^:/\\s]+)(/.*)?$");
    /**
     * FictionHunt urls.
     */
    private HashSet<String> fictionHuntUrls;
    /**
     * FanFiction.net urls.
     */
    private HashSet<String> ffnUrls;
    /**
     * SIYE urls.
     */
    private HashSet<String> siyeUrls;
    /**
     * MuggleNet urls.
     */
    private HashSet<String> mnUrls;
    /**
     * Ao3 urls.
     */
    private HashSet<String> ao3Urls;

    /**
     * Parse the file, populating the various url lists for the different sites.
     * @param storiesFile Link file.
     */
    public LinkFileParser(File storiesFile) {
        super("URLs", storiesFile);
    }

    @Override
    protected void init() {
        fictionHuntUrls = new HashSet<>();
        ffnUrls = new HashSet<>();
        siyeUrls = new HashSet<>();
        mnUrls = new HashSet<>();
        ao3Urls = new HashSet<>();
    }

    /**
     * Processes a line from the input file, attempting to parse a story site url and assign it to one of the url lists.
     * Won't put any invalid or repeat lines in the url lists.
     * @param line Line from the input file.
     */
    @Override
    protected void processLine(String line) throws IllegalStateException {
        // Try to match this line to a url so that we can extract the host.
        Matcher hostMatcher = hostRegex.matcher(line);
        if (!hostMatcher.matches()) {
            if (!line.trim().isEmpty()) Util.loudf(C.PROCESS_LINE_FAILED, type, line);
            return;
        }
        String hostString = hostMatcher.group(2).toLowerCase();
        // Add story url to a set, or none of them if it wasn't valid.
        if (hostString.contains(Site.FH.getHost())) fictionHuntUrls.add(line);
        else if (hostString.contains(Site.FFN.getHost())) ffnUrls.add(line);
        else if (hostString.contains(Site.SIYE.getHost())) siyeUrls.add(line);
        else if (hostString.contains(Site.MN.getHost())) mnUrls.add(line);
        else if (hostString.contains(Site.AO3.getHost())) ao3Urls.add(line);
        else Util.logf(C.PROCESS_LINE_FAILED, type, line); // Malformed or unsupported site url.
    }

    /**
     * Adds a FanFiction.net url to that list. Useful for when we decide to download a FictionHunt story from
     * FanFiction.net instead.
     * @param ffnUrl FanFiction.net url.
     */
    public void addFfnUrl(String ffnUrl) {
        ffnUrls.add(ffnUrl);
    }

    /**
     * Get the list of FictionHunt urls that were parsed.
     * @return FictionHunt urls.
     */
    public HashSet<String> getFictionHuntUrls() {
        return fictionHuntUrls;
    }

    /**
     * Get the list of FanFiction.net urls that were parsed.
     * @return FanFiction.net urls.
     */
    public HashSet<String> getFfnUrls() {
        return ffnUrls;
    }

    /**
     * Get the list of SIYE urls that were parsed.
     * @return SIYE urls.
     */
    public HashSet<String> getSiyeUrls() {
        return siyeUrls;
    }

    /**
     * Get the list of MuggleNet urls that were parsed.
     * @return MuggleNet urls.
     */
    public HashSet<String> getMnUrls() {
        return mnUrls;
    }

    /**
     * Get the list of Ao3 urls that were parsed.
     * @return Ao3 urls.
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
        // TODO do this smarter somehow.
        return fictionHuntUrls.size() + ffnUrls.size() + siyeUrls.size() + mnUrls.size() + ao3Urls.size();
    }
}
