package bkromhout.fdl;

import bkromhout.fdl.events.UpdateTaskProgressEvent;
import bkromhout.fdl.localfic.LocalFicProcessor;
import bkromhout.fdl.parsers.ConfigFileParser;
import bkromhout.fdl.parsers.InputFileParser;
import bkromhout.fdl.util.C;
import bkromhout.fdl.util.ProgressHelper;
import bkromhout.fdl.util.Sites;
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
     * Input file parser.
     */
    private static InputFileParser inputFileParser;
    /**
     * Local story processor.
     */
    public static LocalFicProcessor localFicProcessor;

    /**
     * {@link ProgressHelper} for keeping track of our overall progress.
     */
    //private static ProgressHelper progressHelper; //TODO remove this maybe? Need to test the GUI again.

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
     * Do the fun stuff.
     */
    void run() {
        /* Do pre-run tasks. */
        // Create Site classes and local story processor.
        Sites.init();
        localFicProcessor = new LocalFicProcessor(inputFile.toPath().getParent());

        // Create a InputFileParser to get the story urls from the input file.
        inputFileParser = new InputFileParser(inputFile);

        // If we have a config file, create a ConfigFileParser to get options.
        if (configFile != null) cfg = new ConfigFileParser(configFile).getConfig();

        // Figure out how many stories we will be downloading, then create a ProgressHelper and pass it in.
        int totalStories = 0;
        for (Site site : Sites.all()) totalStories += site.getUrls().size();
        ProgressHelper progressHelper = new ProgressHelper(totalStories);

        /* Download stories from all sites. */
        for (Site site : Sites.all()) site.download(this, cfg);

        /* Create any local stories that we parsed from the input file. */
        // TODO!

        /* Do post-run tasks. */
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
     * Get the Path that represents the location where everything should be saved.
     * @return Out path.
     */
    public static Path getOutPath() {
        return outPath;
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
