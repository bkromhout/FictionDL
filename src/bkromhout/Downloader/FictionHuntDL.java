package bkromhout.Downloader;

import bkromhout.Main;
import bkromhout.Story.FictionHuntStory;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;

/**
 * Downloader for FictionHuntStory.
 */
public class FictionHuntDL {
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
        File storiesFile = new File(path);
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
                line = br.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Processed file.");
    }

    /**
     * Download the stories with URLs in the file.
     */
    public void download() {
        System.out.println("Starting download process...");
        // Create story models from URLs.
        System.out.println("Fetching stories and building story models...");
        ArrayList<FictionHuntStory> stories = new ArrayList<>();
        for (String url : urls) {
            try {
                stories.add(new FictionHuntStory(url));
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }
        // Download and save the stories.
        System.out.println("Downloading stories...\n");
        stories.forEach(this::downloadStory);
        System.out.println("All Finished! :)");
    }

    /**
     * Download the chapters of a story.
     * @param story Story to download.
     */
    private void downloadStory(FictionHuntStory story) {
        // Get chapter documents, and make sure we didn't fail to get some chapter (and if we did, skip this story.
        System.out.printf("Downloading chapters for: \"%s\"\n", story.getTitle());
        ArrayList<Document> chapters = Main.getDocuments(story.getChapterUrls());
        if (story.getChapterUrls().size() != chapters.size()) {
            System.out.printf("Skipping this story; some chapters failed to download: \"%s\"\n\n", story.getTitle());
            return;
        }
        // Sanitize the chapters; there are parts of FictionHunt's HTML that we don't really want.
        // TODO if we want to get real chapter names, it's gonna need to happen before this!!!
        System.out.printf("Sanitizing chapters for: \"%s\"\n", story.getTitle());
        chapters.forEach(this::sanitizeChapter);
        // Save the story.
        System.out.printf("Saving story: \"%s\"\n", story.getTitle());
        saveStory(story, chapters);
        System.out.println("Done!\n");
    }

    /**
     * Removes all parts of the FictionHunt HTML except for the head's title element and the chapter text, which gets
     * pulled up and becomes the contents of the body.
     * @param chapter Document for a FictionHunt story chapter.
     */
    private void sanitizeChapter(Document chapter) {
        // Remove all Elements in <head> except for <title>.
        chapter.select("head > *:not(title)").forEach(Element::remove);
        // Get the chapter's text, keeping all HTML formatting intact. Then replace <body>'s contents with it.
        chapter.body().html(chapter.select("div.text").first().html());
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
