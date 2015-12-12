package bkromhout.fictiondl.Downloader;

import bkromhout.fictiondl.C;
import bkromhout.fictiondl.FictionDL;
import bkromhout.fictiondl.Story.Story;
import bkromhout.fictiondl.Util;

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
     * Create a new EpubDL.
     * @param fictionDL  FictionDL object which owns this downloader.
     * @param storyClass The class of Story which this downloader uses.
     * @param siteName   Human-readable site name for this downloader.
     * @param storyUrls  List of story URLs to be downloaded.
     */
    public EpubDL(FictionDL fictionDL, Class<? extends Story> storyClass, String siteName,
                  HashSet<String> storyUrls) {
        super(fictionDL, storyClass, siteName, storyUrls);
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
        Path file = FictionDL.outPath.resolve(Util.makeEpubFname(story.getTitle(), story.getAuthor()));
        URI dlUrl = URI.create(story.getUrl());
        try (final InputStream in = dlUrl.toURL().openStream()) {
            // Download the ePUB file.
            Files.copy(in, file, StandardCopyOption.REPLACE_EXISTING);
            Util.log(C.DONE + "\n");
        } catch (IOException e) {
            Util.logf(C.SAVE_FILE_FAILED, file.toAbsolutePath().toString());
        }
        storyProcessed(); // Update progress.
    }
}
