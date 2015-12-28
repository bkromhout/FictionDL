package bkromhout.fdl.events;

/**
 * Used by {@link bkromhout.fdl.parsers.InputFileParser InputFileParser} to inform {@link
 * bkromhout.fdl.localfic.LocalStoryProcessor LocalStoryProcessor} of a new local story directory.
 */
public class AddLocalStoryDirEvent {
    /**
     * Name of the local story directory.
     */
    private String dirName;

    /**
     * Create a new {@link AddLocalStoryDirEvent}
     * @param dirName Name of the local story directory.
     */
    public AddLocalStoryDirEvent(String dirName) {
        this.dirName = dirName;
    }

    /**
     * Get the name of the local story directory.
     * @return Directory name.
     */
    public String getDirName() {
        return dirName;
    }
}
