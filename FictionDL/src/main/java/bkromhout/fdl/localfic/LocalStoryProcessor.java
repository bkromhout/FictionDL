package bkromhout.fdl.localfic;

import bkromhout.fdl.EpubCreator;
import bkromhout.fdl.FictionDL;
import bkromhout.fdl.events.AddLSDirNameEvent;
import bkromhout.fdl.ex.InitStoryException;
import bkromhout.fdl.ex.LocalStoryException;
import bkromhout.fdl.ex.StoryinfoJsonException;
import bkromhout.fdl.storys.LocalStory;
import bkromhout.fdl.util.C;
import bkromhout.fdl.util.IWorkProducer;
import bkromhout.fdl.util.ProgressHelper;
import bkromhout.fdl.util.Util;
import com.google.common.eventbus.Subscribe;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.regex.Pattern;

/**
 * Similar to how {@link bkromhout.fdl.FictionDL} delegates each site's story downloading process to a specific {@link
 * bkromhout.fdl.downloaders.Downloader}, it delegates to this class for all types of local stories.
 * @author Brenden Kromhout
 */
public class LocalStoryProcessor implements IWorkProducer {
    /**
     * FilenameFilter which ensures that we can find the storyinfo.json file in a case-insensitive way.
     */
    private static final FilenameFilter STORYINFO_JSON_FILTER = (dir, name) -> name.equalsIgnoreCase("storyinfo.json");
    /**
     * Pattern which matches filenames in the format "#.html". The "?i" indicates that the pattern is case insensitive.
     */
    private static final Pattern chapFileRegex = Pattern.compile("(?i)^\\d+\\.html$");

    /**
     * This is the directory which holds any local story directories.
     */
    private final Path baseDir;
    /**
     * List of story directory names. They are not guaranteed to be valid in any way (valid directory, valid local
     * story, etc.).
     */
    private final HashSet<String> storyDirNames;

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
        if (storyDirNames.size() == 0) return;
        Util.log(C.STARTING_LOCAL_STORY_PROCESS);

        // Check directory.
        Util.log(C.VALIDATING_LOCAL_STORY_DIRS);
        // Try to get a valid directory path from each directory name.
        ArrayList<Path> storyDirs = storyDirsFromDirNames();
        // Make sure that all story directories have a storyinfo.json file in them.
        checkForStoryinfoJsonFile(storyDirs);

        // Process all local stories.
        Util.log(C.CREATING_LOCAL_STORIES);
        storyDirs.forEach(this::processStory);

        Util.log(C.FINISHED_WITH_LOCAL_STORIES);
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

    /**
     * Iterates through the given list of local story directories and removes any that don't have a storyinfo.json file
     * in them.
     * @param storyDirs List of local story directory Paths.
     */
    private void checkForStoryinfoJsonFile(ArrayList<Path> storyDirs) {
        Iterator<Path> storyDirIterator = storyDirs.iterator();
        while (storyDirIterator.hasNext()) {
            Path storyDir = storyDirIterator.next();
            // Check to see if this story directory has a storyinfo.json file.
            if (storyDir.toFile().list(STORYINFO_JSON_FILTER).length != 1) {
                // This story directory doesn't have a storyinfo.json file, so we'll remove it from the list.
                Util.logf(C.NO_STORYINFO_JSON, storyDir.toString());
                storyDirIterator.remove();
                ProgressHelper.storyFailed(0L);
            }
        }
    }

    /**
     * Process a local story at the given path.
     * @param storyDir Story directory Path.
     */
    private void processStory(Path storyDir) {
        Util.loudf(C.CHECKING_LOCAL_STORY, storyDir.toString());
        // Read the storyinfo.json file into a JsonObject.
        Path storyInfoFile = storyDir.resolve(storyDir.toFile().list(STORYINFO_JSON_FILTER)[0]);
        JsonObject storyInfo;

        try (FileReader reader = new FileReader(storyInfoFile.toFile())) {
            storyInfo = new JsonParser().parse(reader).getAsJsonObject();
        } catch (IOException e) {
            // Shouldn't happen, but in case it does...
            e.printStackTrace();
            ProgressHelper.storyFailed(0L);
            return;
        } catch (JsonParseException | IllegalStateException e) {
            // Malformed JSON or getAsJsonObject() failed, respectively.
            Util.logf(C.MALFORMED_STORYINFO_JSON, storyDir.toString());
            ProgressHelper.storyFailed(0L);
            return;
        }

        // Check to make sure that the JSON is valid for our purposes.
        String title; // Need this for logging in a bit.
        try {
            // TODO make this use the utility methods to help us know what part failed and communicate that to the user.
            // Check that the meta object exists.
            if (!storyInfo.has(C.J_META)) throw new StoryinfoJsonException(storyDir, C.J_META);
            JsonObject meta = storyInfo.getAsJsonObject(C.J_META);

            // Check to make sure that meta elements exist.
            if (!meta.has(C.J_TYPE)) throw new StoryinfoJsonException(storyDir, C.J_TYPE);
            if (!meta.has(C.J_VERSION)) throw new StoryinfoJsonException(storyDir, C.J_VERSION);
            String type = meta.getAsJsonPrimitive(C.J_TYPE).getAsString();
            Integer version = meta.getAsJsonPrimitive(C.J_VERSION).getAsInt();

            // Check to make sure that the meta elements are correct.
            if (!C.J_TYPE_LS.equals(type)) throw new StoryinfoJsonException(storyDir, C.J_TYPE);
            if (version != 1) throw new StoryinfoJsonException(storyDir, C.J_VERSION);

            // Check that the info object exists.
            if (!storyInfo.has(C.J_INFO)) throw new StoryinfoJsonException(storyDir, C.J_INFO);
            JsonObject info = storyInfo.getAsJsonObject(C.J_INFO);

            // Check that the title and author elements exist and are nonnull and non-empty.
            if (!info.has(C.J_TITLE)) throw new StoryinfoJsonException(storyDir, C.J_TITLE);
            title = info.getAsJsonPrimitive(C.J_TITLE).getAsString();
            if (title == null || title.trim().isEmpty()) throw new StoryinfoJsonException(storyDir, C.J_TITLE);

            if (!info.has(C.J_AUTHOR)) throw new StoryinfoJsonException(title, C.J_AUTHOR);
            String author = info.getAsJsonPrimitive(C.J_AUTHOR).getAsString();
            if (author == null || author.trim().isEmpty()) throw new StoryinfoJsonException(title, C.J_AUTHOR);
        } catch (UnsupportedOperationException | ClassCastException | IllegalStateException e) {
            // Something wasn't what we expected, so we couldn't cast it. Sadly, we can't really know what part was
            // wrong, so we can't be very specific with the user.
            Util.logf(C.MALFORMED_STORYINFO_JSON, storyDir.toString());
            ProgressHelper.storyFailed(0L);
            return;
        } catch (StoryinfoJsonException e) {
            // Something was missing or invalid.
            Util.log(e.getMessage());
            ProgressHelper.storyFailed(0L);
            return;
        }

        // Create a LocalStory.
        Util.logf(C.PROCESSING_LOCAL_STORY, title);
        LocalStory story;
        try {
            story = new LocalStory(storyInfo, storyDir);
            // Figure out how many chapter files there are in the directory, store value in the LocalStory.
            int numChapFiles = storyDir.toFile().listFiles((dir, name) -> chapFileRegex.matcher(name).matches()).length;
            story.setNumChapFiles(numChapFiles);
        } catch (InitStoryException e) {
            // Issues while creating the local story.
            Util.log(e.getMessage());
            ProgressHelper.storyFailed(0L);
            return;
        }

        // Process chapters for the story.
        try {
            story.processChapters();
        } catch (LocalStoryException e) {
            // Ran into an issue while processing chapters for this local story. Print the message in the exception.
            Util.log(e.getMessage());
            ProgressHelper.storyFailed(story.getNumChapsLeft());
            return;
        }

        // Save the story as an ePUB file.
        Util.logf(C.SAVING_STORY);
        new EpubCreator(story).makeEpub(FictionDL.getOutPath());
        Util.log(C.DONE + C.N);
    }

    @Override
    public int getWorkCount() {
        return storyDirNames.size();
    }

    /**
     * When received, adds {@link AddLSDirNameEvent#getDirName()} to {@link #storyDirNames}. If the event indicates that
     * the folder is a parent folder (its subfolders are local story folders), then will instead add the names of its
     * subfolders to the list.
     * @param event Event instance.
     */
    @Subscribe
    public void onAddLSDirNameEvent(AddLSDirNameEvent event) {
        if (event.getDirName() != null) {
            if (event.isFolderOfLocalStories()) {
                // This folder's subfolders should be treated as local story folders, so we need to get a list of its
                // subfolders and add /those/ to the list.
                try {
                    // Get the folder and make sure that it /is/ a folder.
                    Path parentPath = baseDir.resolve(event.getDirName());
                    File parentFolder = parentPath.toFile();
                    if (!parentFolder.isDirectory()) throw new IllegalArgumentException();

                    // Get an array of subfolders.
                    File[] subfolders = parentFolder.listFiles(File::isDirectory);

                    // Add the subfolder names (with the parent's name and the path separator prepended) to the list.
                    for (File subfolder : subfolders)
                        storyDirNames.add(event.getDirName() + File.separator + subfolder.getName());
                } catch (IllegalArgumentException e) {
                    Util.logf(C.INVALID_STORIES_DIR, event.getDirName());
                }
            } else {
                // This is just one single story folder.
                storyDirNames.add(event.getDirName());
            }
        }
    }
}
