package bkromhout.fdl.localfic;

import bkromhout.fdl.events.AddLocalStoryDirEvent;
import bkromhout.fdl.util.IWorkProducer;
import com.google.common.eventbus.Subscribe;

import java.nio.file.Path;
import java.util.HashSet;

/**
 * Similar to how {@link bkromhout.fdl.FictionDL} delegates each site's story downloading process to a specific {@link
 * bkromhout.fdl.downloaders.Downloader}, it delegates to this class for all types of local stories.
 */
public class LocalStoryProcessor implements IWorkProducer {
    /**
     * This is the directory which holds any local story directories.
     */
    private Path baseDir;
    /**
     * List of story directory names. They are not guaranteed to be valid in any way (valid directory, valid local
     * story, etc.).
     */
    private HashSet<String> storyDirs;

    /**
     * Create a new {@link LocalStoryProcessor}.
     * @param baseDir Directory to look for local stories in.
     */
    public LocalStoryProcessor(Path baseDir) {
        this.baseDir = baseDir;
        this.storyDirs = new HashSet<>();
    }

    /**
     * Try to process and create stories for all of the directories in {@link #storyDirs}.
     */
    public void process() {
        // TODO
    }

    @Override
    public int getWorkCount() {
        return storyDirs.size();
    }

    /**
     * When received, adds {@link AddLocalStoryDirEvent#getDirName()} to {@link #storyDirs}.
     * @param event Event instance.
     */
    @Subscribe
    public void onAddLocalStoryDirEvent(AddLocalStoryDirEvent event) {
        if (event.getDirName() != null) storyDirs.add(event.getDirName());
    }
}
