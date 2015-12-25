package bkromhout.fdl.storys;

import bkromhout.fdl.downloaders.ParsingDL;
import bkromhout.fdl.ex.InitStoryException;
import bkromhout.fdl.util.C;
import bkromhout.fdl.util.Sites;
import bkromhout.fdl.util.Util;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Model object for a <a href="http://thebroomcupboard.net">The Broom Cupboard</a> story.
 */
public class TbcStory extends Story {
    /**
     * Base url to help create chapter urls.
     */
    private static final String TBC_BASE_URL = "www.thebroomcupboard.net";

    /**
     * Create a new {@link TbcStory} based off of a url.
     * @param ownerDl The parsing downloader which owns this story.
     * @param url     url of the story this model represents.
     * @throws InitStoryException if we can't create this story object for some reason.
     */
    public TbcStory(ParsingDL ownerDl, String url) throws InitStoryException {
        super(ownerDl, url, Sites.TBC());
    }

    @Override
    protected void populateInfo() throws InitStoryException {
        // Since TBC URLs don't have story IDs, we have to figure out chapter URLs ourselves. We'll do this first so
        // that we have a first (or only) chapter which we can scrape details from.
        Document infoDoc = Util.downloadHtml(url);
        if (infoDoc == null || infoDoc.select("div#nav25").first() == null) throw initEx(NO_ID_DL_FAIL, url);
        // Now check and see if we have a select box.
        Element selectElem = infoDoc.select("select").first();
        if (selectElem != null) {
            // If so, we need to create chapter urls from it now.
            Elements selOptions = selectElem.select("option");
            for (Element option : selOptions) chapterUrls.add(TBC_BASE_URL + option.attr("value"));
            // Set story url to first chapter url.
            url = chapterUrls.get(0);
            // Re-download infoDoc.
            infoDoc = Util.downloadHtml(url);
            if (infoDoc == null) throw initEx();
        } else {
            // Otherwise, this is a oneshot.
            chapterUrls.add(url);
        }
        // Get title (making sure to remove extra series info from title element first).
        Element detailDiv = infoDoc.select("div:has(b:contains(Story))").first();
        if (detailDiv.select("ul").first() != null) detailDiv.select("ul").first().remove();
        title = detailDiv.text().replace("Story:", "").trim();
        // Get author.
        detailDiv = infoDoc.select("div:has(b:contains(Author))").first();
        author = detailDiv.text().replace("Author:", "").replace("- Add Author To Your Email Update List", "").trim();
        // Get summary.
        summary = getSummaryFromAuthorPage(detailDiv);
        // Get rating.
        detailDiv = infoDoc.select("div:has(b:contains(Rating))").first();
        rating = detailDiv.text().replace("Rating:", "").trim();
        // Get fic type.
        detailDiv = infoDoc.select("div:has(b:contains(Setting))").first();
        ficType = detailDiv.text().replace("Setting:", "").trim();
        // Get status.
        detailDiv = infoDoc.select("div:has(b:contains(Status))").first();
        status = detailDiv.text().contains("Completed") ? C.STAT_C : C.STAT_I;
        // Get word count.
        detailDiv = infoDoc.select("div:has(b:contains(Words))").first();
        wordCount = Integer.parseInt(detailDiv.text().replace("Words:", "").trim());
        // Get date published.
        detailDiv = infoDoc.select("div:has(b:contains(Updated))").first();
        datePublished = detailDiv.text().replace("Updated:", "").trim();
        // Get date last updated.
        dateUpdated = chapterUrls.size() > 1 ? getDateUpdatedFromLastChap() : datePublished;
        if (dateUpdated == null) throw initEx(NO_ID_DL_FAIL, url);
    }

    /**
     * The date "updated" for Broom Cupboard stories is different for each chapter. We're already using the first
     * chapter to get the date published by default, but for multi-chapter stories we have to download the last chapter
     * to get the real date updated.
     * @return Date updated from last chapter, or null if we couldn't get it.
     */
    private String getDateUpdatedFromLastChap() {
        // We'll need to download the last chapter to find the last updated date if this isn't a oneshot.
        Document lastChap = Util.downloadHtml(chapterUrls.get(chapterUrls.size() - 1));
        if (lastChap == null) return null;
        return lastChap.select("div:has(b:contains(Updated))").first().text().replace("Updated:", "").trim();
    }

    /**
     * The summary for Broom Cupboard stories is only accessible from the story entry on the author page, so we have to
     * go there to get it.
     * @param authorDiv Element from the story page which contains a link to the author page.
     * @return Summary string, or null if we couldn't get it.
     */
    private String getSummaryFromAuthorPage(Element authorDiv) {
        // Make the absolute url to the author page and download it.
        String authorLink = TBC_BASE_URL + authorDiv.select("a").first().attr("href");
        Document authorPage = Util.downloadHtml(authorLink);
        if (authorPage == null) return null;
        // Create a relative story link in order to find only summary elements that occur after an <a> element that
        // has this relative story link.
        String relStoryLink = url.replace(TBC_BASE_URL, "");
        // Get the first summary element that occurs after an <a> element which has the relative story link.
        Element summaryElem = authorPage
                .select(String.format("div:has(a[href=\"%s\"]) ~ div:has(b:contains(Summary))", relStoryLink)).first();
        if (summaryElem == null) return null;
        // Clean up the summary string and return it.
        return Util.cleanHtmlString(summaryElem.text().replace("Summary:", "").trim());
    }
}
