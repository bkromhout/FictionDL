package bkromhout.fdl.events;

/**
 * Event listened for by {@link bkromhout.fdl.FictionDL.FictionDLTask} which, when received, will cause it to call
 * {@link bkromhout.fdl.FictionDL.FictionDLTask#updateProgress(long, long)}.
 */
public class UpdateTaskProgressEvent {
    /**
     * Number of work units completed.
     */
    private long workDone;
    /**
     * Total number of work units.
     */
    private long totalWork;

    /**
     * Create a new {@link UpdateTaskProgressEvent}.
     * @param workDone Number of work units completed.
     * @param totalWork Total number of work units.
     */
    public UpdateTaskProgressEvent(long workDone, long totalWork) {
        this.workDone = workDone;
        this.totalWork = totalWork;
    }

    /**
     * Get number of work units completed.
     * @return Work done count.
     */
    public long getWorkDone() {
        return workDone;
    }

    /**
     * Get total number of work units.
     * @return Total work count.
     */
    public long getTotalWork() {
        return totalWork;
    }
}
