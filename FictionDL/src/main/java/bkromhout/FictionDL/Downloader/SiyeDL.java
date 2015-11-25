package bkromhout.FictionDL.Downloader;

import bkromhout.FictionDL.*;
import bkromhout.FictionDL.Story.SiyeStory;
import bkromhout.FictionDL.Story.Story;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Downloader for siye.co.uk ("Sink Into Your Eyes").
 */
public class SiyeDL extends ParsingDL {
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
     * Download the chapters for a story.
     * @param story Story to download chapters for.
     * @return ArrayList of Chapter objects.
     */
    @Override
    protected ArrayList<Chapter> downloadChapters(Story story) {
        ArrayList<Chapter> chapters = super.downloadChapters(story);
        // Parse chapter titles from chapter HTMLs.
        for (Chapter chapter : chapters) {
            // Try to find a <select> element on the page that has chapter titles.
            Element titleElement = chapter.html.select("select[name=\"chapter\"] option[selected]").first();
            // If the story is chaptered, we'll find the <select> element and can get the chapter title from that (we
            // strip off the leading "#. " part of it). If the story is only one chapter, we just call it "Chapter 1".
            if (titleElement != null) {
                Matcher matcher = Pattern.compile(C.SIYE_CHAP_TITLE_REGEX).matcher(titleElement.html().trim());
                matcher.matches();
                chapter.title = matcher.group(2);
            } else {
                chapter.title = "Chapter 1";
            }
        }
        return chapters;
    }

    /**
     * "Why is this method overridden?" you ask? Oh, right, it's because trying to parse stuff from SIYE is literally a
     * giant pain in the ass :)
     * @param chapter Chapter object.
     * @return Chapter content HTML, extracted from original chapter HTML.
     */
    @Override
    protected String extractChapText(Chapter chapter) {
        StringBuilder chapterText = new StringBuilder();
        // So, we need to get a number of things here. First off, we must grab the author's notes (if there are any).
        Element anElement = chapter.html.select("div#notes").first();
        if (anElement != null) chapterText.append(anElement.html());
        // Then, we have to get the actual chapter text itself.
        chapterText.append(chapter.html.select("td[colspan=\"2\"] span").first().html());
        return chapterText.toString();
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
