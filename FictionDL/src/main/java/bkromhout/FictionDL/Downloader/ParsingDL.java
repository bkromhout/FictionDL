package bkromhout.FictionDL.Downloader;

import bkromhout.FictionDL.*;
import bkromhout.FictionDL.Story.Story;
import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Base class for a downloader that has to parse text (as opposed to just downloading an ePUB file, like the
 * Fanfiction.net downloader for example). Downloaders which have to do parsing will also have a corresponding *Story
 * class which subclasses the Story class.
 */
public class ParsingDL {
    // CSS selector to extract chapter text from original HTML.
    protected String chapTextSelector;

    protected void downloadStory(Story story) {
        // Get chapter documents, and make sure we didn't fail to get some chapter (and if we did, skip this story).
        System.out.printf(C.DL_CHAPS_FOR, story.getTitle());
        ArrayList<Chapter> chapters = downloadChapters(story);
        if (story.getChapterUrls().size() != chapters.size()) {
            System.out.println(C.SOME_CHAPS_FAILED);
            return;
        }
        // Sanitize the chapters so that they are in the expected xhtml format for ePUB, then add them to the story.
        System.out.println(C.SANITIZING_CHAPS);
        for (Chapter chapter : chapters) chapter.content = sanitizeChapter(extractChapText(chapter));
        story.setChapters(chapters);
        // Save the story as an ePUB.
        System.out.printf(C.SAVING_STORY);
        new EpubGen(story).makeEpub(FictionDL.dirPath);
        System.out.println(C.DONE + "\n"); // Add an empty line.
    }

    /**
     * Takes care of downloading the original chapter HTMLs and putting them into Chapter objects, but leaves title
     * creation up to the sub-class's overridden version of this method.
     * @param story Story to download chapters for.
     * @return ArrayList of Chapter objects with their HTML added, but without titles created.
     */
    protected ArrayList<Chapter> downloadChapters(Story story) {
        // Download chapter HTML Documents.
        ArrayList<Document> htmls = Util.getDocuments(story.getChapterUrls());
        // Create chapters without titles, those will be filled in by the
        return htmls.stream().map(html -> new Chapter(null, html)).collect(Collectors.toCollection(ArrayList::new));
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
