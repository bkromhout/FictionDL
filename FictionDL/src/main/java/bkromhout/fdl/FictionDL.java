package bkromhout.fdl;

import bkromhout.fdl.downloaders.*;
import bkromhout.fdl.events.UpdateTaskProgressEvent;
import bkromhout.fdl.parsers.ConfigFileParser;
import bkromhout.fdl.parsers.LinkFileParser;
import bkromhout.fdl.util.C;
import bkromhout.fdl.util.ProgressHelper;
import bkromhout.fdl.util.Util;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
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
 * This class is responsible for orchestrating the whole fiction download process. It is given everything it needs to do
 * its job when it is created, and then {@link FictionDL#run()} is called.
 */
public final class FictionDL {
    /**
     * If running from the {@link bkromhout.fdl.ui.Gui Gui}, a reference to the {@link FictionDLTask} hosting us. Null
     * if running from the CLI.
     */
    private FictionDLTask task;
    /**
     * Global EventBus.
     */
    private static final EventBus eventBus = new EventBus();
    /**
     * Global OkHttpClient, will be used for all networking.
     */
    private static OkHttpClient httpClient;
    /**
     * Represents the input (link) file which has a list of story urls
     */
    private File inputFile;
    /**
     * Represents the location where we want downloaded stories to be saved.
     */
    private static Path outPath;
    /**
     * Represents the configuration file. Might be null if one wasn't supplied.
     */
    private File configFile;
    /**
     * Config options parsed from the config file. Never null, but it might not contain any options.
     */
    private ConfigFileParser.Config cfg;
    /**
     * Link file parser.
     */
    private static LinkFileParser linkFileParser;
    /**
     * {@link ProgressHelper} for keeping track of our overall progress.
     */
    private static ProgressHelper progressHelper;

    /**
     * Create a new {@link FictionDL} to execute the program logic.
     * @param args Arguments, mapped to keys.
     * @throws IllegalArgumentException if either of the paths cannot be resolved.
     */
    public FictionDL(HashMap<String, String> args) throws IllegalArgumentException {
        this(args, null);
    }

    /**
     * Create a new {@link FictionDL} to execute the program logic.
     * @param args Arguments, mapped to keys.
     * @param task {@link FictionDLTask}, which won't be null if this {@link FictionDL} is being run from a GUI.
     * @throws IllegalArgumentException if either of the paths cannot be resolved.
     */
    public FictionDL(HashMap<String, String> args, FictionDLTask task) throws IllegalArgumentException {
        // Store the task (it might be null, that's fine).
        this.task = task;
        // Initialize.
        init(args);
    }

    /**
     * Initialize this {@link FictionDL}.
     * @param args Arguments to use during initialization.
     */
    private void init(HashMap<String, String> args) {
        // If we're running from a GUI, register the task with the EventBus so it will receive progress update events.
        if (task != null) eventBus.register(task);

        // Make sure we have an input path and that it is valid.
        if (args.get(C.ARG_IN_PATH) == null) throw new IllegalArgumentException(C.NO_INPUT_PATH);
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
        addOkHttpLogging();
    }

    /**
     * Adds a logger to the OkHttpClient.
     * <p>
     * All log messages will be logged using {@link Util#loud(String)}, so none of them will be printed if verbose mode
     * isn't enabled. Also, they will be purple :)
     */
    private void addOkHttpLogging() {
        // Pass the Util.loud() function to the logger so that it uses our logging methods.
        HttpLoggingInterceptor logger = new HttpLoggingInterceptor(str -> Util.loud(str + C.LOG_PURPLE));
        logger.setLevel(HttpLoggingInterceptor.Level.BASIC);
        httpClient.interceptors().add(logger);
    }

    /**
     * Actually do stuff.
     */
    public void run() {
        // Create a LinkFileParser to get the story urls from the input file.
        linkFileParser = new LinkFileParser(inputFile);
        // If we have a config file, create a ConfigFileParser to get options.
        if (configFile != null) cfg = new ConfigFileParser(configFile).getConfig();
        // Create a ProgressHelper, passing in the total number of stories to process.
        progressHelper = new ProgressHelper(linkFileParser.getTotalNumStories());
        // Download stories.
        getStories(linkFileParser);
        // All done!
        Util.log(C.ALL_FINISHED);
        // OkHttpClient dispatcher threads seems to enjoy sticking around for a while unless we do this >_<
        httpClient.getDispatcher().getExecutorService().shutdownNow();
    }

    /**
     * Takes in a {@link LinkFileParser} which has the various sites' url lists and uses it to kick off the downloading
     * process.
     * @param parser {@link LinkFileParser} which has successfully parsed input file.
     */
    private void getStories(LinkFileParser parser) {
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
            if (cfg.hasCreds(Site.MN)) muggleNetDL.doFormAuth(cfg.getCreds(Site.MN));
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
     * Provide access to the event bus.
     * @return Event bus.
     */
    public static EventBus getEventBus() {
        return eventBus;
    }

    /**
     * Get the Path that represents the location where everything should be saved.
     * @return Out path.
     */
    public static Path getOutPath() {
        return outPath;
    }

    /**
     * Get the parser responsible for parsing the link file.
     * @return Link file parser.
     */
    public static LinkFileParser getLinkFileParser() {
        return linkFileParser;
    }

    /**
     * Get the OkHttpClient in use for this run.
     * @return OkHttpClient.
     */
    public static OkHttpClient getHttpClient() {
        return httpClient;
    }

    /**
     * Subclass of Task so that {@link FictionDL} can be used by a JavaFX GUI app without running on the UI thread.
     */
    public static class FictionDLTask extends Task {
        /**
         * Arguments to pass to {@link FictionDL}.
         */
        private HashMap<String, String> args;

        /**
         * Create a new {@link FictionDLTask}.
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
         * Calls {@link FictionDLTask#updateProgress(long, long)} when an {@link UpdateTaskProgressEvent} is received.
         * @param event Event instance.
         */
        @Subscribe
        public void onUpdateTaskProgressEvent(UpdateTaskProgressEvent event) {
            super.updateProgress(event.getWorkDone(), event.getTotalWork());
        }
    }
}
