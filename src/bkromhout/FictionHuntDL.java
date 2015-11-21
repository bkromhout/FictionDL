package bkromhout;

import java.io.*;
import java.util.ArrayList;

/**
 * Downloader for FictionHunt.
 */
public class FictionHuntDL {
    // Argument from main, the path to the file.
    private File storiesFile;
    // Path to directory containing list file where stories will be downloaded to.
    private File dir = null;
    // List of story URLs
    private ArrayList<String> urls = new ArrayList<>();

    /**
     * Downloader for FictionHunt.
     * @param path Path to file with FictionHunt URLs.
     */
    public FictionHuntDL(String path) {
        initialize(path);
    }

    /**
     * Make sure that file is valid, get a file object for it and its directory, and read the lines from it into an
     * ArrayList.
     * @param path Path to file with FictionHunt URLs.
     */
    private void initialize(String path) {
        // Make sure the given path is valid, is a file, and can be read.
        storiesFile = new File(path);
        if (!(storiesFile.exists() && storiesFile.isFile() && storiesFile.canRead())) {
            System.out.println("Invalid path.");
            System.exit(1);
        }
        // Get absolute path to directory containing the file.
        dir = storiesFile.getParentFile();

        // Try to read lines from file.
        try (BufferedReader br = new BufferedReader(new FileReader(storiesFile))) {
            String line = br.readLine();
            while (line != null) {
                line = line.trim();
                // Add line to list if it isn't blank or already in the list.
                if (!line.isEmpty() && !urls.contains(line)) urls.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Download the stories with URLs in the file.
     */
    public void download() {

    }
}
