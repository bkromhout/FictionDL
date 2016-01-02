package bkromhout.fdl.events;

/**
 * Used to inform {@link bkromhout.fdl.util.ProgressHelper} that it needs to recalculate the value to use for one unit's
 * worth of work.
 */
public class RecalcUnitWorthEvent {
    /**
     * Divisor value.
     */
    private final long divisorVal;

    /**
     * Create a new {@link RecalcUnitWorthEvent}.
     * <p>
     * When {@link bkromhout.fdl.util.ProgressHelper} receives this event, it will calculate a new value for {@link
     * bkromhout.fdl.util.ProgressHelper#currUnitWorth ProgressHelper#currUnitWorth} by dividing {@link
     * bkromhout.fdl.util.ProgressHelper#oneStoryWorth ProgressHelper#oneStoryWorth} by divisorVal.
     * @param divisorVal Divisor value.
     */
    public RecalcUnitWorthEvent(long divisorVal) {
        this.divisorVal = divisorVal;
    }

    /**
     * Value to divide {@link bkromhout.fdl.util.ProgressHelper#oneStoryWorth ProgressHelper#oneStoryWorth} by.
     * @return Divisor value.
     */
    public long getDivisorVal() {
        return divisorVal;
    }
}
