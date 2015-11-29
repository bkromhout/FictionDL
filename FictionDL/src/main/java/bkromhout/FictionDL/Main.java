package bkromhout.FictionDL;

import bkromhout.FictionDL.Gui.Gui;
import javafx.application.Application;
import javafx.application.Platform;
import org.apache.commons.cli.*;

/**
 * Just a simple entry point class for the command line app.
 */
public class Main {
    // Are we running with a GUI?
    public static boolean isGui = false;

    public static void main(String[] args) {
        // Check for GUI argument.
        if (args.length == 1 && args[0].equals("-gui")) {
            isGui = true;
            Application.launch(Gui.class);
            return;
        }
        // If it's not there, precede as normal.
        Util.log(C.VER_STRING);
        // Process CLI input.
        Options options = getOptions();
        CommandLineParser parser = new DefaultParser();
        CommandLine line;
        try {
            line = parser.parse(options, args);
        } catch (ParseException e) {
            Util.log("Bad arguments.");
            printHelp(options);
            return;
        }
        // If the user wants help, help them.
        if (line.hasOption("?")) {
            printHelp(options);
            return;
        }
        // Do cool stuff.
        try {
            new FictionDL(line.getOptionValue("i"), line.getOptionValue("o")).run();
        } catch (IllegalArgumentException e) {
            Util.logf(C.INVALID_PATH, e.getMessage());
        }
    }

    /**
     * Exit the program, using System.exit() if running in CLI mode and Platform.exit() if running in GUI mode.
     * @param returnCode The return code to use.
     */
    public static void exit(int returnCode) {
        if (isGui) Platform.exit();
        else System.exit(returnCode);
    }

    /**
     * Build the options.
     * @return Options for CLI args.
     */
    private static Options getOptions() {
        // Create options object.
        Options options = new Options();
        // Add input file option.
        options.addOption(Option.builder("i")
                .hasArg()
                .argName("INPUT FILE PATH")
                .desc("Input file path (within quotes if it has spaces). REQUIRED!")
                .required()
                .build());
        // Add output file option.
        options.addOption(Option.builder("o")
                .hasArg()
                .argName("OUTPUT DIR PATH")
                .desc("Output directory path (within quotes if it has spaces).")
                .build());
        // Add help option.
        options.addOption(Option.builder("?")
                .longOpt("help")
                .desc("Print this message.")
                .build());
        return options;
    }

    /**
     * Prints usage and help based on options.
     * @param options Options for CLI args.
     */
    private static void printHelp(Options options) {
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp("java -jar FictionDL.jar", options, true);
    }
}
