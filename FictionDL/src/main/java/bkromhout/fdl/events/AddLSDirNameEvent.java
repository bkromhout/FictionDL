package bkromhout.fdl.events;

/**
 * Used by {@link bkromhout.fdl.parsers.InputFileParser InputFileParser} to inform {@link
 * bkromhout.fdl.localfic.LocalStoryProcessor LocalStoryProcessor} of a new local story directory name.
 */
public class AddLSDirNameEvent {
    /**
     * Name of the local story directory.
     */
    private final String dirName;
    /**
     * If true, we will treat the named directory as one which contains multiple story directories inside of it.
     */
    private final boolean isFolderOfLocalStories;

    /**
     * Create a new {@link AddLSDirNameEvent}.
     * @param dirName Name of the local story directory.
     */
    public AddLSDirNameEvent(String dirName) {
        this(dirName, false);
    }

    /**
     * Create a new {@link AddLSDirNameEvent}.
     * @param dirName                Name of the local story directory.
     * @param isFolderOfLocalStories If true, we will treat the named directory as one which contains multiple story
     *                               directories inside of it.
     */
    public AddLSDirNameEvent(String dirName, boolean isFolderOfLocalStories) {
        this.dirName = dirName;
        this.isFolderOfLocalStories = isFolderOfLocalStories;
    }

    /**
     * Get the name of the local story directory.
     * @return Directory name.
     */
    public String getDirName() {
        return dirName;
    }

    /**
     * Get whether or not this is the name of a folder whose subfolders should all be treated as local story folders.
     * @return True if so, otherwise false.
     */
    public boolean isFolderOfLocalStories() {
        return isFolderOfLocalStories;
    }
}
