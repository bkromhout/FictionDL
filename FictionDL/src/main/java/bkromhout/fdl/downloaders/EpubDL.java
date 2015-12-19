package bkromhout.fdl.downloaders;

import bkromhout.fdl.util.C;
import bkromhout.fdl.FictionDL;
import bkromhout.fdl.Site;
import bkromhout.fdl.util.ProgressHelper;
import bkromhout.fdl.util.Util;
import bkromhout.fdl.storys.Story;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;

/**
 * Base class for downloaders which get stories by downloading ePUBs for them.
 */
public abstract class EpubDL extends Downloader {

    /**
     * Create a new {@link EpubDL}.
     * @param fictionDL  FictionDL object which owns this downloader.
     * @param storyClass The class of Story which this downloader uses.
     * @param site       Site that this downloader services.
     * @param storyUrls  List of story urls to be downloaded.
     */
    public EpubDL(FictionDL fictionDL, Class<? extends Story> storyClass, Site site, HashSet<String> storyUrls) {
        super(fictionDL, storyClass, site, storyUrls);
    }

    /**
     * Download a story as an ePUB file and save it.
     * @param story Story to download and save.
     */
    @Override
    protected void downloadStory(Story story) {
        Util.logf(C.DL_EPUB_FOR, Util.unEscapeAmps(story.getTitle()));
        // Obviously, this is a rather simple task, which is why this class and its subclasses are so tiny as compared
        // to ParsingDL and its subclasses.
        Path file = FictionDL.getOutPath().resolve(Util.makeEpubFname(story.getTitle(), story.getAuthor()));
        URI dlUrl = URI.create(story.getUrl());
        try (final InputStream in = dlUrl.toURL().openStream()) {
            // Download the ePUB file.
            Files.copy(in, file, StandardCopyOption.REPLACE_EXISTING);
            Util.log(C.DONE + "\n");
        } catch (IOException e) {
            Util.logf(C.SAVE_FILE_FAILED, file.toAbsolutePath().toString());
        }
        // Update progress bar. We want to add 1 work unit in any case.
        ProgressHelper.storyProcessed(1L);
    }
}
