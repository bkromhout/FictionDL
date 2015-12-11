package bkromhout.FictionDL.Story;

import bkromhout.FictionDL.C;
import bkromhout.FictionDL.Downloader.EpubDL;
import bkromhout.FictionDL.Util;
import bkromhout.FictionDL.ex.InitStoryException;
import org.jsoup.nodes.Document;

/**
 * Model object for a Ao3 (Archive of Our Own) story.
 */
public class Ao3Story extends Story {

    /**
     * Create a new Ao3Story object based off of a URL.
     * @param ownerDl The ePUB downloader which owns this story.
     * @param url     Story URL.
     * @throws InitStoryException if we can't create this story object for some reason.
     */
    public Ao3Story(EpubDL ownerDl, String url) throws InitStoryException {
        super(ownerDl, url);
    }

    @Override
    protected void populateInfo() throws InitStoryException {
        // Set site.
        hostSite = C.HOST_AO3;
        // Get story ID first.
        storyId = parseStoryId(url, C.AO3_SID_REGEX, 1);
        // Normalize the URL, since there are many valid FFN URL formats.
        url = String.format(C.AO3_S_URL, storyId);
        // Get the first chapter in order to parse the story info.
        Document infoDoc = Util.downloadHtml(url);
        // Make sure that we got a Document and that this is a valid story.
        if (infoDoc == null || infoDoc.select("div[class*=\"error\"]").first() != null) throw initEx();
        // Get the title and author so that we can name the ePUB file we will download.
        title = infoDoc.select("h2[class=\"title heading\"]").first().text().trim();
        author = infoDoc.select("a[rel=\"author\"]").first().text().trim();
        // Now set the URL to be a link to download the ePUB file with. Find the link to the ePUB file from the page.
        url = infoDoc.select("a:contains(EPUB)").first().absUrl("href");
        if (url == null) throw initEx(C.NO_EPUB, title);
    }
}
