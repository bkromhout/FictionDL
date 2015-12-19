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
     * Create a new {@link IncWorkDoneEvent}.
     * @param unitsToAdd Work units that have been completed.
     */
    public IncWorkDoneEvent(long unitsToAdd) {
        this.unitsToAdd = unitsToAdd;
    }

    /**
     * Get the number of work units that have been completed.
     * @return Number of work units.
     */
    public long getUnitsToAdd() {
        return unitsToAdd;
    }
}
