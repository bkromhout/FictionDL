package bkromhout.fdl.ex;

/**
 * Exception thrown when a {@link bkromhout.fdl.storys.Story Story} cannot be created for some reason.
 */
public class InitStoryException extends Exception {

    /**
     * Create a new {@link InitStoryException} using a format string and a prior cause.
     * @param cause      Previous Cause.
     * @param format     Format string to use.
     * @param formatArgs Objects to use in format string.
     */
    public InitStoryException(Throwable cause, String format, Object... formatArgs) {
        super(String.format(format, formatArgs), cause);
    }

    /**
     * Create a new {@link InitStoryException} using a format string.
     * @param format     Format string to use.
     * @param formatArgs Objects to use in format string.
     */
    public InitStoryException(String format, Object... formatArgs) {
        this(null, format, formatArgs);
    }
}
