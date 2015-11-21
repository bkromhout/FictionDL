package bkromhout.Downloaders;

import bkromhout.StoryModels.FictionHuntStory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Downloader for FictionHuntStory.
 */
public class FictionHuntDL {
    // Argument from main, the path to the file.
    private File storiesFile;
    // Path to save directory. This is where story folders will be created, and where the story urls file is.
    private Path dirPath;
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
        System.out.println("Checking file...");
        // Make sure the given path is valid, is a file, and can be read.
        storiesFile = new File(path);
        if (!(storiesFile.exists() && storiesFile.isFile() && storiesFile.canRead())) {
            System.err.println("Invalid path.");
            System.exit(1);
        }
        dirPath = storiesFile.getParentFile().toPath();
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
        System.out.println("Processed file.");
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
        System.out.println("Starting download process...");
        // Get initial HTML Document for each story.
        System.out.println("Finding stories...");
        ArrayList<Document> initialStoryDocs = getDocuments(urls);
        // Create story models from initial chapters.
        System.out.println("Building story models...");
        ArrayList<FictionHuntStory> stories = initialStoryDocs.stream().map(FictionHuntStory::new).collect(
                Collectors.toCollection(ArrayList<FictionHuntStory>::new));
        System.out.println("Downloading stories...");
        stories.forEach(this::downloadStory);
        System.out.println("\nAll Finished! :)");
    }

    /**
     * Download the chapters of a story.
     * @param story Story to download.
     */
    private void downloadStory(FictionHuntStory story) {
        // Get chapter documents, and make sure we didn't fail to get some chapter (and if we did, skip this story.
        System.out.printf("Downloading chapters for: \"%s\"\n", story.getTitle());
        ArrayList<Document> chapters = getDocuments(story.getChapterUrls());
        if (story.getChapterUrls().size() != chapters.size()) {
            System.out.printf("Skipping this story; some chapters failed to download: \"%s\"\n", story.getTitle());
            return;
        }
        // Sanitize the chapters; there are parts of FictionHunt's HTML that we don't really want.
        System.out.printf("Sanitizing chapters for: \"%s\"\n", story.getTitle());
        chapters.forEach(this::sanitizeChapter);
        // Save the story.
        System.out.printf("Saving story: \"%s\"\n", story.getTitle());
        saveStory(story, chapters);
        System.out.println("Done!");
    }

    /**
     * Remove parts of a FictionHunt story chapter's HTML to make it cleaner.
     * @param chapter Document for a FictionHunt story chapter.
     */
    private void sanitizeChapter(Document chapter) {
        // TODO: this
    }

    /**
     * Save the given story. TODO eventually make this use real chapter names where possible.
     * @param story    Story to save.
     * @param chapters Chapters of the story.
     */
    private void saveStory(FictionHuntStory story, ArrayList<Document> chapters) {
        // Create the directory if it doesn't already exist.
        Path storyDirPath = dirPath.resolve(String.format("%s - %s", story.getAuthor(), story.getTitle()));
        File storyDir = storyDirPath.toFile();
        if (!storyDir.exists() && !storyDir.mkdir()) {
            System.err.printf("Couldn't create dir to save files at \"%s\"\n", storyDir.getAbsolutePath());
            // Technically this might be just because of a fail file name... but we should just stop anyway.
            System.exit(1);
        }
        // Create storyinfo.txt file.
        saveFile(storyDirPath.resolve("_storyinfo_.txt"), story.toString().getBytes(StandardCharsets.UTF_8));
        // Save chapter files.
        if (chapters.size() == 1) {
            // Only one chapter, use the same name as the directory.
            String chapterFileName = String.format("%s - %s.html", story.getAuthor(), story.getTitle());
            Path chapterPath = storyDirPath.resolve(chapterFileName);
            byte[] chapterData = chapters.get(0).outerHtml().getBytes(StandardCharsets.UTF_8);
            saveFile(chapterPath, chapterData);
        } else {
            // We have multiple chapters.
            for (int i = 0; i < chapters.size(); i++) {
                String chapterFileName = String.format("Chapter %d.html", i + 1);
                Path chapterPath = storyDirPath.resolve(chapterFileName);
                byte[] chapterData = chapters.get(i).outerHtml().getBytes(StandardCharsets.UTF_8);
                saveFile(chapterPath, chapterData);
            }
        }
    }

    /**
     * Save a file at the specified path with the specified data. Will create the file if it doesn't exist and overwrite
     * it if it does.
     * @param filePath Path at which to save the file.
     * @param data     Data to save to the file.
     */
    private void saveFile(Path filePath, byte[] data) {
        try {
            Files.write(filePath, data);
        } catch (IOException e) {
            System.err.printf("Failed to save file: %s\n", filePath.toString());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
