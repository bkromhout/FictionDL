package bkromhout;

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
    }
}
