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
     * Base url to help create urls.
     */
    private static final String TBC_BASE_URL = "http://www.thebroomcupboard.net";
    /**
     * Chapter url, needs chapter id filled in.
     */
    private static final String TBC_C_URL = TBC_BASE_URL + "/mating/story/%s/";
    /**
     * Regex to parse chapter ID.
     */
    private static final String CHAP_ID_REGEX = "/story/(\\d*)";

    /**
     * Create a new {@link TbcStory} based off of a url.
     * @param ownerDl The parsing downloader which owns this story.
     * @param url     url of the story this model represents.
     * @throws InitStoryException if we can't create this story object for some reason.
     */
    public TbcStory(ParsingDL ownerDl, String url) throws InitStoryException {
        super(url, Sites.TBC());
    }

    @Override
    protected void populateInfo() throws InitStoryException {
        // Parse fake story ID (really a chapter ID) to normalize the url.
        url = String.format(TBC_C_URL, parseStoryId(url, CHAP_ID_REGEX, 1));
        // Since TBC URLs don't have true story IDs, we have to figure out chapter URLs ourselves. We'll do this
        // first so that we have a first (or only) chapter which we can scrape details from.
        Document infoDoc = Util.downloadHtml(url);
        if (infoDoc == null || infoDoc.select("div#nav25").first() == null) throw initEx(NO_ID_DL_FAIL, url);

        // Now check and see if we have a select box.
        Element selectElem = infoDoc.select("select").first();
        if (selectElem != null) {
            // If so, we need to create chapter urls from it now.
            Elements selOptions = selectElem.select("option");
            for (Element option : selOptions)
                chapterUrls.add(String.format(TBC_C_URL, parseStoryId(option.attr("value"), CHAP_ID_REGEX, 1)));
            // If the first chapter's url isn't the same as the one we just got, re-download it.
            if (!url.equals(chapterUrls.get(0))) {
                url = chapterUrls.get(0);
                infoDoc = Util.downloadHtml(url);
                if (infoDoc == null) throw initEx();
            }
        } else {
            // Otherwise, this is a oneshot.
            chapterUrls.add(url);
        }

        Element detailDiv = infoDoc.select("div#nav25 > div:has(b:contains(Story))").first();
        // Make sure to remove extra series info from title element before parsing title.
        if (detailDiv.select("ul").first() != null) detailDiv.select("ul").first().remove();
        title = detailDiv.text().replace("Story:", "").trim();

        detailDiv = infoDoc.select("div#nav25 > div:has(b:contains(Author))").first();
        author = detailDiv.text().replace("Author:", "").replace("- Add Author To Your Email Update List", "").trim();

        // Download author page, we'll need it to get summary and total word count.
        Document authorPage = Util.downloadHtml(TBC_BASE_URL + detailDiv.select("a").first().attr("href"));
        if (authorPage == null) throw initEx(NO_ID_DL_FAIL, url);

        // Create a relative story link in order to find only summary/word count elements that occur after an <a>
        // element that has this relative story link on the author page.
        String relStoryLink = url.replace(TBC_BASE_URL, "");
        // Create CSS selector strings for the summary and word count.
        String summaryCssSel = String.format("div:has(a[href=\"%s\"]) ~ div:has(b:contains(Summary))", relStoryLink);
        String wordCountCssSel = String.format("div:has(a[href=\"%s\"]) ~ div:has(b:contains(Words))", relStoryLink);

        detailDiv = authorPage.select(summaryCssSel).first();
        summary = Util.cleanHtmlString(detailDiv.text().replace("Summary:", "").trim());

        detailDiv = authorPage.select(wordCountCssSel).first();
        wordCount = Integer.parseInt(detailDiv.text().replace("Words:", "").replace(",", "").trim());

        detailDiv = infoDoc.select("div#nav25 > div:has(b:contains(Rating))").first();
        rating = detailDiv.text().replace("Rating:", "").trim();

        detailDiv = infoDoc.select("div#nav25 > div:has(b:contains(Setting))").first();
        ficType = detailDiv.text().replace("Setting:", "").trim();

        detailDiv = infoDoc.select("div#nav25 > div:has(b:contains(Status))").first();
        status = detailDiv.text().contains("Completed") ? C.STAT_C : C.STAT_I;

        detailDiv = infoDoc.select("div#nav25 > div:has(b:contains(Updated))").first();
        datePublished = detailDiv.text().replace("Updated:", "").trim();

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
        Document lastC = Util.downloadHtml(chapterUrls.get(chapterUrls.size() - 1));
        if (lastC == null) return null;
        return lastC.select("div#nav25 > div:has(b:contains(Updated))").first().text().replace("Updated:", "").trim();
    }
}
