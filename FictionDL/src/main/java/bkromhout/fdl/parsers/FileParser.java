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
     * Valid file types for parsing.
     */
    public enum FileType {
        URLS("URLs"), CONFIG("config");

        private final String ufName;

        FileType(String ufName) {
            this.ufName = ufName;
        }

        @Override
        public String toString() {
            return this.ufName;
        }
    }

    /**
     * What type of files this parser handles.
     */
    protected FileType type;

    /**
     * Create a new {@link FileParser}.
     * @param type Type of files this parser will handle.
     * @param file File to parse.
     */
    protected FileParser(FileType type, File file) {
        this.type = type;
        init();
        parse(file);
    }

    /**
     * Set up this parser.
     */
    protected abstract void init();

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
