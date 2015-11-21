package bkromhout.Downloader;

import bkromhout.C;
import bkromhout.Chapter;
import bkromhout.Main;
import bkromhout.Story.FictionHuntStory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;

/**
 * Downloader for FictionHuntStory.
 */
public class FictionHuntDL {
    // List of story URLs
    private ArrayList<String> urls = new ArrayList<>();
    // Instance of an FFN downloader, created the first time it's needed, to easily download FFN stories.
    private FanfictionNetDL ffnDownloader = null;

    /**
     * Downloader for FictionHuntStory.
     * @param urls List of FictionHunt URLs.
     */
    public FictionHuntDL(ArrayList<String> urls) {
        this.urls = urls;
    }

    /**
     * Download the stories with URLs in the file.
     */
    public void download() {
        System.out.println("Starting FictionHunt download process...");
        // Create story models from URLs.
        System.out.println("Fetching stories from FictionHunt and building story models...");
        ArrayList<FictionHuntStory> stories = new ArrayList<>();
        for (String url : urls) {
            try {
                stories.add(new FictionHuntStory(url));
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }
        // Download and save the stories.
        System.out.println("Downloading stories from FictionHunt...\n");
        stories.forEach(this::downloadStory);
    }

    /**
     * Download the chapters of a story. If the story is still active on Fanfiction.net, an ePUB file will be downloaded
     * from p0ody-files. If not, it will be downloaded from FictionHunt by scraping and sanitizing the chapters.
     * @param story Story to download.
     */
    private void downloadStory(FictionHuntStory story) {
        if (story.getFfnStoryId() != -1) {
            // Story is still on Fanfiction.net, which is preferable since we can use p0ody-files to download the ePUB.
            if (ffnDownloader == null) ffnDownloader = new FanfictionNetDL(); // Get a FFN downloader instance.
            System.out.println("Story is still available on Fanfiction.net; using p0ody-files.com to download ePUB...");
            ffnDownloader.downloadStoryId(story.getFfnStoryId());
        } else {
            // Story isn't on Fanfiction.net anymore, download directly from FictionHunt.
            // Get chapter documents, and make sure we didn't fail to get some chapter (and if we did, skip this story.
            System.out.printf("Downloading chapters for: \"%s\"\n", story.getTitle());
            ArrayList<Chapter> chapters = downloadChapters(story);
            if (story.getChapterUrls().size() != chapters.size()) {
                System.out.println("Skipping this story; some chapters failed to download!!!\n");
                return;
            }
            // Sanitize the chapters; there are parts of FictionHunt's HTML that we don't really want.
            System.out.println("Sanitizing chapters...");
            chapters.forEach(this::sanitizeChapter);
            // Save the story.
            System.out.println("Saving story...");
            saveStory(story, chapters);
            System.out.println("Done!\n");
        }
    }

    /**
     * Download the chapters for a story.
     * @param story Story to download chapters for.
     * @return ArrayList of Chapter objects.
     */
    private ArrayList<Chapter> downloadChapters(FictionHuntStory story) {
        // Download chapter HTML Documents.
        ArrayList<Document> htmls = Main.getDocuments(story.getChapterUrls());
        // Generate chapter titles in the format "Chapter #"
        ArrayList<String> titles = new ArrayList<>();
        for (int i = 0; i < htmls.size(); i++) titles.add(String.format("Chapter %d", i + 1));
        // Create Chapter models.
        ArrayList<Chapter> chapters = new ArrayList<>();
        for (int i = 0; i < htmls.size(); i++) chapters.add(new Chapter(titles.get(i), htmls.get(i)));
        return chapters;
    }

    /**
     * Takes chapter text from
     * @param chapter Document for a FictionHunt story chapter.
     */
    private void sanitizeChapter(Chapter chapter) {
        // Get the chapter's text, keeping all HTML formatting intact
        String chapterText = chapter.html.select("div.text").first().html();
        // Create a new chapter HTML Document which is minimal.
        String newChapterHtml = String.format(C.CHAPTER_PAGE, chapter.title, chapter.title, chapterText);
        chapter.html = Jsoup.parse(newChapterHtml);
    }

    /**
     * Save the given story.
     * @param story    Story to save.
     * @param chapters Chapters of the story.
     */
    private void saveStory(FictionHuntStory story, ArrayList<Chapter> chapters) {
        // Create the directory if it doesn't already exist.
        Path storyDirPath = Main.dirPath.resolve(String.format("%s - %s", story.getAuthor(), story.getTitle()));
        File storyDir = storyDirPath.toFile();
        if (!storyDir.exists() && !storyDir.mkdir()) {
            System.err.printf("Couldn't create dir to save files at \"%s\"\n", storyDir.getAbsolutePath());
            // Technically this might be just because of a fail file title... but we should just stop anyway.
            System.exit(1);
        }
        // Create style.css file.
        saveFile(storyDirPath.resolve("style.css"), C.CSS.getBytes(StandardCharsets.UTF_8));
        // Create title.xhtml file.
        String titlePageText = String.format(C.TITLE_PAGE, story.getTitle(), story.getAuthor(), story.getRating(),
                story.getWordCount(), story.getChapterUrls().size());
        saveFile(storyDirPath.resolve("title.xhtml"), titlePageText.getBytes(StandardCharsets.UTF_8));
        // Save chapter file(s).
        for (int i = 0; i < chapters.size(); i++) {
            String chapterFileName = String.format("Chapter %d.xhtml", i + 1);
            Path chapterPath = storyDirPath.resolve(chapterFileName);
            byte[] chapterData = chapters.get(i).html.outerHtml().getBytes(StandardCharsets.UTF_8);
            saveFile(chapterPath, chapterData);
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
