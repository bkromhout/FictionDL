package bkromhout.FictionDL;

import bkromhout.FictionDL.Downloader.FanFictionDL;
import bkromhout.FictionDL.Downloader.FictionHuntDL;
import bkromhout.FictionDL.Downloader.SiyeDL;
import javafx.concurrent.Task;

import java.io.File;
import java.nio.file.Path;

/**
 * Fan Fiction Downloader.
 * <p>
 * Originally only supported FictionHunt, but has been expanded to support other sites now as well.
 * <p>
 * Will download stories as ePUB if possible or will scrape the story HTML and generate an ePUB using that.
 */
public class FictionDL {
    // Path to input file.
    private File inputFile;
    // Path where the input file resides, which is where stories will be saved.
    public static Path outPath;
    // File parser.
    public static FileParser parser;
    // Keep a reference to the FictionDLTask if this is being run from the GUI.
    private FictionDLTask task;
    // Total number of stories which are to be downloaded, across all sites.
    private long totalNumStories;
    // Number of stories which have been processed (either have been downloaded or have failed to download).
    private long numStoriesProcessed = 0;

    /**
     * Create a new FictionDL to execute the program logic.
     * @param inputFilePath Path to input file.
     * @param outputDirPath Path to output directory.
     * @throws IllegalArgumentException if either of the paths cannot be resolved.
     */
    public FictionDL(String inputFilePath, String outputDirPath) throws IllegalArgumentException {
        this(inputFilePath, outputDirPath, null);
    }

    /**
     * Create a new FictionDL to execute the program logic.
     * @param inputFilePath Path to input file.
     * @param outputDirPath Path to output directory.
     * @throws IllegalArgumentException if either of the paths cannot be resolved.
     */
    public FictionDL(String inputFilePath, String outputDirPath, FictionDLTask task) throws IllegalArgumentException {
        if (inputFilePath == null) throw new IllegalArgumentException("[No input file path!]");
        // Store this task for later.
        this.task = task;
        // Try to get a file from the input file path.
        inputFile = Util.tryGetFile(inputFilePath);
        // Figure out the output directory.
        if (outputDirPath == null) {
            // If an output directory wasn't specified, use the directory of the input file.
            outPath = inputFile.getAbsoluteFile().getParentFile().toPath();
        } else {
            // An output directory was specified.
            outPath = Util.tryGetPath(outputDirPath);
        }
    }

    /**
     * Actually do stuff.
     */
    public void run() {
        // Create a FileParser to get the story URLs from the input file.
        parser = new FileParser(inputFile);
        // Figure out how many stories we're downloading, then download them.
        totalNumStories = parser.getTotalNumStories();
        getStories(parser);
        // All done!
        System.out.println(C.ALL_FINISHED);
    }

    /**
     * Takes in a FileParse which has the various sites' URL lists and uses it to kick off the downloading process.
     * @param parser FileParser which has successfully parsed input file.
     */
    private void getStories(FileParser parser) {
        /*
        Create a FictionHunt downloader and download stories.
          */
        if (!parser.getFictionHuntUrls().isEmpty()) {
            FictionHuntDL fictionHuntDL = new FictionHuntDL(parser.getFictionHuntUrls());
            fictionHuntDL.download();
            System.out.printf(C.FINISHED_WITH_SITE, FictionHuntDL.SITE);
        }
        /*
        Create a FanFiction.net downloader and download stories.
          */
        if (!parser.getFfnUrls().isEmpty()) {
            FanFictionDL fanFictionDL = new FanFictionDL(parser.getFfnUrls());
            fanFictionDL.download();
            System.out.printf(C.FINISHED_WITH_SITE, FanFictionDL.SITE);
        }
        /*
        Create a SIYE downloader and download stories.
          */
        if (!parser.getSiyeUrls().isEmpty()) {
            SiyeDL siyeDL = new SiyeDL(parser.getSiyeUrls());
            siyeDL.download();
            System.out.printf(C.FINISHED_WITH_SITE, SiyeDL.SITE);
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
        private String inputFilePath;
        private String outputDirPath;

        /**
         * Create a new FictionDLTask.
         * @param inputFilePath Path to input file.
         * @param outputDirPath Path to output directory.
         */
        public FictionDLTask(String inputFilePath, String outputDirPath) {
            this.inputFilePath = inputFilePath;
            this.outputDirPath = outputDirPath;
        }

        @Override
        protected Object call() throws Exception {
            // Do cool stuff.
            new FictionDL(inputFilePath, outputDirPath, this).run();
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
