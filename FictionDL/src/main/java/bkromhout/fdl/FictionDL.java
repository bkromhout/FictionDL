package bkromhout.fdl;

import bkromhout.fdl.downloaders.*;
import javafx.concurrent.Task;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;

/**
 * Fan Fiction Downloader. (Not to be confused wih the FanFictionDL, which is an actual site downloader, this class is a
 * main class which the program is named after.)
 * <p>
 * Originally only supported FictionHunt, but has been expanded to support other sites now as well.
 * <p>
 * Scrapes the story HTML and generates an ePUB using that.
 */
public class FictionDL {
    // Path to input file.
    private File inputFile;
    // Path where the input file resides, which is where stories will be saved.
    public static Path outPath;
    // Path to config file.
    private File configFile;
    // Configuration options.
    private ConfigFileParser.Config cfg;
    // File parser.
    public static LinkFileParser parser;
    // Keep a reference to the FictionDLTask if this is being run from the GUI.
    private FictionDLTask task;
    // Total number of stories which are to be downloaded, across all sites.
    private long totalNumStories;
    // Number of stories which have been processed (either have been downloaded or have failed to download).
    private long numStoriesProcessed = 0;

    /**
     * Create a new FictionDL to execute the program logic.
     * @param args Arguments, mapped to keys.
     * @throws IllegalArgumentException if either of the paths cannot be resolved.
     */
    public FictionDL(HashMap<String, String> args) throws IllegalArgumentException {
        this(args, null);
    }

    /**
     * Create a new FictionDL to execute the program logic.
     * @param args Arguments, mapped to keys.
     * @param task FictionDLTask, which won't be null if this FictionDL is being run from a GUI.
     * @throws IllegalArgumentException if either of the paths cannot be resolved.
     */
    public FictionDL(HashMap<String, String> args, FictionDLTask task) throws IllegalArgumentException {
        // Store the task (it might be null, that's fine).
        this.task = task;
        // If we're running from a GUI, go ahead and set the progress bar to indeterminate.
        if (task != null) task.updateProgress(-1, 0);
        // Make sure we have an input path.
        if (args.get(C.ARG_IN_PATH) == null) throw new IllegalArgumentException(C.NO_IN_PATH);
        // Try to get a file from the input file path.
        inputFile = Util.tryGetFile(args.get(C.ARG_IN_PATH));
        // Figure out the output directory.
        if (args.get(C.ARG_OUT_PATH) != null) {
            // An output directory was specified.
            outPath = Util.tryGetPath(args.get(C.ARG_OUT_PATH));
        } else {
            // If an output directory wasn't specified, use the directory of the input file.
            outPath = inputFile.getAbsoluteFile().getParentFile().toPath();
        }
        // Figure out the config file.
        if (args.get(C.ARG_CFG_PATH) != null) configFile = Util.tryGetFile(args.get(C.ARG_CFG_PATH));
    }

    /**
     * Actually do stuff.
     */
    public void run() {
        // Create a LinkFileParser to get the story URLs from the input file.
        parser = new LinkFileParser(inputFile);
        // If we have a config file, create a ConfigFileParser to get options.
        if (configFile != null) cfg = new ConfigFileParser(configFile).getConfig();
        // Figure out how many stories we're downloading, then download them.
        totalNumStories = parser.getTotalNumStories();
        getStories(parser);
        // All done!
        Util.log(C.ALL_FINISHED);
    }

    /**
     * Takes in a FileParse which has the various sites' URL lists and uses it to kick off the downloading process.
     * @param parser LinkFileParser which has successfully parsed input file.
     */
    private void getStories(LinkFileParser parser) {
        // Set progress bar to 0.
        if (task != null) task.updateProgress(numStoriesProcessed, totalNumStories);
        /*
        Download FictionHunt stories.
          */
        if (!parser.getFictionHuntUrls().isEmpty()) {
            FictionHuntDL fictionHuntDL = new FictionHuntDL(this, parser.getFictionHuntUrls());
            fictionHuntDL.download();
        }
        /*
        Download FanFiction.net stories.
          */
        if (!parser.getFfnUrls().isEmpty()) {
            FanFictionDL fanFictionDL = new FanFictionDL(this, parser.getFfnUrls());
            fanFictionDL.download();
        }
        /*
        Download SIYE stories.
          */
        if (!parser.getSiyeUrls().isEmpty()) {
            SiyeDL siyeDL = new SiyeDL(this, parser.getSiyeUrls());
            siyeDL.download();
        }
        /*
        Download MuggleNet stories.
         */
        if (!parser.getMnUrls().isEmpty()) {
            MuggleNetDL muggleNetDL = new MuggleNetDL(this, parser.getMnUrls());
            if (cfg != null && cfg.hasMnAuth())
                muggleNetDL.addAuth(cfg.getUsername(C.NAME_MN), cfg.getPassword(C.NAME_MN));
            muggleNetDL.download();
        }
        /*
        Download Ao3 stories.
         */
        if (!parser.getAo3Urls().isEmpty()) {
            Ao3DL ao3DL = new Ao3DL(this, parser.getAo3Urls());
            ao3DL.download();
        }
    }

    /**
     * Called by the downloaders each time a story has finished (or has failed to finish) downloading. If this FictionDL
     * is being run from a FictionDLTask, then the task's updateProgress() method will be called as a result.
     */
    public void incrProgress() {
        numStoriesProcessed++;
        if (task != null) task.updateProgress(numStoriesProcessed, totalNumStories);
    }

    /**
     * Subclass of Task so that FictionDL can be used by a JavaFX GUI app.
     */
    public static class FictionDLTask extends Task {
        private HashMap<String, String> args;

        /**
         * Create a new FictionDLTask.
         * @param args Arguments, mapped to keys.
         */
        public FictionDLTask(HashMap<String, String> args) {
            this.args = args;
        }

        @Override
        protected Object call() throws Exception {
            // Do cool stuff.
            new FictionDL(args, this).run();
            return null;
        }

        /**
         * Update the progress of the task.
         * @param done  Amount done.
         * @param total Total amount.
         */
        public void updateProgress(long done, long total) {
            super.updateProgress(done, total);
        }
    }
}
