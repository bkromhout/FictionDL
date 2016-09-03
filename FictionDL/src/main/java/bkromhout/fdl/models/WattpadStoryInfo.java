package bkromhout.fdl.models;

import com.google.gson.annotations.SerializedName;

/**
 * Holds information about a Wattpad story retrieved from the Wattpad API.
 */
public class WattpadStoryInfo {
    /**
     * Story ID.
     */
    private long id;
    /**
     * Story title.
     */
    private String title;
    /**
     * Story author info.
     */
    private WattpadUserInfo user;
    /**
     * Story creation date.
     */
    private String createDate;
    /**
     * Story modification date.
     */
    private String modifyDate;
    /**
     * Story description.
     */
    private String description;
    /**
     * Story tags.
     */
    private String[] tags;
    /**
     * Story cover image url.
     */
    @SerializedName("cover")
    private String coverUrl;
    /**
     * Whether or not the story is completed.
     */
    private boolean completed;
    /**
     * Whether or not the story is mature.
     */
    private boolean mature;
    /**
     * Story url.
     */
    private String url;
    /**
     * Number of chapters.
     */
    private int numParts;
    /**
     * Chapter info objects.
     */
    private WattpadChapterInfo[] parts;

    /**
     * Get the story ID.
     * @return Story ID.
     */
    public long getId() {
        return id;
    }

    /**
     * Get the story title.
     * @return Story title.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Get the story author.
     * @return Story author.
     */
    public String getAuthor() {
        return user.name;
    }

    /**
     * Get the story creation date.
     * @return Story creation date.
     */
    public String getCreateDate() {
        return createDate;
    }

    /**
     * Get the story modification date.
     * @return Story modification date.
     */
    public String getModifyDate() {
        return modifyDate;
    }

    /**
     * Get the story description.
     * @return Story description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Get the story tags.
     * @return Story tags.
     */
    public String[] getTags() {
        return tags;
    }

    /**
     * Get the story cover image url.
     * @return Story cover image url.
     */
    public String getCoverUrl() {
        return coverUrl;
    }

    /**
     * Get whether or not the story is completed.
     * @return Whether or not the story is completed.
     */
    public boolean isCompleted() {
        return completed;
    }

    /**
     * Get whether or not the story is mature.
     * @return Whether or not the story is mature.
     */
    public boolean isMature() {
        return mature;
    }

    /**
     * Get the story url.
     * @return Story url.
     */
    public String getUrl() {
        return url;
    }

    /**
     * Get the number of chapters.
     * @return Number of chapters.
     */
    public int getNumParts() {
        return numParts;
    }

    /**
     * Get the story chapter info objects.
     * @return Story chapter info objects.
     */
    public WattpadChapterInfo[] getParts() {
        return parts;
    }

    /**
     * Wattpad user info.
     */
    private static class WattpadUserInfo {
        /**
         * User name.
         */
        private String name;
    }
}
