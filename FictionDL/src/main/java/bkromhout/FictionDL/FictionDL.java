package bkromhout.FictionDL;

import bkromhout.FictionDL.Downloader.FanfictionNetDL;
import bkromhout.FictionDL.Downloader.FictionHuntDL;
import bkromhout.FictionDL.Downloader.SiyeDL;

import java.nio.file.Path;

/**
 * Fan Fiction Downloader.
 * <p>
 * Originally only supported FictionHunt, but has been expanded to support other sites now as well.
 * <p>
 * Should be run with one argument, which is a path (absolute or relative from where JVM is started) to a text file
 * which contains a list of supported sites' story URLs.
 * <p>
 * Will download stories as ePUB if possible or will scrape the story and generate xhtml files which can be used to
 * generate an ePUB using the Sigil application.
 */
public class FictionDL {
    // Path to input file.
    private String inputFilePath;
    // Path where the input file resides, which is where stories will be saved.
    public static Path dirPath;

    /**
     * Create a new FictionDL to execute the program logic.
     * @param inputFilePath Path to input file.
     */
    public FictionDL(String inputFilePath) {
        this.inputFilePath = inputFilePath;
    }

    /**
     * Actually do stuff.
     */
    public void run() {
        // Create a FileParser to get the story URLs from the input file.
        FileParser parser = new FileParser(inputFilePath);
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
        Create a Fanfiction.net downloader and download stories.
          */
        if (!parser.getFfnUrls().isEmpty()) {
            FanfictionNetDL fanfictionNetDL = new FanfictionNetDL(parser.getFfnUrls());
            fanfictionNetDL.download();
            System.out.printf(C.FINISHED_WITH_SITE, FanfictionNetDL.SITE);
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
