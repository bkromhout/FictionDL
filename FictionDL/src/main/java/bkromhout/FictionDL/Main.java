package bkromhout.FictionDL;

/**
 * Just a simple entry point class for the command line app.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println(C.VER_STRING);
        // Check args, print usage if needed.
        if (args.length != 1) {
            System.out.println(C.USAGE);
            System.exit(0);
        }
        // Do cool stuff.
        new FictionDL(args[0]).run();
    }
}
