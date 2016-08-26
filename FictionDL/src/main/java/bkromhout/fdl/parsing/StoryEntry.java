package bkromhout.fdl.parsing;

import java.util.HashMap;

/**
 * Holds information about a story entry from the input file. This information will be used when creating the story
 * later.
 */
public class StoryEntry {
    /**
     * URL of the story.
     */
    private final String url;
    /**
     * Details about the story which were provided in the input file. These will override any equivalent details when
     * creating stories from sites which use parsing downloaders, but will be ignored for stories on sites where we
     * directly download an ePub file.
     */
    private final HashMap<String, String> detailTags;

    /**
     * Create a new {@link StoryEntry}.
     * @param url Story URL.
     */
    public StoryEntry(String url) {
        if (url == null || url.isEmpty()) throw new IllegalArgumentException("URL must not be null or empty.");
        this.url = url;
        this.detailTags = new HashMap<>();
    }

    /**
     * Get the story URL.
     * @return Story URL.
     */
    public String getUrl() {
        return url;
    }

    /**
     * Add a detail for the story which will override the equivalent detail parsed from the site.
     * @param tagName Detail tag name.
     * @param detail  Detail content.
     */
    public void addDetailTag(String tagName, String detail) {
        this.detailTags.put(tagName, detail);
    }

    /**
     * Copies the detailTags from anther {@link StoryEntry} to this one. Doesn't replace the URL.
     * @param detailTags A HashMap of story detail tags.
     */
    public void addDetailTags(HashMap<String, String> detailTags) {
        this.detailTags.putAll(detailTags);
    }

    /**
     * Get the detail tags map.
     * @return Detail tags.
     */
    public HashMap<String, String> getDetailTags() {
        return detailTags;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StoryEntry that = (StoryEntry) o;
        return url.equals(that.url);

    }

    @Override
    public int hashCode() {
        return url.hashCode();
    }
}
