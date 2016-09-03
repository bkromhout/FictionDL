package bkromhout.fdl;

import bkromhout.fdl.events.UpdateTaskProgressEvent;
import bkromhout.fdl.localfic.LocalStoryProcessor;
import bkromhout.fdl.parsing.ConfigFileParser;
import bkromhout.fdl.parsing.InputFileParser;
import bkromhout.fdl.site.Site;
import bkromhout.fdl.site.Sites;
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
    private final FictionDLTask task;
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
        LocalStoryProcessor localStoryProcessor = new LocalStoryProcessor(inputFile.toPath().getParent());
        C.getEventBus().register(localStoryProcessor);

        // Create a InputFileParser so that site url lists and the local story list are populated.
        new InputFileParser(inputFile);

        // If we have a config file, create a ConfigFileParser to get options.
        if (configFile != null) cfg = new ConfigFileParser(configFile).getConfig();

        // Figure out how work we will be doing, then create a ProgressHelper and pass it in.
        int totalWork = 0;
        for (Site site : Sites.all()) totalWork += site.getWorkCount(); // Add total number of site stories.
        totalWork += localStoryProcessor.getWorkCount(); // Add number of local stories.
        ProgressHelper progressHelper = new ProgressHelper(totalWork);

        /* Download stories from all sites. */
        for (Site site : Sites.all()) site.process(cfg);

        /* Create any local stories that we parsed from the input file. */
        localStoryProcessor.process();

        /* Do post-run tasks. */
        Util.log(C.ALL_FINISHED);
        Util.logf(C.RUN_RESULTS, progressHelper.getStoriesDownloaded(), progressHelper.getTotalNumberOfStories());
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
        private final HashMap<String, String> args;

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
