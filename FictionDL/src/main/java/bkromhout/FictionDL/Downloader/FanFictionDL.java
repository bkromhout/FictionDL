package bkromhout.FictionDL.Downloader;

import bkromhout.FictionDL.C;
import bkromhout.FictionDL.Chapter;
import bkromhout.FictionDL.FictionDL;
import bkromhout.FictionDL.Story.FanFictionStory;
import bkromhout.FictionDL.Util;
import bkromhout.FictionDL.ex.InitStoryException;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Downloader for FanFiction.net stories.
 */
public class FanFictionDL extends ParsingDL {
    /**
     * Create a new FanFiction.net downloader.
     * @param fictionDL FictionDL object which owns this downloader.
     * @param urls      List of FanFiction.net URLs.
     */
    public FanFictionDL(FictionDL fictionDL, ArrayList<String> urls) {
        super(fictionDL, "FanFiction.net", urls, "div#storytext");
    }

    /**
     * Download the stories whose URLs were passed to this instance of the downloader upon creation.
     */
    @Override
    public void download() {
        Util.logf(C.STARTING_SITE_DL_PROCESS, site);
        // Create story models from URLs.
        Util.logf(C.FETCH_BUILD_MODELS, site);
        ArrayList<FanFictionStory> stories = new ArrayList<>();
        for (String url : storyUrls) {
            try {
                stories.add(new FanFictionStory(url));
            } catch (InitStoryException e) {
                storyProcessed(); // Call this, since we have "processed" a story by failing to download it.
                Util.log(e.getMessage());
            }
        }
        // Download and save the stories.
        Util.logf(C.DL_STORIES_FROM_SITE, site);
        stories.forEach(this::downloadStory);
    }

    /**
     * Generate chapter titles by parsing real titles from chapter HTML.
     * @param chapters List of Chapters.
     */
    @Override
    protected void generateChapTitles(ArrayList<Chapter> chapters) {
        // Parse chapter titles from chapter HTMLs.
        for (Chapter chapter : chapters) {
            // Try to find a <select> element on the page that has chapter titles.
            Element titleElement = chapter.html.select("select#chap_select > option[selected]").first();
            // If the story is chaptered, we'll find the <select> element and can get the chapter title from that (we
            // strip off the leading "#. " part of it). If the story is only one chapter, we just call it "Chapter 1".
            if (titleElement != null) {
                Matcher matcher = Pattern.compile(C.FFN_CHAP_TITLE_REGEX).matcher(titleElement.html().trim());
                matcher.matches();
                try {
                    chapter.title = matcher.group(2);
                } catch (IllegalStateException e) {
                    // Apparently, it's possible for there to *not* be a title for a chapter, so the title string may
                    // look like "24. " or something. If that happens, title the chapter "Chapter #".
                    chapter.title = String.format("Chapter %d", chapters.indexOf(chapter) + 1);
                }
            } else {
                chapter.title = "Chapter 1";
            }
        }
    }

    /**
     * Takes chapter HTML from a FanFiction.net chapter and cleans it up, before putting it into the xhtml format
     * required for an ePUB.
     * @param chapterString Chapter's text content HTML for a FanFiction.net story chapter.
     * @return Cleaned HTML.
     */
    @Override
    protected String sanitizeChapter(String chapterString) {
        // Do some FanFiction.net specific cleaning.
        chapterString = chapterString.replace("noshade", "noshade=\"noshade\"");
        return chapterString;
    }
}
