package bkromhout.FictionDL.Downloader;

import bkromhout.FictionDL.C;
import bkromhout.FictionDL.Chapter;
import bkromhout.FictionDL.EpubGen;
import bkromhout.FictionDL.FictionDL;
import bkromhout.FictionDL.Story.SiyeStory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Entities;

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
    private ArrayList<String> urls;

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
        System.out.printf(C.STARTING_SITE_DL_PROCESS, SITE);
        // Create story models from URLs.
        System.out.printf(C.FETCH_BUILD_MODELS, SITE);
        ArrayList<SiyeStory> stories = new ArrayList<>();
        for (String url : urls) {
            try {
                stories.add(new SiyeStory(url));
            } catch (IOException e) {
                System.out.println(e.getMessage());
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
        // Sanitize the chapters so that they are in the expected xhtml format for ePUB, then add them to the story.
        System.out.println(C.SANITIZING_CHAPS);
        chapters.forEach(this::sanitizeChapter);
        story.setChapters(chapters);
        // Save the story as an ePUB.
        System.out.printf(C.SAVING_STORY);
        new EpubGen(story).makeEpub(FictionDL.dirPath);
        System.out.println(C.DONE + "\n"); // Add an empty line.
    }

    /**
     * Download the chapters for a story.
     * @param story Story to download chapters for.
     * @return ArrayList of Chapter objects.
     */
    private ArrayList<Chapter> downloadChapters(SiyeStory story) {
        // Download chapter HTML Documents.
        ArrayList<Document> htmls = FictionDL.getDocuments(story.getChapterUrls());
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
        // Make sure we aren't stripping dumb xhtml things to make pretty, modern html ;)
        chapter.html = Jsoup.parse(newChapterHtml);
        chapter.html.outputSettings().syntax(Document.OutputSettings.Syntax.xml);
        chapter.html.outputSettings().escapeMode(Entities.EscapeMode.xhtml);
    }
}
