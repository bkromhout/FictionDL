package bkromhout.fdl.events;

/**
 * Used to inform {@link bkromhout.fdl.util.ProgressHelper} that it needs to increase the total number of work units it
 * has set.
 */
public class IncTotalWorkEvent {
    /**
     * Number of work units to add to the total number of work units.
     */
    private long unitsToAdd;

    /**
     * Create a new {@link IncTotalWorkEvent}.
     * @param unitsToAdd Work units to add to total.
     */
    public IncTotalWorkEvent(long unitsToAdd) {
        this.unitsToAdd = unitsToAdd;
    }

    /**
     * Get the number of work units to add to the total.
     * @return Number of work units.
     */
    public long getUnitsToAdd() {
        return unitsToAdd;
    }
}
