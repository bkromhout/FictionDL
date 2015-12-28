package bkromhout.fdl.localfic;

import bkromhout.fdl.events.AddLSDirNameEvent;
import bkromhout.fdl.util.C;
import bkromhout.fdl.util.IWorkProducer;
import bkromhout.fdl.util.ProgressHelper;
import bkromhout.fdl.util.Util;
import com.google.common.eventbus.Subscribe;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
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
    private HashSet<String> storyDirNames;

    /**
     * Create a new {@link LocalStoryProcessor}.
     * @param baseDir Directory to look for local stories in.
     */
    public LocalStoryProcessor(Path baseDir) {
        this.baseDir = baseDir;
        this.storyDirNames = new HashSet<>();
    }

    /**
     * Try to process and create stories for all of the directories in {@link #storyDirNames}.
     */
    public void process() {
        Util.log(C.STARTING_LOCAL_STORY_PROCESS);

        Util.log(C.CHECKING_LOCAL_STORY_DIRS);
        // Try to get a valid directory path from each directory name.
        ArrayList<Path> storyDirs = storyDirsFromDirNames();

        //
    }

    /**
     * Return a list of valid story directory Paths that were resolved from the directory names in {@link
     * #storyDirNames}, which it is assumed contains no nulls or empty strings.
     * @return List of story directory Paths.
     */
    private ArrayList<Path> storyDirsFromDirNames() {
        ArrayList<Path> storyDirs = new ArrayList<>();
        for (String dirName : storyDirNames) {
            try {
                // Try to resolve path.
                Path dirPath = baseDir.resolve(dirName);
                // Make sure that it's a directory.
                if (!Files.isDirectory(dirPath)) throw new IllegalArgumentException();
                // Then add it to the list.
                storyDirs.add(dirPath);
            } catch (IllegalArgumentException e) {
                // Invalid directory.
                Util.logf(C.INVALID_STORY_DIR, dirName);
                ProgressHelper.storyFailed(0L);
            }
        }
        return storyDirs;
    }

    @Override
    public int getWorkCount() {
        return storyDirNames.size();
    }

    /**
     * When received, adds {@link AddLSDirNameEvent#getDirName()} to {@link #storyDirNames}.
     * @param event Event instance.
     */
    @Subscribe
    public void onAddLSDirNameEvent(AddLSDirNameEvent event) {
        if (event.getDirName() != null) storyDirNames.add(event.getDirName());
    }
}
