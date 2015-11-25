package bkromhout.FictionDL;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Utility class.
 */
public class Util {

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
     * @param filePath Path at which to save the file.
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
     * Convert any "&" to "&amp;" so long as they aren't part of a character code.
     * @param in The string to escape.
     * @return The escaped string.
     */
    public static String escapeAmps(String in) {
        return in.replaceAll(C.AMP_REGEX, "&amp;");
    }
}
