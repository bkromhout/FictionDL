package bkromhout.Downloader;

import bkromhout.C;
import bkromhout.Chapter;
import bkromhout.Main;
import bkromhout.Story.SiyeStory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Downloader for siye.co.uk ("Sink Into Your Eyes").
 */
public class SiyeDL {
    public static final String SITE = "SIYE";
    // List of SIYE URLs.
    ArrayList<String> urls;

    /**
     * Create a new SIYE downloader.
     * @param urls List of SIYE URLs.
     */
    public SiyeDL(ArrayList<String> urls) {
        this.urls = urls;
    }

    /**
     * Download the stories whose URLs were passed to this instance of the downloader upon creation..
     */
    public void download() {
        // Do nothing if we have no URLs.
        if (urls.isEmpty()) return;
        System.out.printf(C.STARTING_SITE_DL_PROCESS, SITE);
        // Create story models from URLs.
        System.out.printf(C.FETCH_BUILD_MODELS, SITE);
        ArrayList<SiyeStory> stories = new ArrayList<>();
        for (String url : urls) {
            try {
                stories.add(new SiyeStory(url));
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }
        // Download and save the stories.
        System.out.printf(C.DL_STORIES_FROM_SITE, SITE);
        stories.forEach(this::downloadStory);
    }

    /**
     * Download chapters of a story.
     * @param story The story to download.
     */
    private void downloadStory(SiyeStory story) {
        // Get chapter documents, and make sure we didn't fail to get some chapter (and if we did, skip this story).
        System.out.printf(C.DL_CHAPS_FOR, story.getTitle());
        ArrayList<Chapter> chapters = downloadChapters(story);
        if (story.getChapterUrls().size() != chapters.size()) {
            System.out.println(C.SOME_CHAPS_FAILED);
            return;
        }
        // Sanitize the chapters so that they are in the expected xhtml format for ePUB.
        System.out.println(C.SANITIZING_CHAPS);
        chapters.forEach(this::sanitizeChapter);
        // Save the story.
        System.out.printf(C.SAVING_STORY);
        saveStory(story, chapters);
        System.out.println(C.DONE + "\n"); // Add an empty line.
    }

    /**
     * Download the chapters for a story.
     * @param story Story to download chapters for.
     * @return ArrayList of Chapter objects.
     */
    private ArrayList<Chapter> downloadChapters(SiyeStory story) {
        // Download chapter HTML Documents.
        ArrayList<Document> htmls = Main.getDocuments(story.getChapterUrls());
        // Parse chapter titles from chapters.
        ArrayList<String> titles = htmls.stream()
                .map(html -> html.select("div[style=\"text-align: center; font-weight: bold;\"]").first().text())
                .collect(Collectors.toCollection(ArrayList::new));
        // Create Chapter models.
        ArrayList<Chapter> chapters = new ArrayList<>();
        for (int i = 0; i < htmls.size(); i++) chapters.add(new Chapter(titles.get(i), htmls.get(i)));
        return chapters;
    }

    /**
     * Takes chapter HTML from a SIYE chapter and cleans it up, before putting it into the xhtml format required for an
     * ePUB.
     * @param chapter Chapter object containing HTML for a SIYE story chapter.
     */
    private void sanitizeChapter(Chapter chapter) {
        // Get the chapter's text, keeping all HTML formatting intact
        String chapterText = chapter.html.select("span span").first().html();
        // Create a new chapter HTML Document which is minimal.
        String newChapterHtml = String.format(C.CHAPTER_PAGE, chapter.title, chapter.title, chapterText);
        chapter.html = Jsoup.parse(newChapterHtml);
    }

    /**
     * Save the given story.
     * @param story    Story to save.
     * @param chapters Chapters of the story.
     */
    private void saveStory(SiyeStory story, ArrayList<Chapter> chapters) {
        // Create the directory if it doesn't already exist.
        Path storyDirPath = Main.dirPath.resolve(String.format("%s - %s", story.getAuthor(), story.getTitle()));
        File storyDir = storyDirPath.toFile();
        if (!storyDir.exists() && !storyDir.mkdir()) {
            System.err.printf(C.CREATE_DIR_FAILED, storyDir.getAbsolutePath());
            // Technically this might be just because of a fail file title... but we should just stop anyway.
            System.exit(1);
        }
        // Create style.css file.
        Main.saveFile(storyDirPath.resolve("style.css"), C.CSS.getBytes(StandardCharsets.UTF_8));
        // Create title.xhtml file.
        String titlePageText = String.format(C.TITLE_PAGE_SUMMARY, story.getTitle(), story.getAuthor(),
                story.getSummary(), story.getRating(), story.getWordCount(), chapters.size());
        Main.saveFile(storyDirPath.resolve("title.xhtml"), titlePageText.getBytes(StandardCharsets.UTF_8));
        // Save chapter file(s).
        for (int i = 0; i < chapters.size(); i++) {
            String chapterFileName = String.format("Chapter %d.xhtml", i + 1);
            Path chapterPath = storyDirPath.resolve(chapterFileName);
            byte[] chapterData = chapters.get(i).html.outerHtml().getBytes(StandardCharsets.UTF_8);
            Main.saveFile(chapterPath, chapterData);
        }
    }
}
