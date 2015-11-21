package bkromhout;

import bkromhout.Downloader.FictionHuntDL;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * FictionHuntStory downloader.
 * <p>
 * Should be run with one argument, which is a path (absolute or relative from where JVM is started) to a text file
 * which contains a list of FictionHuntStory story URLs.
 * <p>
 * Will download the HTML files for each story into folders at the same location as the file.
 */
public class Main {
    // Path where the input file resides, which is where stories will be saved.
    public static Path dirPath;

    public static void main(String[] args) {
        // Check args, print usage if needed.
        if (args.length != 1) {
            System.out.println("Usage: java -jar FictionHuntDL.jar <path to URL list txt file>");
            System.exit(0);
        }
        // Create a FictionHuntStory downloader and download the files.
        FictionHuntDL fictionHuntDL = new FictionHuntDL(args[0]);
        fictionHuntDL.download();
        System.out.println("All Finished! :)");
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
            System.out.printf("Failed to download HTML from: \"%s\"\n", url);
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
        return new ArrayList<>(urlList.stream().map(Main::downloadHtml).filter(out -> out != null)
                .collect(Collectors.toList()));
    }
}
