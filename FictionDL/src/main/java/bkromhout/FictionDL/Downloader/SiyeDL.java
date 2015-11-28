package bkromhout.FictionDL.Downloader;

import bkromhout.FictionDL.C;
import bkromhout.FictionDL.Chapter;
import bkromhout.FictionDL.Story.SiyeStory;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Downloader for siye.co.uk ("Sink Into Your Eyes") stories.
 */
public class SiyeDL extends ParsingDL {
    public static final String SITE = "SIYE";

    /**
     * Create a new SIYE downloader.
     * @param urls List of SIYE URLs.
     */
    public SiyeDL(ArrayList<String> urls) {
        super(urls, null);
    }

    /**
     * Download the stories whose URLs were passed to this instance of the downloader upon creation..
     */
    public void download() {
        System.out.printf(C.STARTING_SITE_DL_PROCESS, SITE);
        System.out.println(C.SIYE_SLOW); // Inform the user that SIYE is slow and has crappy HTML structure.
        // Create story models from URLs.
        System.out.printf(C.FETCH_BUILD_MODELS, SITE);
        ArrayList<SiyeStory> stories = new ArrayList<>();
        for (String url : storyUrls) {
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
     * Generate chapter titles by parsing real titles from chapter HTML.
     * @param chapters List of Chapters.
     */
    @Override
    protected void generateChapTitles(ArrayList<Chapter> chapters) {
        // Parse chapter titles from chapter HTMLs.
        for (Chapter chapter : chapters) {
            // Try to find a <select> element on the page that has chapter titles.
            Element titleElement = chapter.html.select("select[name=\"chapter\"] option[selected]").first();
            // If the story is chaptered, we'll find the <select> element and can get the chapter title from that (we
            // strip off the leading "#. " part of it). If the story is only one chapter, we just call it "Chapter 1".
            if (titleElement != null) {
                Matcher matcher = Pattern.compile(C.SIYE_CHAP_TITLE_REGEX).matcher(titleElement.html().trim());
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
     * "Why is this method overridden?" you ask? Oh, right, it's because trying to parse stuff from SIYE is literally a
     * giant pain in the ass :)
     * @param chapter Chapter object.
     * @return Chapter HTML, with chapter text extracted from original and put into template.
     */
    @Override
    protected String extractChapText(Chapter chapter) {
        StringBuilder chapterText = new StringBuilder();
        // So, we need to get a number of things here. First off, we must grab the author's notes (if there are any).
        Element anElement = chapter.html.select("div#notes").first();
        if (anElement != null) chapterText.append(anElement.html()).append("<hr /><br />");
        // Then, we have to get the actual chapter text itself.
        chapterText.append(chapter.html.select("td[colspan=\"2\"] span").first().html());
        return String.format(C.CHAPTER_PAGE, chapter.title, chapter.title, chapterText.toString());
    }

    /**
     * Takes chapter HTML from a SIYE chapter and cleans it up, before putting it into the xhtml format required for an
     * ePUB.
     * @param chapterString Chapter's text content HTML for a FictionHunt story chapter.
     * @return Cleaned HTML.
     */
    @Override
    protected String sanitizeChapter(String chapterString) {
        // TODO strip extra <br>s first!
        return chapterString;
    }
}
