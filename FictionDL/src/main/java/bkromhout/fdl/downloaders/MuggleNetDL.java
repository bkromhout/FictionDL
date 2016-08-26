package bkromhout.fdl.downloaders;

import bkromhout.fdl.chapter.Chapter;
import bkromhout.fdl.site.Sites;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Downloader for <a href="http://fanfiction.mugglenet.com">MuggleNet</a> stories.
 */
public class MuggleNetDL extends ParsingDL {
    /**
     * MuggleNet login page url.
     */
    private static final String MN_L_URL = "http://fanfiction.mugglenet.com/user.php?action=login";

    /**
     * Create a new {@link MuggleNetDL}.
     */
    public MuggleNetDL() {
        super(Sites.MN(), "div.gb-full blockquote, div#story");
    }

    @Override
    protected RequestBody getSiteAuthForm(String u, String p) {
        if (u == null || u.isEmpty() || p == null || p.isEmpty()) return null;
        return new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
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
     * Creates a title for a chapter by parsing the actual title from {@link Chapter#rawHtml}.
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
     * MuggleNet chapters' raw HTML first needs to have number of extra elements removed from
     * <code>div.contentLeft</code> (which is where the various notes and the chapter content are, alongside the extra
     * elements), then we'll add <code>&lt;hr /&gt;</code>s between chapter content and any notes.
     * <p>
     * At most, there can be a section for story notes at the top, followed by a top author's notes section, followed by
     * the chapter content, followed by another author's notes section at the bottom.
     * @param chapter Chapter object.
     * @see Chapter
     */
    @Override
    protected void extractChapText(Chapter chapter) {
        StringBuilder chapterText = new StringBuilder();

        Elements content = chapter.rawHtml.select(chapTextSelector);
        Iterator<Element> iterator = content.iterator();
        while (iterator.hasNext()) {
            chapterText.append(iterator.next().html());
            if (iterator.hasNext()) chapterText.append("<hr />");
        }
        chapter.contentFromString(chapterText.toString().replace("blockquote", "div"));
    }
}
