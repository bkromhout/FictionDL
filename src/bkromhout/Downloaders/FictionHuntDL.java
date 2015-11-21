package bkromhout.Downloaders;

import bkromhout.StoryModels.FictionHuntStory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.*;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Downloader for FictionHuntStory.
 */
public class FictionHuntDL {
    // Argument from main, the path to the file.
    private File storiesFile;
    // Path to directory containing list file where stories will be downloaded to.
    private File dir = null;
    // List of story URLs
    private ArrayList<String> urls = new ArrayList<>();

    /**
     * Downloader for FictionHuntStory.
     * @param path Path to file with FictionHuntStory URLs.
     */
    public FictionHuntDL(String path) {
        initialize(path);
    }

    /**
     * Make sure that file is valid, get a file object for it and its directory, and read the lines from it into an
     * ArrayList.
     * @param path Path to file with FictionHuntStory URLs.
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
        // Try to read lines from file into the url list
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
     * Takes in a list of URLs (as strings) and returns a list of Documents downloaded from the URLs. Any malformed URLs
     * in the input list will be skipped.
     * @param urlList List of URLs to get Documents for.
     * @return Documents for all valid URLs that were in the input list.
     */
    private ArrayList<Document> getDocuments(ArrayList<String> urlList) {
        ArrayList<Document> docs = new ArrayList<>();
        // Loop through the URL list and download from each.
        for (String url : urlList) {
            try {
                docs.add(Jsoup.connect(url).get());
            } catch (IOException e) {
                System.out.printf("Failed to download HTML from: \"%s\"\n", url);
            }
        }
        return docs;
    }

    /**
     * Download the stories with URLs in the file.
     */
    public void download() {
        // Get initial HTML Document for each story.
        ArrayList<Document> initialStoryDocs = getDocuments(urls);
        // Create story models from initial chapters.
        ArrayList<FictionHuntStory> stories = initialStoryDocs.stream().map(FictionHuntStory::new).collect(
                Collectors.toCollection(ArrayList<FictionHuntStory>::new));

    }


}
