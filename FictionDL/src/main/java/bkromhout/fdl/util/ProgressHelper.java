package bkromhout.fdl.util;

import bkromhout.fdl.events.IncWorkDoneEvent;
import bkromhout.fdl.events.RecalcUnitWorthEvent;
import bkromhout.fdl.events.UpdateTaskProgressEvent;
import bkromhout.fdl.storys.Story;
import com.google.common.eventbus.Subscribe;

/**
 * This class is responsible for keeping track of the progress of the whole download process.
 */
public class ProgressHelper {
    /**
     * Amount of work so far, regardless of success.
     */
    private double workDone;
    /**
     * Total amount of work.
     */
    private final double totalWork;
    /**
     * How much of the overall progress percentage is one story worth? Will always be equal to 1.0.
     */
    private final double oneStoryWorth = 1.0;
    /**
     * How much of the overall progress percentage is one work unit currently worth? This can change over the time, but
     * it will always be <= the value of {@link #oneStoryWorth}.
     */
    private double currUnitWorth;
    /**
     * If true, an IllegalStateException will be thrown if an event received in {@link
     * #onIncWorkDoneEvent(IncWorkDoneEvent)} has a value of >= 0.
     * <p>
     * {@link #onRecalcUnitWorthEvent(RecalcUnitWorthEvent)} will set this back to false when called.
     */
    private boolean needsRecalc = false;

    /**
     * Create a new {@link ProgressHelper} using the total number of stories to download as a baseline for the amount of
     * work which will be completed.
     * @param totalStories Aggregate number of stories to process.
     */
    public ProgressHelper(long totalStories) {
        // Register with the event bus.
        C.getEventBus().register(this);
        // Set work done to 0.
        this.workDone = 0.0;
        // Set total work to the number of stories.
        this.totalWork = totalStories;
        // Set the current work unit worth to be equal to one story's worth of work. For now.
        this.currUnitWorth = oneStoryWorth;
        // Update GUI progress bar.
        updateTaskProgress();

        Util.loudf("TotalWork=%f\nOneStoryWorth=%f\n" + C.LOG_GOLD + C.LOG_ULINE, totalWork, oneStoryWorth);
    }

    /**
     * Posts an {@link UpdateTaskProgressEvent} to set GUI progress bar, passing in {@link #workDone} and {@link
     * #totalWork}.
     */
    private void updateTaskProgress() {
        C.getEventBus().post(new UpdateTaskProgressEvent(workDone, totalWork));
    }

    /**
     * Get the total amount of work.
     * @return Total work.
     */
    public double getTotalWork() {
        return totalWork;
    }

    /**
     * When received, recalculated the value of {@link #currUnitWorth} by dividing {@link #oneStoryWorth} by {@link
     * RecalcUnitWorthEvent#divisorVal}.
     * @param event Event instance.
     */
    @Subscribe
    public void onRecalcUnitWorthEvent(RecalcUnitWorthEvent event) {
        if (event.getDivisorVal() <= 0L) currUnitWorth = oneStoryWorth;
        else currUnitWorth = oneStoryWorth / (double) event.getDivisorVal();
        needsRecalc = false;
        Util.loudf("CurrUnitWorth=%f\n" + C.LOG_GOLD + C.LOG_ULINE, currUnitWorth);
    }

    /**
     * When received, adds some amount to {@link #workDone} based on the value of {@link
     * IncWorkDoneEvent#getUnitsToAdd()}. If value is <= 0, adds {@link #oneStoryWorth}. If value is > 0, adds value
     * times {@link #currUnitWorth}.
     * @param event Event instance.
     */
    @Subscribe
    public void onIncWorkDoneEvent(IncWorkDoneEvent event) {
        if (event.getUnitsToAdd() <= 0L) workDone += oneStoryWorth;
        else {
            if (needsRecalc) throw new IllegalStateException(C.STALE_UNIT_WORTH);
            workDone += currUnitWorth * (double) event.getUnitsToAdd();
        }
        if (event.didFail()) needsRecalc = true;
        // Update progress bar.
        updateTaskProgress();
    }

    /*
    Static methods to post events to this progress helper. Collected here because the context is immediately obvious.
    */

    /**
     * Recalculates the current worth of a single work unit based on the divisor value given.
     * <p>
     * Keeping in mind that the worth of one work unit is always <= one story's worth of work, the value passed must be
     * the max possible number of times that {@link #finishedWorkUnit()} will be called before this method is called
     * again.
     * @param divisorVal Value to divide {@link ProgressHelper#oneStoryWorth} by in order to calculate the new worth of
     *                   one work unit. If this is set to <= 0, the worth of one unit will be set to {@link
     *                   ProgressHelper#oneStoryWorth}.
     */
    public static void recalcUnitWorth(long divisorVal) {
        C.getEventBus().post(new RecalcUnitWorthEvent(divisorVal));
    }

    /**
     * Call if a {@link Story} has failed to be successfully downloaded and saved.
     * <p>
     * If the value passed for workUnitsLeft is > 0, {@link #recalcUnitWorth(long)} <i>must</i> be called again prior to
     * calling either {@link #finishedWorkUnit()} or this method again with workUnitsLeft > 0.
     * @param workUnitsLeft Number of work units that are still left at this point.
     */
    public static void storyFailed(long workUnitsLeft) {
        C.getEventBus().post(new IncWorkDoneEvent(workUnitsLeft, true));
    }

    /**
     * Call to indicate a single unit of work has been finished.
     * <p>
     * Callers should take care not to call a greater number of times than the value which was last passed to {@link
     * #recalcUnitWorth(long)} so that the progress bar stays accurate.
     */
    public static void finishedWorkUnit() {
        C.getEventBus().post(new IncWorkDoneEvent(1L));
        Util.loud("Finished Work Unit");
    }
}
