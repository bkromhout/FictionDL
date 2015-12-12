package bkromhout.fdl.downloaders;

import bkromhout.fdl.*;
import bkromhout.fdl.storys.Story;
import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.stream.Collectors;

/**
 * Base class for downloaders which get stories by scraping their site HTML, and then generating story EPUB files by
 * parsing and cleaning the scraped HTML data.
 */
public abstract class ParsingDL extends Downloader {

    /**
     * CSS selector to extract chapter text from original HTML.
     */
    protected String chapTextSelector;

    /**
     * Create a new ParsingDL.
     * @param fictionDL        FictionDL object which owns this downloader.
     * @param storyClass       The class of Story which this downloader uses.
     * @param siteName         Human-readable site name for this downloader.
     * @param storyUrls        List of story URLs to be downloaded.
     * @param chapTextSelector CSS selector used to extract chapter text from original chapter HTMLs. (If all of the
     *                         chapter's text cannot be extracted with one CSS selector, the subclass will need to pass
     *                         null for this and override the extractChapText() method.)
     */
    protected ParsingDL(FictionDL fictionDL, Class<? extends Story> storyClass, String siteName,
                        HashSet<String> storyUrls, String chapTextSelector) {
        super(fictionDL, storyClass, siteName, storyUrls);
        this.chapTextSelector = chapTextSelector;
    }

    /**
     * Download the chapters of a story, get their titles, extract their content, then process everything and save the
     * story as an ePUB file.
     * <p>
     * For subclasses which choose to override this method: Make sure that if a story has been processed to the point
     * where it won't be touched again, the .storyProcessed() method is called. This call would not be necessary if, for
     * example, a story is passed to a different downloader which would call .storyProcessed() itself.
     * @param story Story to download and save.
     */
    @Override
    protected void downloadStory(Story story) {
        Util.logf(C.DL_CONTENT_FOR, Util.unEscapeAmps(story.getTitle()));
        // Download the chapters and fill in their titles.
        ArrayList<Chapter> chapters = downloadChapters(story);
        // Make sure we got all of the chapters. If we didn't we won't continue.
        if (story.getChapterUrls().size() != chapters.size()) {
            Util.log(C.SOME_CHAPS_FAILED);
            storyProcessed(); // Update progress.
            return;
        }
        // Extract the chapter text and sanitize it so that the chapters are in the expected xhtml format for ePUB.
        Util.log(C.SANITIZING_CHAPS);
        for (Chapter chapter : chapters) chapter.content = sanitizeChapter(extractChapText(chapter));
        // Associate the chapters with the story.
        story.setChapters(chapters);
        // Save the story as an ePUB file.
        Util.logf(C.SAVING_STORY);
        new EpubCreator(story).makeEpub(FictionDL.outPath);
        Util.log(C.DONE + "\n");
        storyProcessed(); // Update progress.
    }

    /**
     * Takes care of downloading the original chapter HTMLs and putting them into Chapter objects, then calls
     * generateChapTitles() to fill in the chapter titles.
     * @param story Story to download chapters for.
     * @return ArrayList of Chapter objects with their HTML added and titles generated.
     */
    private ArrayList<Chapter> downloadChapters(Story story) {
        // Download chapter HTML Documents.
        ArrayList<Document> htmls = Util.getDocuments(story.getChapterUrls(), cookies);
        // Create chapters without titles, those will be filled in by the overridden method.
        ArrayList<Chapter> chapters = htmls.stream().map(html -> new Chapter(null, html)).collect(
                Collectors.toCollection(ArrayList::new));
        // Generate titles.
        generateChapTitles(chapters);
        return chapters;
    }

    /**
     * Takes an array of Chapter objects which have had a site's original chapter HTML put into them, and creates
     * chapter titles for each.
     * <p>
     * By default, this method simply generates titles like "Chapter 1", "Chapter 2", etc. Subclasses should override
     * this method in order to generate more specific titles.
     * @param chapters List of Chapters.
     */
    protected void generateChapTitles(ArrayList<Chapter> chapters) {
        // Generate default chapter names in the format "Chapter #".
        for (int i = 0; i < chapters.size(); i++) chapters.get(i).title = String.format("Chapter %d", i + 1);
    }

    /**
     * Extract chapter text content from the chapter.
     * @param chapter Chapter object.
     * @return Chapter HTML, with chapter text extracted from original and put into template.
     */
    protected String extractChapText(Chapter chapter) {
        // Get the chapter's text, keeping all HTML formatting intact
        String chapterText = chapter.html.select(chapTextSelector).first().html();
        // Put the chapter's text into a chapter HTML template.
        return String.format(C.CHAPTER_PAGE, chapter.title, chapter.title, chapterText);
    }

    /**
     * Stub, just returns the input. Should be overridden to do anything special.
     * @param chapterString Input chapter HTML.
     * @return Same as input (subclasses can override to do site-specific sanitizing).
     */
    protected String sanitizeChapter(String chapterString) {
        return chapterString;
    }
}
