package bkromhout.FictionDL.Downloader;

import bkromhout.FictionDL.C;
import bkromhout.FictionDL.Chapter;
import bkromhout.FictionDL.FictionDL;
import bkromhout.FictionDL.Story.FictionHuntStory;
import bkromhout.FictionDL.Story.MuggleNetStory;
import bkromhout.FictionDL.Util;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Downloader for MuggleNet stories.
 */
public class MuggleNetDL extends ParsingDL {
    public static final String SITE = "FictionHunt";

    /**
     * Create a new MuggleNet downloader.
     * @param fictionDL FictionDL object which owns this downloader.
     * @param urls      List of MuggleNet URLs.
     */
    public MuggleNetDL(FictionDL fictionDL, ArrayList<String> urls) {
        super(fictionDL, urls, ); // TODO figure out
    }

    @Override
    public void download() {
        Util.logf(C.STARTING_SITE_DL_PROCESS, SITE);
        // Create story models from URLs.
        Util.logf(C.FETCH_BUILD_MODELS, SITE);
        ArrayList<MuggleNetStory> stories = new ArrayList<>();
        for (String url : storyUrls) {
            try {
                stories.add(new MuggleNetStory(url));
            } catch (IOException e) {
                storyProcessed(); // Call this, since we have "processed" a story by failing to download it.
                Util.log(e.getMessage());
            }
        }
        // Download and save the stories.
        Util.logf(C.DL_STORIES_FROM_SITE, SITE);
        stories.forEach(this::downloadStory);
    }

    /**
     * Generate chapter titles by parsing real titles from chapter HTML.
     * @param chapters List of Chapters.
     */
    @Override
    protected void generateChapTitles(ArrayList<Chapter> chapters) {
        // TODO DO THIS FOR MN!!!

        // Parse chapter titles from chapter HTMLs.
        for (Chapter chapter : chapters) {
            // Try to find a <select> element on the page that has chapter titles.
            Element titleElement = chapter.html.select().first();
            // If the story is chaptered, we'll find the <select> element and can get the chapter title from that (we
            // strip off the leading "#. " part of it). If the story is only one chapter, we just call it "Chapter 1".
            if (titleElement != null) {
                Matcher matcher = Pattern.compile(C.MN_CHAP_TITLE_REGEX).matcher(titleElement.html().trim());
                matcher.matches();
                try {
                    chapter.title = matcher.group(2);
                } catch (IllegalStateException e) {
                    // Apparently, it's possible for there to *not* be a title for a chapter, so the title string may
                    // look like "24. " or something. If that happens, title the chapter "Chapter #".
                    chapter.title = String.format("Chapter %d", chapters.indexOf(chapter) + 1);
                }
            } else {
                chapter.title = "Chapter 1"; // TODO check to see if it may have a title on MN
            }
        }
    }

    /**
     * MuggleNet needs to have number of elements removed for div.contentLeft, then we'll add <hr />s between story and
     * notes.
     * @param chapter Chapter object.
     * @return Chapter HTML, with chapter text extracted from original and put into template.
     */
    @Override
    protected String extractChapText(Chapter chapter) {
        // TODO this
        return null;
    }
}
