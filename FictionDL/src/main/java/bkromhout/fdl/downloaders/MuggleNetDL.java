package bkromhout.fdl.downloaders;

import bkromhout.fdl.C;
import bkromhout.fdl.Chapter;
import bkromhout.fdl.FictionDL;
import bkromhout.fdl.storys.MuggleNetStory;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.RequestBody;
import org.jsoup.nodes.Element;

import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Downloader for MuggleNet stories.
 */
public class MuggleNetDL extends ParsingDL {
    /**
     * MuggleNet login page link.
     */
    private static final String MN_L_URL = "http://fanfiction.mugglenet.com/user.php?action=login";

    /**
     * Create a new MuggleNet downloader.
     * @param fictionDL FictionDL object which owns this downloader.
     * @param urls      List of MuggleNet URLs.
     */
    public MuggleNetDL(FictionDL fictionDL, HashSet<String> urls) {
        super(fictionDL, MuggleNetStory.class, C.NAME_MN, urls, "div.contentLeft");
    }

    @Override
    protected RequestBody getSiteAuthForm(String u, String p) {
        if (u == null || u.isEmpty() || p == null || p.isEmpty()) return null;
        return new MultipartBuilder().type(MultipartBuilder.FORM)
                                     .addFormDataPart("penname", u)
                                     .addFormDataPart("password", p)
                                     .addFormDataPart("cookiecheck", "1")
                                     .addFormDataPart("submit", "Submit")
                                     .build();
    }

    @Override
    protected String getSiteLoginUrl() {
        return MN_L_URL;
    }

    /**
     * Creates a title for a chapter by parsing the actual title form the raw chapter HTML in the given Chapter object.
     * @param chapter Chapter object.
     */
    @Override
    protected void generateChapTitle(Chapter chapter) {
        // Try to find a <select> element on the page that has chapter titles.
        Element titleElement = chapter.rawHtml.select("select[name=\"chapter\"] > option[selected]").first();
        // If the story is chaptered, we'll find the <select> element and can get the chapter title from that (we
        // strip off the leading "#. " part of it). If the story is only one chapter, we just call it "Chapter 1".
        if (titleElement != null) {
            Matcher matcher = Pattern.compile("(\\d+.\\s)(.*)").matcher(titleElement.html().trim());
            matcher.matches();
            try {
                chapter.title = matcher.group(2);
            } catch (IllegalStateException e) {
                // Apparently, it's possible for there to *not* be a title for a chapter, so the title string may
                // look like "24. " or something. If that happens, title the chapter "Chapter #".
                chapter.title = String.format("Chapter %d", chapter.number);
            }
        } else {
            chapter.title = "Chapter 1";
        }
    }

    /**
     * MuggleNet chapters' raw HTML first needs to have number of extra elements removed from `div.contentLeft` (which
     * is where the various notes and the chapter content are, alongside the extra elements), then we'll add `&lt;hr
     * /&gt;`s between chapter content and any notes.
     * <p>
     * At most, there can be a section for story notes at the top, followed by a top author's notes section, followed by
     * the chapter content, followed by another author's notes section at the bottom.
     * @param chapter Chapter object.
     * @see Chapter
     */
    @Override
    protected void extractChapText(Chapter chapter) {
        StringBuilder chapterText = new StringBuilder();
        // First off, we need to drill down to just the div.contentLeft element.
        Element content = chapter.rawHtml.select(chapTextSelector).first();
        // Now, we want to strip out any children of div.contentLeft which are not div.notes or div#story, so select
        // all of those and remove them.
        content.select("div.contentLeft > *:not(div.notes, div#story)").remove();
        // Now, we want to insert <hr /> tags between any remaining divs.
        content.children().after("<hr />");
        content.select("hr").last().remove();
        // Now we can finally output the html.
        chapterText.append(content.html());
        chapter.content = String.format(C.CHAPTER_PAGE, chapter.title, chapter.title, chapterText.toString());
    }
}
