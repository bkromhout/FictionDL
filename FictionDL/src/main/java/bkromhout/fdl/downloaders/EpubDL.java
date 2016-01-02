package bkromhout.fdl.downloaders;

import bkromhout.fdl.FictionDL;
import bkromhout.fdl.site.Site;
import bkromhout.fdl.storys.Story;
import bkromhout.fdl.util.C;
import bkromhout.fdl.util.ProgressHelper;
import bkromhout.fdl.util.Util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Base class for downloaders which get stories by downloading ePUBs for them.
 */
abstract class EpubDL extends Downloader {

    /**
     * Create a new {@link EpubDL}.
     * @param site Site that this downloader services.
     */
    EpubDL(Site site) {
        super(site);
    }

    /**
     * Download a story as an ePUB file and save it.
     * @param story Story to download and save.
     */
    @Override
    protected void downloadStory(Story story) {
        Util.logf(C.DL_EPUB_FOR, Util.unEscapeAmps(story.getTitle()));
        ProgressHelper.recalcUnitWorth(0L); // Set unit worth to one story's worth.
        // Figure out save file path and download URI.
        Path file = FictionDL.getOutPath().resolve(Util.makeEpubFname(story.getTitle(), story.getAuthor()));
        URI dlUrl = URI.create(story.getUrl());

        try (final InputStream in = dlUrl.toURL().openStream()) {
            // Download the ePUB file.
            Files.copy(in, file, StandardCopyOption.REPLACE_EXISTING);
            Util.log(C.DONE + C.N);
        } catch (IOException e) {
            Util.logf(C.SAVE_FILE_FAILED, file.toAbsolutePath().toString());
        }
        // We update the progress bar whether we succeed or not.
        ProgressHelper.finishedWorkUnit();
    }
}
