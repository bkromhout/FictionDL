package bkromhout.FictionDL;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses the input file.
 */
public class FileParser {
    // Regex for extracting host strings.
    private Pattern hostRegex = Pattern.compile(C.HOST_REGEX);
    // FictionHunt URLs.
    private ArrayList<String> fictionHuntUrls = new ArrayList<>();
    // Fanfiction.net URLs.
    private ArrayList<String> ffnUrls = new ArrayList<>();
    // SIYE URLs.
    private ArrayList<String> siyeUrls = new ArrayList<>();

    /**
     * Parse the file, populating the various URL lists for the different sites.
     */
    public FileParser(String path) {
        initialize(path);
    }

    private void initialize(String path) {
        System.out.printf(C.CHECK_AND_PARSE_FILE);
        // Make sure the given path is valid, is a file, and can be read.
        File storiesFile = new File(path);
        if (!(storiesFile.exists() && storiesFile.isFile() && storiesFile.canRead())) {
            System.out.println(C.INVALID_PATH);
            System.exit(1);
        }
        FictionDL.dirPath = storiesFile.getParentFile().toPath();
        // Try to read lines from file into the url list
        try (BufferedReader br = new BufferedReader(new FileReader(storiesFile))) {
            String line = br.readLine();
            while (line != null) {
                try {
                    // Process the line.
                    processLine(line.trim());
                } catch (IllegalStateException e) {
                    // If we couldn't match one of the lines, just say which one in the output (or silently skip it if
                    // it's a blank line.
                    if (!line.trim().isEmpty()) System.out.printf(C.PROCESS_LINE_FAILED, line);
                }
                line = br.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(C.DONE);
    }

    /**
     * Processes a line from the input file, attempting to parse a story site URL and assign it to one of the URL
     * lists. Won't put any invalid or repeat lines in the URL lists.
     * @param line Line from the input file.
     */
    private void processLine(String line) throws IllegalStateException {
        // Try to match this line to a URL so that we can extract the host.
        Matcher hostMatcher = hostRegex.matcher(line);
        if (!hostMatcher.matches()) throw new IllegalStateException();
        String hostString = hostMatcher.group(2).toLowerCase();
        // ...then add it to it, so long as it isn't a repeat. (Note that since FFN has so many different link styles,
        // it's totally possible for the same story to get added twice. Maybe I'll add some normalization code later.)
        if (hostString.contains("fictionhunt.com") && !fictionHuntUrls.contains(line)) fictionHuntUrls.add(line);
        else if (hostString.contains("fanfiction.net") && !ffnUrls.contains(line)) ffnUrls.add(line);
        else if (hostString.contains("siye.co.uk") && ! siyeUrls.contains(line)) siyeUrls.add(line);
    }

    /**
     * Get the list of FictionHunt URLs that were parsed.
     * @return FictionHunt URLs.
     */
    public ArrayList<String> getFictionHuntUrls() {
        return fictionHuntUrls;
    }

    /**
     * Get the list of Fanfiction.net URLs that were parsed.
     * @return Fanfiction.net URLs.
     */
    public ArrayList<String> getFfnUrls() {
        return ffnUrls;
    }

    /**
     * Get the list of SIYE URLs that were parsed.
     * @return SIYE URLs.
     */
    public ArrayList<String> getSiyeUrls() {
        return siyeUrls;
    }
}