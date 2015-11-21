package bkromhout;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Parses the input file.
 */
public class FileParser {
    // FictionHunt URLs.
    private ArrayList<String> fictionHuntUrls = new ArrayList<>();

    /**
     * Parse the file, populating the various URL lists for the different sites.
     */
    public FileParser(String path) {
        initialize(path);
    }

    private void initialize(String path) {
        System.out.println("Checking and parsing file...");
        // Make sure the given path is valid, is a file, and can be read.
        File storiesFile = new File(path);
        if (!(storiesFile.exists() && storiesFile.isFile() && storiesFile.canRead())) {
            System.err.println("Invalid path.");
            System.exit(1);
        }
        Main.dirPath = storiesFile.getParentFile().toPath();
        // Try to read lines from file into the url list
        // TODO we could probably use the Files api for this with lambdas...
        try (BufferedReader br = new BufferedReader(new FileReader(storiesFile))) {
            String line = br.readLine();
            while (line != null) {
                processLine(line.trim());
                line = br.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Processed file.\n");
    }

    /**
     * Processes a line from the input file, attempting to parse a story site URL and assign it to one of the URL
     * lists.
     * @param line Line from the input file.
     */
    private void processLine(String line) {
        // TODO currently blindly assumes all URLs are FictionHunt urls.
        // Add line to list if it isn't blank or already in the list.
        if (!line.isEmpty() && !fictionHuntUrls.contains(line)) fictionHuntUrls.add(line);
    }

    /**
     * Get the list of FictionHunt URLs that were parsed.
     * @return FictionHunt URLs.
     */
    public ArrayList<String> getFictionHuntUrls() {
        return fictionHuntUrls;
    }
}
