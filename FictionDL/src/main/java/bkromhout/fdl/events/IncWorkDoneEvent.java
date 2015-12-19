package bkromhout.fdl.events;

/**
 * Used to inform {@link bkromhout.fdl.util.ProgressHelper} that it needs to increase the number of completed work
 * units.
 */
public class IncWorkDoneEvent {
    /**
     * Number of work units to add to the number of completed work units.
     */
    private long unitsToAdd;
    /**
     * Set to true if the units being added are from some failure.
     */
    private boolean didFail;

    /**
     * Create a new {@link IncWorkDoneEvent}.
     * @param unitsToAdd Work units that have been completed.
     */
    public IncWorkDoneEvent(long unitsToAdd) {
        this(unitsToAdd, false);
    }

    /**
     * Create a new {@link IncWorkDoneEvent}.
     * @param unitsToAdd Work units that have been completed.
     * @param didFail    Whether the units that are added from this event are due to failures or not.
     */
    public IncWorkDoneEvent(long unitsToAdd, boolean didFail) {
        this.unitsToAdd = unitsToAdd;
        this.didFail = didFail;
    }

    /**
     * Get the number of work units that have been completed.
     * @return Number of work units.
     */
    public long getUnitsToAdd() {
        return unitsToAdd;
    }

    /**
     * Check is the units here were from failures.
     * @return True if so, otherwise false.
     */
    public boolean didFail() {
        return didFail;
    }
}
