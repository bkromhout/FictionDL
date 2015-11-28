package bkromhout.FictionDL;

import bkromhout.FictionDL.Downloader.FanFictionDL;
import bkromhout.FictionDL.Downloader.FictionHuntDL;
import bkromhout.FictionDL.Downloader.SiyeDL;

import java.io.File;
import java.nio.file.Path;

/**
 * Fan Fiction Downloader.
 * <p>
 * Originally only supported FictionHunt, but has been expanded to support other sites now as well.
 * <p>
 * Will download stories as ePUB if possible or will scrape the story HTML and generate an ePUB using that.
 */
public class FictionDL {
    // Path to input file.
    private File inputFile;
    // Path where the input file resides, which is where stories will be saved.
    public static Path outPath;
    // File parser.
    public static FileParser parser;

    /**
     * Create a new FictionDL to execute the program logic.
     * @param inputFilePath Path to input file.
     * @param outputDirPath Path to output directory.
     * @throws IllegalArgumentException if either of the paths cannot be resolved.
     */
    public FictionDL(String inputFilePath, String outputDirPath) throws IllegalArgumentException {
        if (inputFilePath == null) throw new IllegalArgumentException();
        // Try to get a file from the input file path.
        inputFile = Util.tryGetFile(inputFilePath);
        // Figure out the output directory.
        if (outputDirPath == null) {
            // If an output directory wasn't specified, use the directory of the input file.
            outPath = inputFile.getAbsoluteFile().getParentFile().toPath();
        } else {
            // An output directory was specified.
            outPath = Util.tryGetPath(outputDirPath);
        }
    }

    /**
     * Actually do stuff.
     */
    public void run() {
        // Create a FileParser to get the story URLs from the input file.
        parser = new FileParser(inputFile);
        // Download all stories.
        getStories(parser);
        // All done!
        System.out.println(C.ALL_FINISHED);
    }

    /**
     * Takes in a FileParse which has the various sites' URL lists and uses it to kick off the downloading process.
     * @param parser FileParser which has successfully parsed input file.
     */
    private void getStories(FileParser parser) {
        /*
        Create a FictionHunt downloader and download stories.
          */
        if (!parser.getFictionHuntUrls().isEmpty()) {
            FictionHuntDL fictionHuntDL = new FictionHuntDL(parser.getFictionHuntUrls());
            fictionHuntDL.download();
            System.out.printf(C.FINISHED_WITH_SITE, FictionHuntDL.SITE);
        }
        /*
        Create a FanFiction.net downloader and download stories.
          */
        if (!parser.getFfnUrls().isEmpty()) {
            FanFictionDL fanFictionDL = new FanFictionDL(parser.getFfnUrls());
            fanFictionDL.download();
            System.out.printf(C.FINISHED_WITH_SITE, FanFictionDL.SITE);
        }
        /*
        Create a SIYE downloader and download stories.
          */
        if (!parser.getSiyeUrls().isEmpty()) {
            SiyeDL siyeDL = new SiyeDL(parser.getSiyeUrls());
            siyeDL.download();
            System.out.printf(C.FINISHED_WITH_SITE, SiyeDL.SITE);
        }
    }
}
