package bkromhout;

import bkromhout.Downloaders.FictionHuntDL;

/**
 * FictionHuntStory downloader.
 * <p>
 * Should be run with one argument, which is a path (absolute or relative from where JVM is started) to a text file
 * which contains a list of FictionHuntStory story URLs.
 * <p>
 * Will download the HTML files for each story into folders at the same location as the file. Sadly, since
 * FictionHuntStory doesn't preserve chapter names, the files will be named like "Chapter #".
 */
public class Main {

    public static void main(String[] args) {
        // Check args, print usage if needed.
        if (args.length != 1) {
            System.out.println("Usage: java FictionHuntDL.jar <path to URL list txt file>");
            System.exit(0);
        }
        // Create a FictionHuntStory downloader and download the files.
        FictionHuntDL fictionHuntDL = new FictionHuntDL(args[0]);
        fictionHuntDL.download();
    }
}
