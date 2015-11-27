package bkromhout.FictionDL;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Utility class with static methods.
 */
public class Util {
    /**
     * Attempt to verify that a directory path exists (or can be created), then return a Path of it.
     * @param path String path of a directory.
     * @return Path of directory if given path is valid or creatable, otherwise prints line and exits. Returns null if
     * passed null.
     * @throws IllegalArgumentException if passed null or if there are any exceptions that occur.
     */
    public static Path tryGetPath(String path) throws IllegalArgumentException {
        File dir = new File(path);
        if ((!dir.exists() && !dir.mkdirs()) || !dir.isDirectory()) throw new IllegalArgumentException();
        return dir.toPath();
    }

    /**
     * Attempt to verify that a file exists and is readable and writable, then return a File of it.
     * @param path String path of a file.
     * @return File if file at given path exists and is readable and writable, otherwise prints line and exits. Returns
     * null if passed null.
     * @throws IllegalArgumentException if passed null or if there are any exceptions that occur.
     */
    public static File tryGetFile(String path) throws IllegalArgumentException {
        File file = new File(path);
        if (!file.exists() || !file.isFile() || !file.canRead() || !file.canWrite())
            throw new IllegalArgumentException();
        return file;
    }

    /**
     * Download an HTML document from the given URL.
     * @param url URL to download.
     * @return A Document object, or null if the url was malformed.
     */
    public static Document downloadHtml(String url) {
        Document doc = null;
        try {
            doc = Jsoup.connect(url).get();
        } catch (IOException e) {
            // We're just ignoring the exception really.
            System.out.printf(C.HTML_DL_FAILED, url);
        }
        return doc;
    }

    /**
     * Takes in a list of URLs (as strings) and returns a list of Documents downloaded from the URLs. Any malformed URLs
     * in the input list will be skipped.
     * @param urlList List of URLs to get Documents for.
     * @return Documents for all valid URLs that were in the input list.
     */
    public static ArrayList<Document> getDocuments(ArrayList<String> urlList) {
        // Loop through the URL list and download from each. Obviously filter out any null elements.
        return new ArrayList<>(urlList.stream().map(Util::downloadHtml).filter(out -> out != null)
                .collect(Collectors.toList()));
    }

    /**
     * Save a file at the specified path with the specified data. Will create the file if it doesn't exist and overwrite
     * it if it does.
     * @param filePath Path at which to save the file. It is assumed that the file name is legal.
     * @param data     Data to save to the file.
     */
    public static void saveFile(Path filePath, byte[] data) {
        try {
            Files.write(filePath, data);
        } catch (IOException e) {
            System.out.printf(C.SAVE_FILE_FAILED, filePath.toString());
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Escape ampersands that aren't part of code points.
     * @param in The string to escape.
     * @return The escaped string.
     */
    public static String escapeAmps(String in) {
        return in.replaceAll(C.AMP_REGEX, "&#x26;");
    }

    /**
     * Converts characters that, while valid in Windows-1252 are control characters in Unicode, to their corresponding
     * Unicode representations. Also escaptes any ampersands not already part of a character code.
     * @param in The string to escape.
     * @return The escaped string.
     */
    public static String convertWin1252Chars(String in) {
        return escapeAmps(in).replace("\u0096", "–").replace("\u0097", "—").replace("\u0091", "‘").replace("\u0092",
                "’").replace("\u0093", "“").replace("\u0094", "”").replace("\u0095", "•").replace("\u0085", "…");
    }

    /**
     * Removes or replaces characters which could potentially be illegal, and does a few other things.
     * @param in The file name to fix.
     * @return The fixed file name.
     */
    public static String ensureLegalFilename(String in) {
        // Remove problematic characters.
        String out = in.replace("<", "").replace(">", "").replace(":", "-").replace("\"", "").replace("/", "").replace(
                "\\", "").replace("|", "-").replace("?", "").replace("*", "").replace("\0", "");
        // Ensure that the end of the filename isn't a space or period for Windows' sake.
        out = out.trim();
        while (out.charAt(out.length() - 1) == '.') out = out.substring(0, out.length() - 2);
        // Let's make sure that the file doesn't start with a period either, or it'll be hidden on Linux-based OSs.
        while (out.charAt(0) == '.') out = out.replaceFirst("\\.", "");
        // Okay, we should be good now.
        return out;
    }
}
