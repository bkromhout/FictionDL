package bkromhout;

import bkromhout.Downloader.FanfictionNetDL;
import bkromhout.Downloader.FictionHuntDL;
import bkromhout.Downloader.SiyeDL;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Fan Fiction Downloader.
 * <p>
 * Originally only supported FictionHunt, but has been expanded to support other sites now as well.
 * <p>
 * Should be run with one argument, which is a path (absolute or relative from where JVM is started) to a text file
 * which contains a list of supported sites' story URLs.
 * <p>
 * Will download stories as ePUB if possible or will scrape the story and generate xhtml files which can be used to
 * generate an ePUB using the Sigil application.
 */
public class Main {
    // Path where the input file resides, which is where stories will be saved.
    public static Path dirPath;

    public static void main(String[] args) {
        // Check args, print usage if needed.
        if (args.length != 1) {
            System.out.println(C.USAGE);
            System.exit(0);
        }
        // Create a FileParser to get the story URLs from the input file.
        FileParser parser = new FileParser(args[0]);
        // Download all stories.
        getStories(parser);
        // All done!
        System.out.println(C.ALL_FINISHED);
    }

    /**
     * Takes in a FileParse which has the various sites' URL lists and uses it to kick off the downloading process.
     * @param parser FileParser which has successfully parsed input file.
     */
    private static void getStories(FileParser parser) {
        /*
        Create a FictionHunt downloader and download stories.
          */
        if (!parser.getFictionHuntUrls().isEmpty()) {
            FictionHuntDL fictionHuntDL = new FictionHuntDL(parser.getFictionHuntUrls());
            fictionHuntDL.download();
            System.out.printf(C.FINISHED_WITH_SITE, FictionHuntDL.SITE);
        }
        /*
        Create a Fanfiction.net downloader and download stories.
          */
        if (!parser.getFfnUrls().isEmpty()) {
            FanfictionNetDL fanfictionNetDL = new FanfictionNetDL(parser.getFfnUrls());
            fanfictionNetDL.download();
            System.out.printf(C.FINISHED_WITH_SITE, FanfictionNetDL.SITE);
        }
        /*
        Create a SIYE downloader and download stories.
          */
        if (!parser.getSiyeUrls().isEmpty()) {
            SiyeDL siyeDL = new SiyeDL(parser.getSiyeUrls());
            siyeDL.download();
            System.out.printf(C.FINISHED_WITH_SITE, SiyeDL.SITE);
        }
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
        return new ArrayList<>(urlList.stream().map(Main::downloadHtml).filter(out -> out != null)
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
}
