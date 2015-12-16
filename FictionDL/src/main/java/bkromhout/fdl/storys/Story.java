package bkromhout.fdl.storys;

import bkromhout.fdl.C;
import bkromhout.fdl.Chapter;
import bkromhout.fdl.downloaders.Downloader;
import bkromhout.fdl.ex.InitStoryException;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Base Story class from which site-specific story classes should extend.
 */
public abstract class Story {
    /**
     * Indicates a URL is malformed.
     */
    private static final String BAD_URL = "BAD_URL";
    /**
     * Indicates we couldn't find an ePUB file to download for a story.
     */
    public static final String NO_EPUB = "NO_EPUB";

    // The downloader which owns this story.
    protected Downloader ownerDl;
    // Story URL.
    protected String url;
    // Story ID.
    protected String storyId;
    // Story title.
    protected String title;
    // Story author.
    protected String author;
    // Site story is from (will be used as "Publisher" metadata).
    protected String hostSite;
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
    // Story genres (may be "None/Gen").
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
     * Create a new Story.
     * @param ownerDl The downloader which owns this story.
     * @param url     Story URL.
     * @throws InitStoryException if we can't create this story object for some reason.
     */
    protected Story(Downloader ownerDl, String url) throws InitStoryException {
        this.ownerDl = ownerDl;
        this.url = url;
        populateInfo();
    }

    /**
     * Populate this model's fields.
     * @throws InitStoryException if we can't fill in all of the fields needed for this story object.
     */
    protected abstract void populateInfo() throws InitStoryException;

    /**
     * Parse the storyId of a story from its URL using regex.
     * @param url   URL of story.
     * @param regex Regex that will help extract the story ID.
     * @param group Number of the group from the regex that will contain the story ID.
     * @return Story ID, or null.
     * @throws InitStoryException if we can't parse the story ID from the URL.
     */
    protected String parseStoryId(String url, String regex, int group) throws InitStoryException {
        Matcher matcher = Pattern.compile(regex).matcher(url);
        if (!matcher.find()) throw initEx(BAD_URL, url);
        return matcher.group(group);
    }

    /**
     * Throw a InitStoryException with some message about why we couldn't create this story.
     * @return InitStoryException with the message we figure out.
     */
    protected InitStoryException initEx() {
        return initEx(null, null);
    }

    /**
     * Throw a InitStoryException with some message about why we couldn't create this story.
     * @param assist String to help us figure out why we couldn't create this story, and thus what message to put in the
     *               exception.
     * @return InitStoryException with the message we figure out.
     */
    protected InitStoryException initEx(String assist) {
        return initEx(assist, null);
    }

    /**
     * Throw a InitStoryException with some message about why we couldn't create this story.
     * @param assist String to help us figure out why we couldn't create this story, and thus what message to put in the
     *               exception.
     * @param str1   The first string to substitute into some message.
     * @return InitStoryException with the message we figure out.
     */
    protected InitStoryException initEx(String assist, String str1) {
        if (assist == null)
            return new InitStoryException(String.format(C.STORY_DL_FAILED, ownerDl.getSiteName(), storyId));
        switch (assist) {
            case BAD_URL:
                // URL was bad. str1 is the malformed URL.
                return new InitStoryException(String.format(C.INVALID_URL, str1));
            case NO_EPUB:
                // Couldn't find an ePUB file to download. str1 is the story title.
                return new InitStoryException(String.format(C.NO_EPUB_ON_SITE, ownerDl.getSiteName(), str1));
            case MuggleNetStory.MN_REG_USERS_ONLY:
                // Need to login to MuggleNet.
                return new InitStoryException(String.format(C.MUST_LOGIN, ownerDl.getSiteName(), storyId));
            default:
                // Default string.
                return new InitStoryException(String.format(C.STORY_DL_FAILED, ownerDl.getSiteName(), storyId));
        }
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
     * Get story's host site. This string is site domain, not the human readable site name.
     * @return Story site.
     */
    public String getHostSite() {
        return hostSite;
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
     * Harry Potter time period categories such as "Post-Hogwarts". Not all sites may have something worth filling this
     * in for.)
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
     * Get this story's genres. Might be "None/Gen".
     * @return Story genres.
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
     * Compares the two Chapters by their {@link Chapter#url url} fields along with this Story's list of chapter URLs.
     * Gets the indices of the given Chapters' URLs in this Story's chapter URL list and returns an integer to indicate
     * which comes first using {@link Integer#compare(int, int)}.
     * <p>
     * We call this "slow" because the sorting requires information outside of the given Chapter objects.
     * @param c1 One Chapter object.
     * @param c2 Another Chapter object.
     * @return Integer which indicates order of c1 and c2.
     * @see Story
     * @see Chapter
     */
    public int slowChapSort(Chapter c1, Chapter c2) {
        return Integer.compare(chapterUrls.indexOf(c1.url), chapterUrls.indexOf(c2.url));
    }

    /**
     * Compares the two Chapters by their {@link Chapter#number number} fields using {@link Integer#compare(int, int)}.
     * <p>
     * We call this "fast" because the sorting only needs the Chapter objects.
     * @param c1 One Chapter object.
     * @param c2 Another Chapter object.
     * @return Integer which indicates order of c1 and c2.
     * @see Chapter
     */
    public int fastChapSort(Chapter c1, Chapter c2) {
        return Integer.compare(c1.number, c2.number);
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
