package bkromhout.fdl.localfic;

import java.nio.file.Path;

/**
 * In short, grants us the ability to create ePubs from local files/folders. This class is the orchestrator for any such
 * process.
 */
public class LocalFicProcessor {
    /**
     * This is the directory which holds any local fic directories.
     */
    private Path baseDir;

    /**
     * Create a new {@link LocalFicProcessor}.
     * @param baseDir Directory to look for local fics in.
     */
    public LocalFicProcessor(Path baseDir) {
        this.baseDir = baseDir;
    }

}
