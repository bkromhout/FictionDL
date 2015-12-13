package bkromhout.fdl.storys;

import bkromhout.fdl.C;
import bkromhout.fdl.Util;
import bkromhout.fdl.downloaders.ParsingDL;
import bkromhout.fdl.ex.InitStoryException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Model object for a FanFiction.net story.
 */
public class FanFictionStory extends Story {
    /**
     * FFN story link, just needs the story ID string substituted into it.
     */
    public static final String FFN_S_URL = "https://www.fanfiction.net/s/%s/1";
    /**
     * FFN story chapter link, just needs the story ID string and chapter number substituted into it.
     */
    private static final String FFN_C_URL = "https://www.fanfiction.net/s/%s/%d";
    /**
     * Regex to determine if a string contains a valid FFN genre. If .find() returns true, it does.
     */
    private static final String FFN_GENRE_REGEX = "\\QAdventure\\E|\\QAngst\\E|\\QCrime\\E|\\QDrama\\E|\\QFamily\\E" +
            "|\\QFantasy\\E|\\QFriendship\\E|\\QGeneral\\E|\\QHorror\\E|\\QHumor\\E|\\QHurt/Comfort\\E|\\QMystery\\E" +
            "|\\QParody\\E|\\QPoetry\\E|\\QRomance\\E|\\QSci-Fi\\E|\\QSpiritual\\E|\\QSupernatural\\E|\\QSuspense\\E" +
            "|\\QTragedy\\E|\\QWestern\\E";

    /**
     * Create a new FanFictionStory object based off of a URL.
     * @param ownerDl The parsing downloader which owns this story.
     * @param url     URL of the story this model represents.
     * @throws InitStoryException if we can't create this story object for some reason.
     */
    public FanFictionStory(ParsingDL ownerDl, String url) throws InitStoryException {
        super(ownerDl, url);
    }

    @Override
    protected void populateInfo() throws InitStoryException {
        // Set site.
        hostSite = C.HOST_FFN;
        // Get story ID first.
        storyId = parseStoryId(url, "/\\s/(\\d*)", 1);
        // Normalize the URL, since there are many valid FFN URL formats.
        url = String.format(FFN_S_URL, storyId);
        // Get the first chapter in order to parse the story info.
        Document infoDoc = Util.downloadHtml(url);
        // Make sure that we got a Document and that this is a valid story.
        if (infoDoc == null || infoDoc.select("span.gui_warning").first() != null) throw initEx();
        // Get the title.
        title = infoDoc.select("div#profile_top b").first().html().trim();
        // Get the author.
        author = infoDoc.select("div#profile_top a[href~=" + "/u/.*" + "]").first().html().trim();
        // Get the summary.
        summary = infoDoc.select("div#profile_top > div").first().html().trim();
        // Get the fic type.
        ficType = parseFicType(infoDoc);
        // Get the details string and split it up to help get the other details.
        Element detailElem = infoDoc.select("div#profile_top > span").last();
        String[] details = detailElem.text().split(" - ");
        // Get the rating.
        rating = details[0].replace("Rated: ", "").trim();
        // Get the chapter count, remember its index for later.
        int chapCntIdx = findDetailsStringIdx(details, "Chapters: ");
        int chapCount = chapCntIdx == -1 ? 1 : Integer.parseInt(details[chapCntIdx].replace("Chapters: ", "")
                .replace(",", "").trim());
        // Get the word count.
        int wordCntIdx = findDetailsStringIdx(details, "Words: ");
        wordCount = Integer.parseInt(details[wordCntIdx].replace("Words: ", "").replace(",", "").trim());
        // Figure genres and characters, either or both of which may be missing.
        if (chapCntIdx == 2 || wordCntIdx == 2) {
            // We don't have genres or characters, set them accordingly.
            genres = C.NO_GENRE;
            characters = C.NONE;
        } else if (chapCntIdx == 3 || (chapCntIdx == -1 && wordCntIdx == 3)) {
            // We have something at index 2, but we need to figure out if it's a genre or characters. This also means
            // that we only have one of either genres or characters, not both.
            Matcher genreMatcher = Pattern.compile(FFN_GENRE_REGEX).matcher(details[2]);
            if (genreMatcher.find()) {
                // This is the genres string, we don't have characters.
                genres = details[2].trim();
                characters = C.NONE;
            } else {
                // This is the characters string, we don't have genres.
                genres = C.NO_GENRE;
                characters = details[2].trim();
            }
        } else {
            // We have both a genres string and a characters string.
            genres = details[2].trim();
            characters = details[3].trim();
        }
        // Get the dates.
        Elements dates = detailElem.select("span > span");
        // Get the date published.
        datePublished = Util.dateFromFfnTime(dates.last().attr("data-xutime"));
        // Get the date last updated. If there isn't one, use the date published.
        dateUpdated = dates.size() > 1 ? Util.dateFromFfnTime(dates.first().attr("data-xutime")) : datePublished;
        // Get the status.
        status = findDetailsStringIdx(details, "Status: Complete") != -1 ? C.STAT_C : C.STAT_I;
        // Generate chapter URLs.
        for (int i = 0; i < chapCount; i++) chapterUrls.add(String.format(FFN_C_URL, storyId, i + 1));
    }

    /**
     * Parse the fic type.
     * @param infoDoc The info HTML.
     * @return The fic type.
     */
    private String parseFicType(Document infoDoc) {
        // We need to see if this is a crossover or not.
        Element element = infoDoc.select("img[src=\"/static/fcons/arrow-switch.png\"]").first();
        if (element == null) {
            // Regular, non-crossover fic.
            element = infoDoc.select("span.lc-left").first();
            StringBuilder ficType = new StringBuilder();
            for (Element part : element.select("a")) ficType.append(part.text()).append(" > ");
            return ficType.delete(ficType.length() - 3, ficType.length()).toString();
        } else {
            // Crossover fic.
            return element.nextElementSibling().text().trim();
        }
    }

    /**
     * Find the index of the details string that has the search string in it.
     * @param details The details string array.
     * @param search  The search string.
     * @return The index of the details string containing the search string, or -1.
     */
    private int findDetailsStringIdx(String[] details, String search) {
        for (int i = 0; i < details.length; i++) if (details[i].contains(search)) return i;
        return -1;
    }
}