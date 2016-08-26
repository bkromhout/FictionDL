package bkromhout.fdl.stories;

import bkromhout.fdl.ex.InitStoryException;
import bkromhout.fdl.parsing.StoryEntry;
import bkromhout.fdl.site.Sites;
import bkromhout.fdl.util.C;
import bkromhout.fdl.util.Util;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.DateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Model object for a <a href="https://www.fanfiction.net">FanFiction.net</a> story.
 */
public class FanFictionStory extends Story {
    /**
     * FFN story link template, just needs the story ID string substituted into it.
     */
    public static final String FFN_S_URL = "https://www.fanfiction.net/s/%s/1";
    /**
     * FFN story chapter link template, just needs the story ID string and chapter number substituted into it.
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
     * Create a new {@link FanFictionStory} based off of a url.
     * @param storyEntry Story entry with details from the input file.
     * @throws InitStoryException if we can't create this story object for some reason.
     */
    public FanFictionStory(StoryEntry storyEntry) throws InitStoryException {
        super(storyEntry, Sites.FFN());
    }

    @Override
    protected void populateInfo() throws InitStoryException {
        // Get story ID and use it to normalize the url, then download the url so that we can parse story info.
        storyId = parseStoryId(url, "/s/(\\d*)", 1);
        url = String.format(FFN_S_URL, storyId);
        Document infoDoc = Util.getHtml(url);
        // Make sure that we got a Document and that this is a valid story.
        if (infoDoc == null || infoDoc.select("span.gui_warning").first() != null)
            throw new InitStoryException(C.STORY_DL_FAILED, site.getName(), storyId);

        title = hasDetailTag(C.J_TITLE) ? detailTags.get(C.J_TITLE)
                : infoDoc.select("div#profile_top b").first().html().trim();
        author = hasDetailTag(C.J_AUTHOR) ? detailTags.get(C.J_AUTHOR)
                : infoDoc.select("div#profile_top a[href~=" + "/u/.*" + "]").first().html().trim();
        summary = hasDetailTag(C.J_SUMMARY) ? detailTags.get(C.J_SUMMARY)
                : infoDoc.select("div#profile_top > div").first().html().trim();
        ficType = hasDetailTag(C.J_FIC_TYPE) ? detailTags.get(C.J_FIC_TYPE)
                : parseFicType(infoDoc); // FFN fandom/crossover fandom

        // Get the details string and split it up to help get the other details.
        Element detailElem = infoDoc.select("div#profile_top > span").last();
        String[] details = detailElem.text().split(" - ");

        rating = hasDetailTag(C.J_RATING) ? detailTags.get(C.J_RATING)
                : details[0].replace("Rated: ", "").trim();

        // Get the chapter count, remember its index for later.
        int chapCntIdx = findDetailsStringIdx(details, "Chapters: ");
        int chapCount = chapCntIdx == -1 ? 1 :
                Integer.parseInt(details[chapCntIdx].replace("Chapters: ", "").replace(",", "").trim());

        // Get the word count, remember its index for later too.
        int wordCntIdx = findDetailsStringIdx(details, "Words: ");
        wordCount = Integer.parseInt(details[wordCntIdx].replace("Words: ", "").replace(",", "").trim());

        // Figure out genres and characters, either or both of which may be missing.
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

        // However, we might just override that work we just did for genres/characters ;)
        if (hasDetailTag(C.J_GENRES)) genres = detailTags.get(C.J_GENRES);
        if (hasDetailTag(C.J_CHARACTERS)) characters = detailTags.get(C.J_CHARACTERS);

        // Get the dates.
        Elements dates = detailElem.select("span > span");
        datePublished = dateFromFfnTime(dates.last().attr("data-xutime"));
        dateUpdated = dates.size() > 1 ? dateFromFfnTime(dates.first().attr("data-xutime")) : datePublished;

        status = findDetailsStringIdx(details, "Status: Complete") != -1 ? C.STAT_C : C.STAT_I;

        // Detail tags which the site doesn't support.
        if (hasDetailTag(C.J_SERIES)) series = detailTags.get(C.J_SERIES);
        if (hasDetailTag(C.J_WARNINGS)) warnings = detailTags.get(C.J_WARNINGS);

        // Generate chapter urls.
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


    /**
     * Takes a long time value that was parsed from FanFiction.net, multiplies it by 1000 to make it match the Java long
     * time format, then returns a string with it printed in the format MMM dd, yyyy.
     * @param ffnTime String long value from an FFN data-xutime attribute.
     * @return Date string.
     */
    private String dateFromFfnTime(String ffnTime) {
        // Add some zeros to make it like a Java long.
        long longFfnTime = Long.parseLong(ffnTime);
        longFfnTime *= 1000;
        // Create a Date object.
        Date date = new Date(longFfnTime);
        // Create the formatter.
        DateFormat df = DateFormat.getDateInstance();
        df.setTimeZone(TimeZone.getTimeZone("US/Pacific"));
        // Return string.
        return df.format(date);
    }
}
