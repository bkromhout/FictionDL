package bkromhout.fdl.parsing;

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
    private static final Pattern HOST_REGEX = Pattern.compile("^(http[s]?://)?([^:/\\s]+)(/.*)?$");
    /**
     * Regex for matching lines which point to local story directories.
     * <p>
     * Will match a line if, ignoring leading whitespace, it starts with "@fdl:ls=". (There can be whitespace before the
     * "=", but anything after it will be part of group 1).
     * <p>
     * If a line matches, group 1 will contain the rest of the line following "=". This text is intended to be used as
     * the name of a directory that is relative to the folder that the input file in in.
     */
    private static final Pattern LOCAL_STORY_REGEX = Pattern.compile("^\\s*\\Q@fdl:ls\\E\\s*=(.*)$");
    /**
     * Regex for matching lines which point to a directory that only contains local story directories.
     * <p>
     * Will match a line if, ignoring leading whitespace, it starts with "@fdl:ls_folder=". (There can be whitespace
     * before the "=", but anything after it will be part of group 1).
     * <p>
     * If a line matches, group 1 will contain the rest of the line following "=". This text is intended to be used as
     * the name of a directory that is relative to the folder that the input file in in.
     */
    private static final Pattern LOCAL_STORIES_FOLDER_REGEX = Pattern.compile("^\\s*\\Q@fdl:ls_folder\\E\\s*=(.*)$");
    /**
     * The names of the allowed detail tags.
     */
    private static final String[] ALLOWED_DETAIL_TAGS = {C.J_TITLE, C.J_AUTHOR, C.J_SUMMARY, C.J_SERIES, C.J_FIC_TYPE,
                                                         C.J_WARNINGS, C.J_RATING, C.J_GENRES, C.J_CHARACTERS};
    /**
     * Regex for matching detail tag lines. Only the detail tags in {@link #ALLOWED_DETAIL_TAGS} will be matched.
     * <p>
     * Will match a line if, ignoring leading whitespace, it starts with "@fdl:{Allowed Detail Tag}=", where {Allowed
     * Detail Tag} is one of {@link #ALLOWED_DETAIL_TAGS}. (There can be whitespace before the "=", but anything after
     * it will be part of group 1).
     * <p>
     * If a line matches, group 1 will contain the detail tag name, and group 2 will contain the rest of the line
     * following "=".
     */
    private static final Pattern DETAIL_TAG_REGEX =
            Pattern.compile("^\\s*\\Q@fdl:\\E(" + Util.buildOrRegex(ALLOWED_DETAIL_TAGS) + ")\\s*=(.*)$");

    /**
     * The last story entry created. Will be {@code null} if we haven't seen any story links yet.
     */
    private StoryEntry lastStoryEntry = null;

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
        // Ignore a line if it starts with a #.
        if (line.startsWith("#")) return;

        // Check if this line specifies a single local story folder.
        Matcher matcher = LOCAL_STORY_REGEX.matcher(line);
        if (matcher.matches()) {
            // Get the directory name from the line, stripping any leading/trailing whitespace.
            String dirName = matcher.group(1).trim();
            // Add the directory name to the list of local story directories in the local story processor if non-empty.
            if (!dirName.isEmpty()) C.getEventBus().post(new AddLSDirNameEvent(dirName));
            return;
        }

        // Check if this line specifies a folder whose subfolders should all be treated as local story folders.
        matcher = LOCAL_STORIES_FOLDER_REGEX.matcher(line);
        if (matcher.matches()) {
            // Get the directory name from the line, stripping any leading/trailing whitespace.
            String dirName = matcher.group(1).trim();
            // Add the directory name to the list of local story directories in the local story processor if
            // non-empty, but be sure to tell it that it needs to check for subfolders!
            if (!dirName.isEmpty()) C.getEventBus().post(new AddLSDirNameEvent(dirName, true));
            return;
        }

        // Try to match this line as an http(s) url so that we can extract the host.
        matcher = HOST_REGEX.matcher(line);
        if (matcher.matches()) {
            String hostString = matcher.group(2).toLowerCase();
            // Try to add the line (which we now know is a url) to a Site's list, and return immediately if there is
            // a site which can handle it.
            if (tryAddUrlToSomeSite(hostString, line))
                return;
        }

        // Try to match this line as a detail tag.
        matcher = DETAIL_TAG_REGEX.matcher(line);
        if (matcher.matches()) {
            // If we haven't seen a story link yet, then we can't use this detail tag, so we log and ignore it.
            if (lastStoryEntry == null) {
                Util.logf(C.DETAIL_TAG_IGNORED, line.trim());
                return;
            }
            // Get the detail tag name to use as the tag name.
            String tagName = matcher.group(1).trim();
            String detail = matcher.group(2).trim();
            // Add this detail to the last story entry we made.
            lastStoryEntry.addDetailTag(tagName, detail);
            return;
        }

        // Verbose log this line if we got here and it's non-empty, we couldn't process it.
        if (!line.trim().isEmpty()) Util.loudf(C.PROCESS_LINE_FAILED, type, line);
    }

    /**
     * Using the given host string, figure out which site the given {@code url} belongs to, then create a new {@link
     * StoryEntry} and assigns it to that site.
     * @param hostString String parsed from the line that should contain a substring which is equal to some {@link
     *                   Site#host} value.
     * @param url        The url to use to create a new {@link StoryEntry} to add to some {@link Site Site}'s {@link
     *                   Site#storyEntries story entry list}.
     * @return True if a {@link Site} was found to add the url to, otherwise false. Returns false immediately if either
     * parameter is null or empty.
     */
    private boolean tryAddUrlToSomeSite(String hostString, String url) {
        if (hostString == null || hostString.isEmpty() || url == null || url.isEmpty()) return false;
        // Iterate through the list of sites to find one to add the url to.
        for (Site site : Sites.all()) {
            if (hostString.contains(site.getHost())) {
                // Found a site which we can add this URL to.
                lastStoryEntry = new StoryEntry(url);
                site.getStoryEntries().add(lastStoryEntry);
                return true;
            }
        }
        // No site found to add the url to.
        return false;
    }
}
