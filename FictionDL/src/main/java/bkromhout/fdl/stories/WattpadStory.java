package bkromhout.fdl.stories;

import bkromhout.fdl.ex.InitStoryException;
import bkromhout.fdl.models.WattpadChapterInfo;
import bkromhout.fdl.models.WattpadStoryInfo;
import bkromhout.fdl.parsing.StoryEntry;
import bkromhout.fdl.site.Site;
import bkromhout.fdl.site.Sites;
import bkromhout.fdl.util.C;
import bkromhout.fdl.util.Util;
import okhttp3.HttpUrl;
import org.jsoup.nodes.Document;

/**
 * Created by bkromhout on 8/26/16.
 * @author Brenden Kromhout
 */
public class WattpadStory extends Story {
    /**
     * Key string template for storing chapter titles which were parsed from the story info to be used when generating
     * chapter titles.
     */
    public static final String CHAP_TITLE_KEY_TEMPLATE = "__chapter %d title__";
    /**
     * Wattpad story URL, needs story ID substituted into it. Will return a JSON object with story and chapter info,
     * which we parse into a {@link bkromhout.fdl.models.WattpadStoryInfo} object.
     */
    private static final String WP_S_URL = "https://www.wattpad.com/api/v3/stories/%s?fields=id,title,user(name)," +
            "createDate,modifyDate,description,tags,cover,completed,mature,url,numParts,parts(id,title,wordCount," +
            "text_url(text))";

    /**
     * Create a new {@link WattpadStory}.
     * @param storyEntry Story entry with details from the input file.
     * @throws InitStoryException if we can't create this story object for some reason.
     */
    public WattpadStory(StoryEntry storyEntry) throws InitStoryException {
        super(storyEntry, Sites.WP());
    }

    @Override
    protected void populateInfo() throws InitStoryException {
        // Get story info JSON from wattpad.
        storyId = parseStoryId(url, "/story/(\\d*)", 1);
        WattpadStoryInfo storyInfo = Util.getModel(String.format(WP_S_URL, storyId), WattpadStoryInfo.class);
        if (storyInfo == null) throw new InitStoryException(C.STORY_DL_FAILED, site.getName(), storyId);

        // Read story info.
        title = hasDetailTag(C.J_TITLE) ? detailTags.get(C.J_TITLE) : storyInfo.getTitle();
        author = hasDetailTag(C.J_AUTHOR) ? detailTags.get(C.J_AUTHOR) : storyInfo.getAuthor();
        datePublished = storyInfo.getCreateDate().split("T")[0];
        dateUpdated = storyInfo.getModifyDate().split("T")[0];
        summary = hasDetailTag(C.J_SUMMARY) ? detailTags.get(C.J_SUMMARY) : getSummary(storyInfo);
        genres = hasDetailTag(C.J_GENRES) ? detailTags.get(C.J_GENRES)
                : String.join(", ", (CharSequence[]) storyInfo.getTags());
        status = storyInfo.isCompleted() ? C.STAT_C : C.STAT_I;
        rating = hasDetailTag(C.J_RATING) ? detailTags.get(C.J_RATING) :
                (storyInfo.isMature() ? "Mature" : "Not Mature");

        // Download cover image.
        HttpUrl coverImageUrl = HttpUrl.parse(storyInfo.getCoverUrl());
        coverImageFileName = coverImageUrl.pathSegments().get(coverImageUrl.pathSegments().size() - 1);
        coverImage = Util.getBinary(coverImageUrl.toString());

        // Detail tags which the site doesn't support.
        if (detailTags.containsKey(C.J_SERIES)) series = detailTags.get(C.J_SERIES);
        if (detailTags.containsKey(C.J_FIC_TYPE)) ficType = detailTags.get(C.J_FIC_TYPE);
        if (detailTags.containsKey(C.J_WARNINGS)) warnings = detailTags.get(C.J_WARNINGS);
        if (detailTags.containsKey(C.J_CHARACTERS)) characters = detailTags.get(C.J_CHARACTERS);

        // Read the chapter info from the story info.
        for (int i = 0; i < storyInfo.getParts().length; i++) {
            WattpadChapterInfo chapterInfo = storyInfo.getParts()[i];

            // Add chapter text URL to chapter URLs.
            chapterUrls.add(chapterInfo.getChapterTextUrl());
            // Increment word count by chapter word count.
            wordCount += chapterInfo.getWordCount();
            // Story chapter title for later on.
            detailTags.put(String.format(CHAP_TITLE_KEY_TEMPLATE, i + 1), chapterInfo.getTitle());
        }
    }

    /**
     * Get the formatted description instead of the plaintext one.
     * @param storyInfo Story info object.
     * @return Formatted description.
     */
    private String getSummary(WattpadStoryInfo storyInfo) {
        Document storyPage = Util.getHtml(url);
        if (storyPage == null) return storyInfo.getDescription();
        return storyPage.select("h2.description>pre").first().html().replace("\n", "<br />");
    }
}
