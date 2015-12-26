package bkromhout.fdl.events;

/**
 * Event listened for by {@link bkromhout.fdl.FictionDL.FictionDLTask} which, when received, will cause it to call
 * {@link bkromhout.fdl.FictionDL.FictionDLTask#updateProgress(long, long)}.
 */
public class UpdateTaskProgressEvent {
    /**
     * Number of work units completed.
     */
    private double workDone;
    /**
     * Total number of work units.
     */
    private double totalWork;

    /**
     * Create a new {@link UpdateTaskProgressEvent}.
     * @param workDone Number of work units completed.
     * @param totalWork Total number of work units.
     */
    public UpdateTaskProgressEvent(double workDone, double totalWork) {
        this.workDone = workDone;
        this.totalWork = totalWork;
    }

    /**
     * Get number of work units completed.
     * @return Work done count.
     */
    public double getWorkDone() {
        return workDone;
    }

    /**
     * Get total number of work units.
     * @return Total work count.
     */
    public double getTotalWork() {
        return totalWork;
    }
}
