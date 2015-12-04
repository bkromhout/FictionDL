package bkromhout.FictionDL.Story;

import bkromhout.FictionDL.Chapter;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Base Story class.
 */
public class Story {
    // Story URL.
    protected String url;
    // Story ID.
    protected String storyId;
    // Story title.
    protected String title;
    // Story author.
    protected String author;
    // Site story is from (will be used as "Publisher" metadata).
    protected String site;
    // Story summary.
    protected String summary;
    // Story series.
    protected String series;
    // Story type.
    protected String ficType;
    // Story warnings.
    protected String warnings;
    // Story rating.
    protected String rating;
    // Story genre(s) (may be "None/Gen").
    protected String genres;
    // Story characters (and perhaps pairings, if they can be parsed.)
    protected String characters;
    // Story word count.
    protected int wordCount;
    // Date story was published.
    protected String datePublished;
    // Date story was last updated (may be the same as the publish date).
    protected String dateUpdated;
    // Story status ("Complete", "Incomplete", "Abandoned", etc.).
    protected String status;
    // List of chapter URLs.
    protected ArrayList<String> chapterUrls = new ArrayList<>();
    // List of chapters.
    protected ArrayList<Chapter> chapters = new ArrayList<>();

    /**
     * Parse the storyId of a story from its URL using regex.
     * @param url URL of story.
     * @param regex Regex that will help extract the story ID.
     * @param group Number of the group from the regex that will contain the story ID.
     * @return Story ID.
     */
    protected String parseStoryId(String url, String regex, int group) {
        Matcher matcher = Pattern.compile(regex).matcher(url);
        matcher.find();
        return matcher.group(group);
    }

    /**
     * Get story's URL.
     * @return Story URL.
     */
    public String getUrl() {
        return url;
    }

    /**
     * Get this story's ID
     * @return Story ID.
     */
    public String getStoryId() {
        return storyId;
    }

    /**
     * Get this story's title.
     * @return Story title.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Get this story's author.
     * @return Story author.
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Get story's origin site.
     * @return Story site.
     */
    public String getSite() {
        return site;
    }

    /**
     * Get this story's summary.
     * @return Story summary.
     */
    public String getSummary() {
        return summary;
    }

    /**
     * Get this story's series.
     * @return Story series.
     */
    public String getSeries() {
        return series;
    }

    /**
     * Get this story's type. (Ex: FFN's story type is are fandom categories like "Books > Harry Potter", SIYE's are
     * Harry Potter time period categories such as "Post-Hogwarts". Not all sites may have something worth filling
     * this in for.)
     * @return Story type.
     */
    public String getFicType() {
        return ficType;
    }

    /**
     * Get this story's warnings. Some sites may not support this.
     * @return Story warnings.
     */
    public String getWarnings() {
        return warnings;
    }

    /**
     * Get this story's rating.
     * @return Story rating.
     */
    public String getRating() {
        return rating;
    }

    /**
     * Get this story's genre(s). Might be "None/Gen".
     * @return Story genre(s).
     */
    public String getGenres() {
        return genres;
    }

    /**
     * Get this story's characters (and perhaps pairings, if they can be parsed).
     * @return Story characters.
     */
    public String getCharacters() {
        return characters;
    }

    /**
     * Get this story's word count.
     * @return Story word count.
     */
    public int getWordCount() {
        return wordCount;
    }

    /**
     * Get this story's chapter count.
     * @return Story chapter count.
     */
    public int getChapterCount() {
        return chapterUrls.size();
    }

    /**
     * Get this story's date published.
     * @return Date published.
     */
    public String getDatePublished() {
        return datePublished;
    }

    /**
     * Get this story's last update date.
     * @return Date updated.
     */
    public String getDateUpdated() {
        return dateUpdated;
    }

    /**
     * Get this story's status ("Complete", "Incomplete", etc.).
     * @return Story status.
     */
    public String getStatus() {
        return status;
    }

    /**
     * Get the chapter URLs for this story.
     * @return Story chapter URLs.
     */
    public ArrayList<String> getChapterUrls() {
        return chapterUrls;
    }

    /**
     * Get this story's chapters.
     * @return Story chapters.
     */
    public ArrayList<Chapter> getChapters() {
        return chapters;
    }

    /**
     * Set this Story's chapters.
     * @param chapters Chapters for this story.
     */
    public void setChapters(ArrayList<Chapter> chapters) {
        this.chapters = chapters;
    }
}
