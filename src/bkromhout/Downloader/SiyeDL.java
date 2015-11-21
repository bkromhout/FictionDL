package bkromhout.Downloader;

import java.util.ArrayList;

/**
 * Downloader for siye.co.uk ("Sink Into Your Eyes").
 */
public class SiyeDL {
    public static final String SITE = "SIYE";
    // List of SIYE URLs.
    ArrayList<String> urls;

    /**
     * Create a new SIYE downloader.
     * @param urls SIYE URLs to download.
     */
    public SiyeDL(ArrayList<String> urls) {
        this.urls = urls;
    }

    /**
     * Download the stories whose URLs were passed to this instance of the downloader upon creation..
     */
    public void download() {
        // TODO: this
    }
}
