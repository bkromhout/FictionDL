package bkromhout.FictionDL.Story;

import bkromhout.FictionDL.Chapter;

import java.util.ArrayList;

/**
 * Base Story class.
 */
public class Story {
    // Story ID.
    protected String storyId;
    // Story title.
    protected String title;
    // Story author.
    protected String author;
    // Story summary.
    protected String summary;
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
     * Get this story's summary.
     * @return Story summary.
     */
    public String getSummary() {
        return summary;
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
     * Get this story's status ("Complete", "Incomplete", "Abandoned", etc.).
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
