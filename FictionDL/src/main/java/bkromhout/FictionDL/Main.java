package bkromhout.FictionDL;

import org.apache.commons.cli.*;

/**
 * Just a simple entry point class for the command line app.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println(C.VER_STRING);
        // Process CLI input.
        Options options = getOptions();
        CommandLineParser parser = new DefaultParser();
        CommandLine line;
        try {
            line = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println("Bad arguments.");
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
            System.out.println(C.INVALID_PATH);
        }
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
