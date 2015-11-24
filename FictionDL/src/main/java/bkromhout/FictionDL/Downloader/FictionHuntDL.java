package bkromhout.FictionDL.Downloader;

import bkromhout.FictionDL.C;
import bkromhout.FictionDL.Chapter;
import bkromhout.FictionDL.EpubGen;
import bkromhout.FictionDL.FictionDL;
import bkromhout.FictionDL.Story.FictionHuntStory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Entities;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;

/**
 * Downloader for FictionHunt stories.
 */
public class FictionHuntDL {
    public static final String SITE = "FictionHunt";
    // List of story URLs
    private ArrayList<String> urls;
    // Instance of an FFN downloader, created the first time it's needed, to easily download FFN stories.
    private FanfictionNetDL ffnDownloader = null;

    /**
     * Create a new FictionHunt downloader.
     * @param urls List of FictionHunt URLs.
     */
    public FictionHuntDL(ArrayList<String> urls) {
        this.urls = urls;
    }

    /**
     * Download the stories whose URLs were passed to this instance of the downloader upon creation.
     */
    public void download() {
        System.out.printf(C.STARTING_SITE_DL_PROCESS, SITE);
        // Create story models from URLs.
        System.out.printf(C.FETCH_BUILD_MODELS, SITE);
        ArrayList<FictionHuntStory> stories = new ArrayList<>();
        for (String url : urls) {
            try {
                stories.add(new FictionHuntStory(url));
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
        // Download and save the stories.
        System.out.printf(C.DL_STORIES_FROM_SITE, SITE);
        stories.forEach(this::downloadStory);
    }

    /**
     * Download the chapters of a story. If the story is still active on Fanfiction.net, an ePUB file will be downloaded
     * from p0ody-files. If not, it will be downloaded from FictionHunt by scraping and sanitizing the chapters.
     * @param story Story to download.
     */
    private void downloadStory(FictionHuntStory story) {
        System.out.printf(C.DL_CHAPS_FOR, story.getTitle());
        if (story.getFfnStoryId() != null) {
            // Story is still on Fanfiction.net, which is preferable since we can use p0ody-files to download the ePUB.
            if (ffnDownloader == null) ffnDownloader = new FanfictionNetDL(); // Get a FFN downloader instance.
            System.out.printf(C.FH_STORY_ON_FFN, story.getTitle());
            ffnDownloader.downloadByStoryId(story.getFfnStoryId(), story.getTitle());
        } else {
            // Story isn't on Fanfiction.net anymore, download directly from FictionHunt.
            // Get chapter documents, and make sure we didn't fail to get some chapter (and if we did, skip this story).
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
    }

    /**
     * Download the chapters for a story.
     * @param story Story to download chapters for.
     * @return ArrayList of Chapter objects.
     */
    private ArrayList<Chapter> downloadChapters(FictionHuntStory story) {
        // Download chapter HTML Documents.
        ArrayList<Document> htmls = FictionDL.getDocuments(story.getChapterUrls());
        // Generate chapter titles in the format "Chapter #"
        ArrayList<String> titles = new ArrayList<>();
        for (int i = 0; i < htmls.size(); i++) titles.add(String.format("Chapter %d", i + 1));
        // Create Chapter models.
        ArrayList<Chapter> chapters = new ArrayList<>();
        for (int i = 0; i < htmls.size(); i++) chapters.add(new Chapter(titles.get(i), htmls.get(i)));
        return chapters;
    }

    /**
     * Takes chapter HTML from a FictionHunt chapter and cleans it up, before putting it into the xhtml format required
     * for an ePUB.
     * @param chapter Chapter object containing HTML for a FictionHunt story chapter.
     */
    private void sanitizeChapter(Chapter chapter) {
        // Get the chapter's text, keeping all HTML formatting intact
        String chapterText = chapter.html.select("div.text").first().html();
        // Create a new chapter HTML Document which is minimal.
        String newChapterHtml = String.format(C.CHAPTER_PAGE, chapter.title, chapter.title, chapterText);
        // Make sure we aren't stripping dumb xhtml things to make pretty, modern html ;)
        chapter.html = Jsoup.parse(newChapterHtml);
        chapter.html.outputSettings().syntax(Document.OutputSettings.Syntax.xml);
        chapter.html.outputSettings().escapeMode(Entities.EscapeMode.xhtml);
    }
}
