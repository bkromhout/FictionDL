package bkromhout.fdl.downloaders;

import bkromhout.fdl.Chapter;
import bkromhout.fdl.FictionDL;
import bkromhout.fdl.ESite;
import bkromhout.fdl.storys.TbcStory;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.RequestBody;

import java.util.HashSet;

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
     * @param urls      List of Broom Cupboard urls.
     */
    public TbcDL(FictionDL fictionDL, HashSet<String> urls) {
        super(fictionDL, TbcStory.class, ESite.TBC, urls, null);
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
        super.generateChapTitle(chapter);
    }

    /**
     * The Broom Cupboard's raw HTML is
     * @param chapter Chapter object.
     */
    @Override
    protected void extractChapText(Chapter chapter) {
        super.extractChapText(chapter);
    }
}
