package bkromhout;

import java.io.File;

/**
 * FictionHunt downloader.
 */
public class Main {

    public static void main(String[] args) {
        // Check usage.
        if (args.length != 1) {
            System.out.println("Usage: java FictionHuntDL.jar <path to URL list txt file>");
            System.exit(0);
        }

        // Make sure the given path is valid, is a file, and can be read.
        File storiesFile = new File(args[0]);
        if (!(storiesFile.exists() && storiesFile.isFile() && storiesFile.canRead())) {
            System.out.println("Invalid path.");
            System.exit(1);
        }

        
    }
}
