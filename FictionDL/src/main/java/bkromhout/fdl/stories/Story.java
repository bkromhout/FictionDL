package bkromhout.fdl.stories;

import bkromhout.fdl.chapter.Chapter;
import bkromhout.fdl.ex.InitStoryException;
import bkromhout.fdl.parsing.StoryEntry;
import bkromhout.fdl.site.Site;
import bkromhout.fdl.util.C;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Base Story class from which site-specific story classes should be extended.
 */
public abstract class Story {

    // Site that story is from (name will be used as "Publisher" metadata).
    final Site site;
    // Detail tags from the input file. These will override corresponding details for stories obtained using a
    // parsing downloader.
    final HashMap<String, String> detailTags = new HashMap<>();
    // Story url.
    String url;
    // Story ID.
    String storyId;
    // Story title.
    String title;
    // Story author.
    String author;
    // Story summary.
    String summary;
    // Story series.
    String series;
    // Story type.
    String ficType;
    // Story warnings.
    String warnings;
    // Story rating.
    String rating;
    // Story genres (may be "None/Gen").
    String genres;
    // Story characters (and perhaps pairings, if they can be parsed).
    String characters;
    // Story word count.
    int wordCount;
    // Date story was published.
    String datePublished;
    // Date story was last updated (may be the same as the publish date).
    String dateUpdated;
    // Story status ("Complete", "Incomplete", etc.).
    String status;
    // List of chapter urls.
    final ArrayList<String> chapterUrls = new ArrayList<>();
    // List of chapters.
    ArrayList<Chapter> chapters = new ArrayList<>();

    /**
     * Create a new {@link Story}.
     * @param storyEntry Story entry with details from the input file.
     * @param site       Site that story is from.
     * @throws InitStoryException if we can't create this story object for some reason.
     */
    Story(StoryEntry storyEntry, Site site) throws InitStoryException {
        if (storyEntry != null) {
            this.url = storyEntry.getUrl();
            this.detailTags.putAll(storyEntry.getDetailTags());
        }
        this.site = site;
        populateInfo();
    }

    /**
     * Populate this model's fields.
     * @throws InitStoryException if we can't fill in all of the fields needed for this story object.
     */
    protected abstract void populateInfo() throws InitStoryException;

    /**
     * Parse the storyId of a story from its url using regex.
     * @param url   url of story.
     * @param regex Regex that will help extract the story ID.
     * @param group Number of the group from the regex that will contain the story ID.
     * @return Story ID, or null.
     * @throws InitStoryException if we can't parse the story ID from the url.
     */
    String parseStoryId(String url, String regex, int group) throws InitStoryException {
        Matcher matcher = Pattern.compile(regex).matcher(url);
        if (!matcher.find()) throw new InitStoryException(C.INVALID_URL, url);
        return matcher.group(group);
    }

    /**
     * Check to see if this story has a detail tag for the given tag name with a non-{@code null}, non-empty value.
     * @param tagName Detail tag name to check.
     * @return True if the given tag name is mapped to a non-{@code null}, non-empty value; otherwise false.
     */
    public boolean hasDetailTag(String tagName) {
        if (!detailTags.containsKey(tagName)) return false;
        String detail = detailTags.get(tagName);
        return detail != null && !detail.isEmpty();
    }

    /**
     * Get story's url.
     * @return Story url.
     */
    public String getUrl() {
        return url;
    }

    /**
     * Get detail tags for this story which came from the input file.
     * @return Story's detail tags.
     */
    public HashMap<String, String> getDetailTags() {
        return detailTags;
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
    public String getHost() {
        return site != null ? site.getHost() : null;
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
     * Get the number of chapter urls for this story.
     * @return Story chapter url count.
     */
    public int getChapterUrlCount() {
        return chapterUrls.size();
    }

    /**
     * Get this story's chapter count.
     * @return Story chapter count.
     */
    public int getChapterCount() {
        return chapters.size();
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
     * Get the chapter urls for this story.
     * @return Story chapter urls.
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
