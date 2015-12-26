package bkromhout.fdl.storys;

import bkromhout.fdl.downloaders.EpubDL;
import bkromhout.fdl.ex.InitStoryException;
import bkromhout.fdl.util.Sites;
import bkromhout.fdl.util.Util;
import org.jsoup.nodes.Document;

/**
 * Model object for an <a href="http://archiveofourown.org/">Ao3</a> story.
 */
public class Ao3Story extends Story {
    /**
     * Ao3 story info link, just needs story ID substituted into it.
     */
    private static final String AO3_S_URL = "http://archiveofourown.org/works/%s?view_adult=true";

    /**
     * Create a new {@link Ao3Story} based off of a url.
     * @param ownerDl The ePUB downloader which owns this story.
     * @param url     Story url.
     * @throws InitStoryException if we can't create this story object for some reason.
     */
    public Ao3Story(EpubDL ownerDl, String url) throws InitStoryException {
        super(ownerDl, url, Sites.AO3());
    }

    @Override
    protected void populateInfo() throws InitStoryException {
        // Get story ID and use it to normalize the url, then download the url so that we can parse story info.
        storyId = parseStoryId(url, "/works/(\\d*)", 1);
        url = String.format(AO3_S_URL, storyId);
        Document infoDoc = Util.downloadHtml(url);
        // Make sure that we got a Document and that this is a valid story.
        if (infoDoc == null || infoDoc.select("div[class*=\"error\"]").first() != null) throw initEx();

        // Get the title and author so that we can name the ePUB file we will download.
        title = infoDoc.select("h2[class=\"title heading\"]").first().text().trim();
        author = infoDoc.select("a[rel=\"author\"]").first().text().trim();

        // Now set the url to be a link to download the ePUB file with. Find the link to the ePUB file from the page.
        url = infoDoc.select("a:contains(EPUB)").first().absUrl("href");
        if (url == null) throw initEx(Story.NO_EPUB, title);
    }
}
