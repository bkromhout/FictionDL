package bkromhout.fdl.downloaders;

import bkromhout.fdl.Chapter;
import bkromhout.fdl.FictionDL;
import bkromhout.fdl.util.Sites;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.RequestBody;

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
        // TODO
    }

    /**
     * The Broom Cupboard's raw HTML doesn't have a single CSS selector string which can select only the story content
     * from it. The closest thing to it still contains many detail elements which we will want to strip.
     * @param chapter Chapter object.
     */
    @Override
    protected void extractChapText(Chapter chapter) {
        // TODO
    }
}
