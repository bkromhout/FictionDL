package bkromhout.fdl.util;

import bkromhout.fdl.events.IncWorkDoneEvent;
import bkromhout.fdl.events.RecalcUnitWorthEvent;
import bkromhout.fdl.events.UpdateTaskProgressEvent;
import bkromhout.fdl.stories.Story;
import com.google.common.eventbus.Subscribe;

/**
 * This class is responsible for keeping track of the progress of the whole download process.
 *
 * TODO keep track of completed vs. failed stories.
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
     * @param totalWork Number of total work units.
     */
    public ProgressHelper(long totalWork) {
        // Register with the event bus.
        C.getEventBus().register(this);
        // Set work done to 0.
        this.workDone = 0.0;
        // Set total work to the number of stories.
        this.totalWork = totalWork;
        // Set the current work unit worth to be equal to one story's worth of work. For now.
        this.currUnitWorth = oneStoryWorth;
        // Update GUI progress bar.
        updateTaskProgress();
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
     * When received, recalculate the value of {@link #currUnitWorth} by dividing {@link #oneStoryWorth} by {@link
     * RecalcUnitWorthEvent#divisorVal}.
     * @param event Event instance.
     */
    @Subscribe
    public void onRecalcUnitWorthEvent(RecalcUnitWorthEvent event) {
        if (event.getDivisorVal() <= 0L) currUnitWorth = oneStoryWorth;
        else currUnitWorth = oneStoryWorth / (double) event.getDivisorVal();
        needsRecalc = false;
    }

    /**
     * When received, adds some amount to {@link #workDone} based on the value of {@link
     * IncWorkDoneEvent#getUnitsToAdd()}. If value is <= 0, adds {@link #oneStoryWorth}. If value is > 0, adds value
     * times {@link #currUnitWorth}.
     * @param event Event instance.
     */
    @Subscribe
    public void onIncWorkDoneEvent(IncWorkDoneEvent event) {
        // If <= 0L, add one story's worth of work.
        if (event.getUnitsToAdd() <= 0L) workDone += oneStoryWorth;
        else {
            if (needsRecalc) throw new IllegalStateException(C.STALE_UNIT_WORTH);
            workDone += currUnitWorth * (double) event.getUnitsToAdd();
        }
        if (event.didFail() && currUnitWorth != oneStoryWorth) needsRecalc = true;
        // Update progress bar.
        updateTaskProgress();
    }

    /*
    Static methods to post events to this progress helper. Collected here because the context is immediately obvious.
    */

    /**
     * Recalculates the current worth of a single work unit based on {@code divisor}.
     * <p>
     * Keeping in mind that the worth of one work unit is always <= one story's worth of work, so {@code divisor} must
     * be the max possible number of times that {@link #finishedWorkUnit()} will be called before this method is called
     * again.
     * @param divisorVal Value to divide {@link #oneStoryWorth} by in order to calculate the new worth of one work unit.
     *                   If this is set to <= 0L, the worth of one unit will be set to {@link #oneStoryWorth}.
     */
    public static void recalcUnitWorth(long divisorVal) {
        C.getEventBus().post(new RecalcUnitWorthEvent(divisorVal));
    }

    /**
     * Call if a {@link Story} has failed to be successfully saved.
     * <p>
     * Passing a value of {@code 0L} or less indicates that a whole story failed, and that we should add a story's worth
     * of work to the progress bar.
     * <p>
     * If {@code workUnitsLeft} > 0, {@link #recalcUnitWorth(long)} <i>must</i> be called again prior to calling either
     * this method again with a {@code workUnitsLeft} value > 0L, or {@link #finishedWorkUnit()}.
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
        C.getEventBus().post(new IncWorkDoneEvent());
    }
}
