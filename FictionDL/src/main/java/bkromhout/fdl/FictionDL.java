package bkromhout.fdl;

import bkromhout.fdl.downloaders.*;
import bkromhout.fdl.events.UpdateTaskProgressEvent;
import bkromhout.fdl.parsers.ConfigFileParser;
import bkromhout.fdl.parsers.LinkFileParser;
import bkromhout.fdl.util.C;
import bkromhout.fdl.util.ProgressHelper;
import bkromhout.fdl.util.Util;
import com.google.common.eventbus.Subscribe;
import javafx.concurrent.Task;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;

/**
 * This class is responsible for orchestrating the whole fiction download process. It is given everything it needs to do
 * its job when it is created, and then {@link FictionDL#run()} is called.
 */
public class FictionDL {
    /**
     * If running from the {@link bkromhout.fdl.ui.Gui Gui}, a reference to the {@link FictionDLTask} hosting us. Null
     * if running from the CLI.
     */
    private FictionDLTask task;
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
        if (task != null) C.getEventBus().register(task);

        // Make sure we have an input path and that it is valid.
        if (args.get(C.ARG_IN_PATH) == null) throw new IllegalArgumentException(C.NO_INPUT_PATH);
        inputFile = Util.tryGetFile(args.get(C.ARG_IN_PATH));

        // Figure out the output directory. If we were given one, make sure it's valid. If we weren't use the
        // directory of the input file.
        if (args.get(C.ARG_OUT_PATH) != null) outPath = Util.tryGetPath(args.get(C.ARG_OUT_PATH));
        else outPath = inputFile.getAbsoluteFile().getParentFile().toPath();

        // Get the config file path, if present.
        if (args.get(C.ARG_CFG_PATH) != null) configFile = Util.tryGetFile(args.get(C.ARG_CFG_PATH));
    }

    /**
     * Tasks to do before running.
     */
    private void preRun() {
        // Create a LinkFileParser to get the story urls from the input file.
        linkFileParser = new LinkFileParser(inputFile);
        // If we have a config file, create a ConfigFileParser to get options.
        if (configFile != null) cfg = new ConfigFileParser(configFile).getConfig();
        // Create a ProgressHelper, passing in the total number of stories to process.
        progressHelper = new ProgressHelper(linkFileParser.getTotalNumStories());
    }

    /**
     * Tasks to do after running.
     */
    private void postRun() {
        Util.log(C.ALL_FINISHED);
        Util.loudf(C.RUN_RESULTS, progressHelper.getTotalWork());
        freeResources();
    }

    /**
     * Explicitly free resources which could keep the JVM from shutting down.
     */
    private void freeResources() {
        //Main.httpClient.getDispatcher().getExecutorService().shutdownNow(); // Shut down OkHttp dispatcher's
        // ExecutorService.
        //Schedulers.shutdown(); // Shut down all RxJava schedulers.
        //ConnectionPool.getDefault().evictAll(); // Evict OkHttp's connection pool.
        //C.getHttpClient().getConnectionPool().evictAll();
        //Main.eventBusExecutor.shutdownNow(); // Shut down event bus executor.
    }

    /**
     * Do the fun stuff.
     */
    public void run() {
        // Do pre-run tasks.
        preRun();
        // TODO Wouldn't it be cool if we could just iterate instead of calling each one? Yeah...
        // Download FictionHunt stories.
        if (!linkFileParser.getFictionHuntUrls().isEmpty()) {
            FictionHuntDL fictionHuntDL = new FictionHuntDL(this, linkFileParser.getFictionHuntUrls());
            fictionHuntDL.download();
        }

        // Download FanFiction.net stories.
        if (!linkFileParser.getFfnUrls().isEmpty()) {
            FanFictionDL fanFictionDL = new FanFictionDL(this, linkFileParser.getFfnUrls());
            fanFictionDL.download();
        }

        // Download SIYE stories.
        if (!linkFileParser.getSiyeUrls().isEmpty()) {
            SiyeDL siyeDL = new SiyeDL(this, linkFileParser.getSiyeUrls());
            siyeDL.download();
        }

        // Download MuggleNet stories.
        if (!linkFileParser.getMnUrls().isEmpty()) {
            MuggleNetDL muggleNetDL = new MuggleNetDL(this, linkFileParser.getMnUrls());
            if (cfg.hasCreds(ESite.MN)) muggleNetDL.doFormAuth(cfg.getCreds(ESite.MN));
            muggleNetDL.download();
        }

        // Download Ao3 stories.
        if (!linkFileParser.getAo3Urls().isEmpty()) {
            Ao3DL ao3DL = new Ao3DL(this, linkFileParser.getAo3Urls());
            ao3DL.download();
        }

        // Do post-run tasks.
        postRun();
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
     * Subclass of Task so that {@link FictionDL} can be used by a JavaFX GUI app without running on the UI thread.
     */
    public static class FictionDLTask extends Task {
        /**
         * Reference to the instance of {@link FictionDL} that this task is running.
         */
        private FictionDL fictionDL;
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
            fictionDL = new FictionDL(args, this);
            fictionDL.run();
            return null;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            fictionDL.freeResources();
            fictionDL = null;
            return super.cancel(mayInterruptIfRunning);
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
