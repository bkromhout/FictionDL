package bkromhout.fdl.stories;

import bkromhout.fdl.ex.InitStoryException;
import bkromhout.fdl.parsing.StoryEntry;
import bkromhout.fdl.site.Sites;
import bkromhout.fdl.util.C;
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
     * Create a new {@link Ao3Story}.
     * @param storyEntry Story entry with details from the input file.
     * @throws InitStoryException if we can't create this story object for some reason.
     */
    public Ao3Story(StoryEntry storyEntry) throws InitStoryException {
        super(storyEntry, Sites.AO3());
    }

    @Override
    protected void populateInfo() throws InitStoryException {
        // Get story ID and use it to normalize the url, then download the url so that we can parse story info.
        storyId = parseStoryId(url, "/works/(\\d*)", 1);
        url = String.format(AO3_S_URL, storyId);
        Document infoDoc = Util.getHtml(url);
        // Make sure that we got a Document and that this is a valid story.
        if (infoDoc == null || infoDoc.select("div[class*=\"error\"]").first() != null)
            throw new InitStoryException(C.STORY_DL_FAILED, site.getName(), storyId);

        // Get the title and author so that we can name the ePUB file we will download.
        title = hasDetailTag(C.J_TITLE) ? detailTags.get(C.J_TITLE)
                : infoDoc.select("h2[class=\"title heading\"]").first().text().trim();
        author = hasDetailTag(C.J_AUTHOR) ? detailTags.get(C.J_AUTHOR)
                : infoDoc.select("a[rel=\"author\"]").first().text().trim();

        // Now set the url to be a link to download the ePUB file with. Find the link to the ePUB file from the page.
        url = infoDoc.select("a:contains(EPUB)").first().absUrl("href");
        if (url == null) throw new InitStoryException(C.NO_EPUB_ON_SITE, site.getName(), title);
    }
}
