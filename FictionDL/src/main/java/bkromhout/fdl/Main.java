package bkromhout.fdl;

import bkromhout.fdl.ui.Gui;
import javafx.application.Application;
import javafx.application.Platform;
import org.apache.commons.cli.*;

import java.util.HashMap;

/**
 * Just a simple entry point class for the command line app.
 */
public class Main {
    /**
     * Are we running with a GUI?
     */
    public static boolean isGui = false;
    /**
     * Verbose log output?
     */
    public static boolean isVerbose = false;

    public static void main(String[] args) {
        // Make sure we start the GUI if the jar was run with no arguments at all (AKA, it was double-clicked).
        if (args.length == 0) {
            isGui = true;
            Application.launch(Gui.class);
            return;
        }
        // Process CLI input.
        Options options = getOptions();
        CommandLineParser parser = new DefaultParser();
        CommandLine line;
        try {
            line = parser.parse(options, args);
        } catch (ParseException e) {
            Util.log(C.INVALID_ARGS);
            printHelp(options);
            return;
        }
        // If the user wants help, help them.
        if (line.hasOption("?")) {
            printHelp(options);
            return;
        }
        // Check verbosity.
        if (line.hasOption("v")) isVerbose = true;
        // Check GUI.
        if (line.hasOption("g")) {
            // Run FictionDL using the GUI.
            isGui = true;
            Application.launch(Gui.class);
        } else {
            // Run FictionDL using the CLI.
            Util.log(C.VER_STRING);
            try {
                // Create arguments map.
                HashMap<String, String> ficDlArgs = new HashMap<>();
                ficDlArgs.put(C.ARG_IN_PATH, line.getOptionValue("i"));
                ficDlArgs.put(C.ARG_OUT_PATH, line.getOptionValue("o"));
                ficDlArgs.put(C.ARG_CFG_PATH, line.getOptionValue("c"));
                // Run FictionDL.
                new FictionDL(ficDlArgs).run();
            } catch (IllegalArgumentException e) {
                Util.log(e.getMessage());
            }
        }
    }

    /**
     * Exit the program, using System.exit() if running in CLI mode, but adding Platform.exit() if running in GUI mode.
     * @param returnCode The return code to use.
     */
    public static void exit(int returnCode) {
        if (isGui) Platform.exit();
        System.exit(returnCode);
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
                                .desc("Input file path (within quotes if it has spaces). This option is required if " +
                                        "none of -g, " +
                                        "--gui, -?, or --help are present.")
                                .build());
        // Add output file option.
        options.addOption(Option.builder("o")
                                .hasArg()
                                .argName("OUTPUT DIR PATH")
                                .desc("Output directory path (within quotes if it has spaces).")
                                .build());
        // Add config file option.
        options.addOption(Option.builder("c")
                                .hasArg()
                                .argName("CONFIG FILE PATH")
                                .desc("Config file path (within quotes if it has spaces).")
                                .build());
        // Add verbose option.
        options.addOption(Option.builder("v")
                                .desc("Verbose log output. Little of this is useful to most users.")
                                .build());
        // Add GUI option.
        options.addOption(Option.builder("g")
                                .longOpt("gui")
                                .desc("Explicitly run with the GUI, ignores any path arguments. The same effect can " +
                                        "be achieved by " +
                                        "supplying no arguments.")
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
