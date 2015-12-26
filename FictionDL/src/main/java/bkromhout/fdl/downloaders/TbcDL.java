package bkromhout.fdl.downloaders;

import bkromhout.fdl.Chapter;
import bkromhout.fdl.FictionDL;
import bkromhout.fdl.util.Sites;
import bkromhout.fdl.util.Util;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.RequestBody;
import org.jsoup.nodes.Element;

/**
 * Downloader for <a href="http://thebroomcupboard.net">The Broom Cupboard</a> stories.
 */
public class TbcDL extends ParsingDL {
    /**
     * The Broom Cupboard login page url.
     */
    private static final String TBC_L_URL = "http://thebroomcupboard.net/processlogin.php?cid=";

    /**
     * Create a new {@link TbcDL}.
     * @param fictionDL FictionDL object which owns this downloader.
     */
    public TbcDL(FictionDL fictionDL) {
        super(fictionDL, Sites.TBC(), null);
    }

    @Override
    protected RequestBody getSiteAuthForm(String u, String p) {
        if (u == null || u.isEmpty() || p == null || p.isEmpty()) return null;
        return new MultipartBuilder().type(MultipartBuilder.FORM)
                                     .addFormDataPart("txtusername", u)
                                     .addFormDataPart("txtpassword", p)
                                     .addFormDataPart("remember", "yes")
                                     .addFormDataPart("login", "Login")
                                     .build();
    }

    @Override
    protected String getSiteLoginUrl() {
        return TBC_L_URL;
    }

    /**
     * Creates a title for a chapter by parsing the actual title from {@link Chapter#rawHtml}.
     * @param chapter Chapter object.
     */
    @Override
    protected void generateChapTitle(Chapter chapter) {
        // All Broom Cupboard stories have their chapter titles in a common place. :)
        chapter.title = chapter.rawHtml.select("div#nav25 > center").first().text();
    }

    /**
     * The Broom Cupboard's raw HTML doesn't have a single CSS selector string which can select only the story content
     * from it. The closest thing to it still contains many detail elements which we will want to strip so that we are
     * left with only the chapter content.
     * <p>
     * The easiest way to do this is to simply copy the child nodes of the aforementioned "closest" element within a
     * certain range of indices.
     * @param chapter Chapter object.
     */
    @Override
    protected void extractChapText(Chapter chapter) {
        // Get content container div from raw HTML.
        Element container = chapter.rawHtml.select("div#nav25").first();
        // Start index should be 2 more than the index of the chapter title. This way, we don't include the actual
        // title or the obligatory empty <p> that immediately follows it.
        int startIdx = container.childNodes().indexOf(container.select("center").first()) + 2;
        // End index should be 2 less that then number of child nodes. This way, we don't include the last <p>
        // element, which is always either empty (for oneshots) or a <select> element (for chaptered stories).
        int endIdx = container.childNodes().size() - 1;
        // Create the chapter content string and put it into the chapter.
        chapter.contentFromString(Util.divFromChildCopies(container, startIdx, endIdx).html().trim());
    }
}