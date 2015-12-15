package bkromhout.fdl;

import com.squareup.okhttp.OkHttpClient;

/**
 * Constants file. Only constants which are either used by multiple classes (or are likely to be in the future) or which
 * are exceedingly long (in the case of strings) belong here. All others should be in a more specific class.
 */
public abstract class C {
    /**
     * Program version string.
     */
    public static final String VER_STRING = "FictionDL, Version 2.0.1";

    /**
     * This is here for code-style convenience. Typing C.getHttpClient() is much shorter than FictionDl.httpClient ;)
     * @return The OkHttpClient from FictionDl.
     */
    public static OkHttpClient getHttpClient() {
        return FictionDL.httpClient;
    }

    /*
    Log style tags. Putting these anywhere within a log string will cause the string to be formatted accordingly if
    it's printed to the TextFlow log in the GUI. The tags are stripped before the string is printed. One color and
    multiple styles can apply, and apply to the *whole* string.
     */
    public static final String LOG_RED = "!red!";
    public static final String LOG_BLUE = "!blue!";
    public static final String LOG_GREEN = "!green!";

    /*
    Keys.
     */
    public static final String ARG_IN_PATH = "arg_in_path";
    public static final String ARG_OUT_PATH = "arg_out_path";
    public static final String ARG_CFG_PATH = "arg_cfg_path";

    /*
    Log Strings.
     */
    // General.
    public static final String DONE = "Done!" + LOG_GREEN;

    public static final String PARSING_FILE = "Parsing %s file...";

    public static final String STARTING_SITE_DL_PROCESS = "\nStarting %s download process...\n" + LOG_BLUE;

    public static final String STARTING_SITE_AUTH_PROCESS = "\nAttempting to log in to %s..." + LOG_BLUE;

    public static final String FETCH_BUILD_MODELS = "Fetching story infos from %s and building story models...\n";

    public static final String DL_STORIES_FROM_SITE = "Downloading stories from %s...\n\n";

    public static final String DL_CONTENT_FOR = "Downloading: \"%s\"\n";

    public static final String SANITIZING_CHAPS = "Sanitizing chapters...";

    public static final String SAVING_STORY = "Saving Story...";

    public static final String DL_EPUB_FOR = "Downloading ePUB for: \"%s\"...";

    public static final String FINISHED_WITH_SITE = "Finished with %s.\n\n" + LOG_BLUE;

    public static final String ALL_FINISHED = "\nAll Finished! :)" + LOG_GREEN;

    // FictionHunt-specific.
    public static final String FH_ON_FFN = "\"%s\" is still available on FanFiction.net; will download from there.\n\n";

    public static final String FH_FFN_CHECK_FAILED = "Failed to check if story is still active on FFN. Oh well, " +
            "never hurts to try!" + LOG_RED;

    // SIYE-specific.
    public static final String SIYE_PRE_DL = "SIYE's process is *very slow* due to their horrid HTML structure, " +
            "please be patient ;)";

    // Ao3-specific.
    public static final String AO3_PRE_DL = "Ao3 stories occasionally fail to download, just try them again.";

    // Non-site-specific errors and warnings.
    public static final String INVALID_ARGS = "Bad arguments.";

    public static final String INVALID_PATH = "Invalid path: \"%s\"\n." + LOG_RED;

    public static final String NO_INPUT_PATH = "[No input file path!]";

    public static final String INVALID_URL = "Invalid URL: \"%s\"\n." + LOG_RED;

    public static final String PROCESS_LINE_FAILED = "Couldn't process this line from the %s file: \"%s\"\n" + LOG_RED;

    public static final String HTML_DL_FAILED = "Failed to download HTML from: \"%s\"\n" + LOG_RED;

    public static final String PARSE_HTML_FAILED = "Couldn't parse HTML for \"%s\"" + LOG_RED;

    public static final String STORY_DL_FAILED = "Couldn't get %s story with ID=%s. Skipping it." + LOG_RED;

    public static final String SOME_CHAPS_FAILED = "Skipping this story; some chapters failed to download!\n" + LOG_RED;

    public static final String SAVE_FILE_FAILED = "Failed to save file: \"%s\"\n" + LOG_RED;

    public static final String NO_EPUB_ON_SITE = "Couldn't find ePUB on %s for story \"%s\". Skipping it." + LOG_RED;

    public static final String MUST_LOGIN = "You must provide %s login info to download story with ID=%s!" + LOG_RED;

    public static final String LOGIN_FAILED = "\nCouldn't log in to %s. Check your login info.\n" + LOG_RED;

    // Error strings used by exceptions which we don't catch. Don't include log tags, they won't be stripped!
    public static final String HTML_UNEXP_RESP = "Unexpected result when trying to download HTML from \"%s\"\n";

    public static final String CHAP_NUM_NOT_ASSIGNED = "Chapter number hasn't been assigned yet!";

    /*
    Domains and human-readable names for supported sites.
     */
    public static final String NAME_FFN = "FanFiction.net";
    public static final String HOST_FFN = "fanfiction.net";

    public static final String NAME_FH = "FictionHunt";
    public static final String HOST_FH = "fictionhunt.com";

    public static final String NAME_SIYE = "SIYE";
    public static final String HOST_SIYE = "siye.co.uk";

    public static final String NAME_MN = "MuggleNet";
    public static final String HOST_MN = "fanfiction.mugglenet.com";

    public static final String NAME_AO3 = "Ao3";
    public static final String HOST_AO3 = "archiveofourown.org";

    /*
    Default/specific detail values.
     */
    /**
     * Useful for anything we want to set to None.
     */
    public static final String NONE = "None";

    /**
     * Used when a story doesn't have a genre.
     */
    public static final String NO_GENRE = "None/Gen";

    /**
     * Used for completed stories.
     */
    public static final String STAT_C = "Complete";

    /**
     * Used for incomplete stories.
     */
    public static final String STAT_I = "Incomplete";

    /*
    File Template Strings. These are long and would be annoying to have in the EpubCreator class.
     */
    /**
     * Yay CSS. It doesn't honestly matter that much anyway though, ePUB readers can do whatever they want to.
     */
    public static final String CSS = "#chapText{\ttext-align: left;\tfont: 1em Calibri;\tline-height: 1.05em;}" +
            "#chapTitle{\tfont: bold 1.2em Calibri;\ttext-align: left;\tline-height: 1.25em;}" +
            "#ficTitle{\tfont: bold 1.7em Calibri;\ttext-align: center;}" +
            "#ficAuthor{\tfont: 1.4em Calibri;\ttext-align: center;}" +
            "#footer {position: absolute; bottom: 0; width: 100%; height :60px; font: 1em Calibri}" +
            "body{\ttext-align: left;\tfont: 1em Calibri;\tline-height: 1.05em;}";

    /**
     * Chapter page. Has a number of areas for replacement using String.format(): Chapter title, Chapter title, Chapter
     * text (HTML!).
     */
    public static final String CHAPTER_PAGE = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
            "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\"\n" +
            "  \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">\n" +
            "\n" +
            "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
            "<head>\n" +
            "  <link href=\"../Styles/style.css\" rel=\"stylesheet\" type=\"text/css\" />\n" +
            "  <meta content=\"http://www.w3.org/1999/xhtml; charset=utf-8\" http-equiv=\"Content-Type\" />\n" +
            "  <title>%s</title>\n" +
            "</head>\n" +
            "\n" +
            "<body>\n" +
            "  <h1 id=\"chapTitle\">%s</h1>\n" +
            "\n" +
            "  <div id=\"chapText\">\n" +
            "%s\n" +
            "  </div>\n" +
            "</body>\n" +
            "</html>";

    /**
     * The first portion of a title page. Has places to substitute in the title and author.
     */
    public static final String TITLE_PAGE_START = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
            "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\"\n" +
            "  \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">\n" +
            "\n" +
            "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
            "<head>\n" +
            "  <link href=\"../Styles/style.css\" rel=\"stylesheet\" type=\"text/css\" />\n" +
            "  <meta content=\"http://www.w3.org/1999/xhtml; charset=UTF-8\" http-equiv=\"Content-Type\" />\n" +
            "  <title> </title>\n" +
            "</head>\n" +
            "\n" +
            "<body>\n" +
            "<center>\n" +
            "  <div id=\"ficTitle\" style=\"font-size: 200%%;\">\n" +
            "    %s\n" +
            "  </div>\n" +
            "\n" +
            "  <div id=\"ficAuthor\" style=\"font-size: 150%%;\">\n" +
            "    By: %s\n" +
            "  </div>\n" +
            "</center>\n";

    /**
     * A part of the title page. Has a place for the name and value of a string detail.
     */
    public static final String TITLE_PAGE_S_PART =
            "\n<p style=\"margin: 0.25em 0em;\"><strong><u>%s</u>:</strong> %s</p>\n";

    /**
     * A part of the title page. Has a place for the name and value of a numeric detail.
     */
    public static final String TITLE_PAGE_D_PART =
            "\n<p style=\"margin: 0.25em 0em;\"><strong><u>%s</u>:</strong> %,d</p>\n";

    /**
     * The end part of the title page.
     */
    public static final String TITLE_PAGE_END = "\n</body>\n</html>";
}
