package bkromhout.fdl.parsers;

import bkromhout.fdl.C;
import bkromhout.fdl.Util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Base file parser.
 */
public abstract class FileParser {
    /**
     * What type of files this parser handles.
     */
    protected String type;

    /**
     * Create a new file parser.
     * @param type Type of files this parser will handle.
     * @param file File to parse.
     */
    protected FileParser(String type, File file) {
        this.type = type;
        parse(file);
    }

    /**
     * Parse the file and process it.
     * @param file File to parse.
     */
    private void parse(File file) {
        Util.logf(C.PARSING_FILE, type);
        // Try to read lines from file into the url list
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine();
            while (line != null) {
                // Process the line.
                processLine(line.trim());
                line = br.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Util.log(C.DONE);
    }

    /**
     * Process a line from the file.
     * @param line Line.
     */
    protected abstract void processLine(String line);
}
