package bkromhout.fdl.util;

/**
 * Intended to be implemented by any classes which do work that should be tracked by {@link ProgressHelper}.
 */
public interface IWorkProducer {

    /**
     * Get the number of work units that this object expects it will produce.
     * <p>
     * It is a logical fallacy to call this method during or after the implementing class has started doing work.
     * @return Some number of work units >= 0.
     */
    int getWorkCount();
}
