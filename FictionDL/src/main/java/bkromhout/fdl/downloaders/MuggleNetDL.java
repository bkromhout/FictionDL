package bkromhout.fdl.downloaders;

import bkromhout.fdl.C;
import bkromhout.fdl.Chapter;
import bkromhout.fdl.FictionDL;
import bkromhout.fdl.storys.MuggleNetStory;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.RequestBody;
import org.jsoup.nodes.Element;
import rx.functions.Action1;

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
     * Creates an action which takes a Chapter object and creates a title for it by parsing the real chapter titles from
     * the raw chapter HTML.
     * @return An action which generates chapter titles.
     */
    @Override
    protected Action1<? super Chapter> generateChapTitle() {
        return chapter -> {
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
                    chapter.title = String.format("Chapter %d", chapter.num);
                }
            } else {
                chapter.title = "Chapter 1";
            }
        };
    }

    /**
     * Creates an action which takes a Chapter objects and fills in its content field by extracting a desired part of
     * the raw HTML.
     * <p>
     * MuggleNet needs to have number of elements removed for div.contentLeft, then we'll add <hr />s between story and
     * notes.
     * @return An action which fills in the {@link Chapter#content content} field of the given Chapter.
     * @see Chapter
     */
    @Override
    protected Action1<? super Chapter> extractChapText() {
        return chapter -> {
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
        };
    }
}
