package bkromhout.fdl;

import bkromhout.fdl.downloaders.*;
import bkromhout.fdl.parsers.ConfigFileParser;
import bkromhout.fdl.parsers.LinkFileParser;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.logging.HttpLoggingInterceptor;
import javafx.concurrent.Task;

import java.io.File;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * Fan Fiction Downloader. (Not to be confused wih the FanFictionDL, which is an actual site downloader, this class is a
 * main class which the program is named after.)
 * <p>
 * Originally only supported FictionHunt, but has been expanded to support other sites now as well.
 * <p>
 * Scrapes the story HTML and generates an ePUB using that.
 */
public class FictionDL {
    /**
     * Global OkHttpClient.
     */
    public static OkHttpClient httpClient;
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
        // Initialize.
        init(args);
    }

    /**
     * Initialize this FictionDL.
     * @param args Arguments to use during initialization.
     */
    private void init(HashMap<String, String> args) {
        // If we're running from a GUI, go ahead and set the progress bar to indeterminate.
        if (task != null) task.updateProgress(-1, 0);

        // Make sure we have an input path and that it is valid.
        if (args.get(C.ARG_IN_PATH) == null) throw new IllegalArgumentException(C.NO_IN_PATH);
        inputFile = Util.tryGetFile(args.get(C.ARG_IN_PATH));

        // Figure out the output directory. If we were given one, make sure it's valid. If we weren't use the
        // directory of the input file.
        if (args.get(C.ARG_OUT_PATH) != null) outPath = Util.tryGetPath(args.get(C.ARG_OUT_PATH));
        else outPath = inputFile.getAbsoluteFile().getParentFile().toPath();

        // Get the config file path, if present.
        if (args.get(C.ARG_CFG_PATH) != null) configFile = Util.tryGetFile(args.get(C.ARG_CFG_PATH));

        // Set up the OkHttpClient.
        httpClient = new OkHttpClient();
        httpClient.setCookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
        httpClient.getDispatcher().setMaxRequestsPerHost(10); // Bump this up from 5.
        httpClient.setReadTimeout(0, TimeUnit.MILLISECONDS);
        if (Main.isVerbose) addOkHttpLogging(); // Definitely don't do this if we aren't in verbose mode.
    }

    /**
     * Adds a logger to the OkHttpClient. In a method so that it's easy to turn off.
     */
    private void addOkHttpLogging() {
        HttpLoggingInterceptor logger = new HttpLoggingInterceptor();
        logger.setLevel(HttpLoggingInterceptor.Level.BASIC);
        httpClient.interceptors().add(logger);
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
        // OkHttpClient dispatcher threads seems to enjoy sticking around for a while unless we do this >_<
        httpClient.getDispatcher().getExecutorService().shutdownNow();
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
            if (cfg.hasCreds(C.NAME_MN)) muggleNetDL.doFormAuth(cfg.getCreds(C.NAME_MN));
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
