package bkromhout.fdl.util;

import bkromhout.fdl.FictionDL;
import bkromhout.fdl.events.IncTotalWorkEvent;
import bkromhout.fdl.events.IncWorkDoneEvent;
import bkromhout.fdl.events.UpdateTaskProgressEvent;
import com.google.common.eventbus.Subscribe;

/**
 * This class is responsible for keeping track of the progress of the whole download process.
 */
public class ProgressHelper {
    /**
     * Number of work units done so far.
     */
    private long workDone;
    /**
     * Total number of work units.
     */
    private long totalWork;

    /**
     * Create a new {@link ProgressHelper} using the total number of stories to download as a baseline for the amount of
     * work which will be completed.
     * @param totalStories Aggregate number of stories to process.
     */
    public ProgressHelper(long totalStories) {
        // Register with the event bus.
        FictionDL.getEventBus().register(this);
        // Set work done to 0.
        this.workDone = 0L;
        // At the time of initialization, we only have knowledge of the total number of stories, across all sites,
        // that we will attempt to process. Thus, we use that as our initial value for totalWork.
        this.totalWork = totalStories;
        // Update GUI progress bar.
        updateTaskProgress();
    }

    /**
     * Posts an {@link UpdateTaskProgressEvent} to set GUI progress bar, passing in {@link #workDone} and {@link
     * #totalWork}.
     */
    private void updateTaskProgress() {
        FictionDL.getEventBus().post(new UpdateTaskProgressEvent(workDone, totalWork));
    }

    /**
     * Adds some number of work units to {@link #totalWork} when received.
     * @param event Event instance.
     */
    @Subscribe
    public void onIncTotalWorkEvent(IncTotalWorkEvent event) {
        // In order to keep the progress bar from going down when we update the total number of work units, we double
        // the amount that we add to the total, and then we add half of it to the number of work units completed.
        long num = event.getUnitsToAdd();
        totalWork += 2 * num;
        workDone += num;
        // Now update the progress bar.
        updateTaskProgress();
    }

    /**
     * Adds some number of work units to {@link #workDone} when received.
     * @param event Event instance.
     */
    @Subscribe
    public void onIncWorkDoneEvent(IncWorkDoneEvent event) {
        workDone += event.getUnitsToAdd();
        // Update progress bar.
        updateTaskProgress();
    }
}
