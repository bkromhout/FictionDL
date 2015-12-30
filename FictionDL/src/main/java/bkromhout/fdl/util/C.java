package bkromhout.fdl.util;

import bkromhout.fdl.Main;
import com.google.common.eventbus.EventBus;
import com.squareup.okhttp.OkHttpClient;

/**
 * Constants.
 * <p>
 * <b>Note on String constants:</b> All human-readable strings should be here. Other than that, only strings which are
 * used by multiple classes (or potentially could be), which are private to this class, or which are exceedingly long
 * should be put here. Strings that do not fall under these requirements should be in a more context-specific class.
 */
public abstract class C {
    /**
     * Program version string.
     */
    public static final String VER_STRING = "FictionDL, Version 3.1.1";


    /* Global accessors. These are here purely for the convenience of typing "C" rather than a longer class name. */

    /**
     * Provide access to the OkHttpClient.
     * @return The OkHttpClient from {@link Main}.
     */
    public static OkHttpClient getHttpClient() {
        return Main.httpClient;
    }

    /**
     * Provide access to the event bus.
     * @return Event bus from {@link Main}.
     */
    public static EventBus getEventBus() {
        return Main.eventBus;
    }

    /*
    Log style tags.
    Putting these anywhere within a log string will cause the string to be formatted accordingly if
    it's printed to the TextFlow log in the GUI. The tags are stripped before the string is printed. One color and
    multiple styles can apply, and apply to the *whole* string.
     */
    static final String LOG_ULINE = "!underline!";
    static final String LOG_BLUE = "!blue!";
    static final String LOG_GREEN = "!green!";
    public static final String LOG_LOUD = "!purple!"; // Verbose only. Purple
    static final String LOG_WARN = "!yellow!"; // Warnings only. Yellow.
    static final String LOG_ERR = "!red!"; // Errors only. Red.

    /*
    Program argument map keys.
     */
    public static final String ARG_IN_PATH = "arg_in_path";
    public static final String ARG_OUT_PATH = "arg_out_path";
    public static final String ARG_CFG_PATH = "arg_cfg_path";

    /*
    Log strings.
     */
    // General.
    public static final String DONE = "Done!" + LOG_GREEN;

    public static final String PARSING_FILE = "Parsing %s file...";

    public static final String SANITIZING_CHAPS = "Sanitizing chapters...";

    public static final String SAVING_STORY = "Saving Story...";

    public static final String ALL_FINISHED = "\nAll Finished! :)" + LOG_GREEN;

    public static final String RUN_RESULTS = "This run generated %f total units of work.\n" + LOG_LOUD;

    // Site Story Process
    public static final String STARTING_SITE_DL_PROCESS = "\nStarting %s download process...\n" + LOG_BLUE;

    public static final String STARTING_SITE_AUTH_PROCESS = "\nAttempting to log in to %s..." + LOG_BLUE;

    public static final String FETCH_BUILD_MODELS = "Fetching all stories' details from %s to build story models...\n";

    public static final String DL_STORIES_FROM_SITE = "Downloading stories from %s...\n\n";

    public static final String DL_CONTENT_FOR = "Downloading: \"%s\"\n"; // For downloaders which extend ParsingDL.

    public static final String DL_EPUB_FOR = "Downloading ePUB for: \"%s\"..."; // For downloaders which extend EpubDL.

    public static final String FINISHED_WITH_SITE = "Finished with %s.\n\n" + LOG_BLUE;

    // Local Story Process
    public static final String STARTING_LOCAL_STORY_PROCESS = "\nStarting local story creation process..." + LOG_BLUE;

    public static final String VALIDATING_LOCAL_STORY_DIRS = "Validating local story folders...";

    public static final String CREATING_LOCAL_STORIES = "Creating local stories...\n";

    public static final String CHECKING_LOCAL_STORY = "Checking story in: \"%s\"\n" + LOG_LOUD;

    public static final String PROCESSING_LOCAL_STORY = "Processing: \"%s\"\n";

    public static final String READING_CHAP_FILE = "Reading chapter file: \"%s\"\n" + LOG_LOUD;

    public static final String FINISHED_WITH_LOCAL_STORIES = "Finished with local stories.\n" + LOG_BLUE;

    // FictionHunt-specific.
    public static final String FH_ON_FFN = "\"%s\" is still available on FanFiction.net; will download from there.\n\n";

    public static final String FH_FFN_CHECK_FAILED = "Failed to check if story is still active on FFN. Oh well, " +
            "never hurts to try!" + LOG_WARN;

    // SIYE-specific.
    public static final String SIYE_PRE_DL = "SIYE's process is *very slow* due to their horrid HTML structure, " +
            "please be patient ;)";

    // Ao3-specific.
    public static final String AO3_PRE_DL = "Ao3 stories occasionally fail to download, just try them again.";

    /*
    Warning and Error log strings. (Site-specific warnings and errors may be above)
    Warning strings should have LOG_WARN appended, and error strings should have LOG_ERR appended.
     */
    // Common suffixes for when we're going to skip a story.
    private static final String LOG_ERR_SKIP = " Skipping it." + LOG_ERR;
    private static final String LOG_ERR_SKIPN = LOG_ERR_SKIP + "\n";

    // General.
    public static final String INVALID_PATH = "Invalid path: \"%s\".\n" + LOG_ERR;

    public static final String INVALID_URL = "Invalid URL: \"%s\".\n" + LOG_ERR;

    // Parsing.
    public static final String PROCESS_LINE_FAILED = "Couldn't process this line from %s file: \"%s\".\n" + LOG_WARN;

    public static final String PARSE_HTML_FAILED = "Couldn't parse HTML for \"%s\"." + LOG_WARN;

    // General Network.
    static final String HTML_DL_FAILED = "Failed to download HTML from: \"%s\".\n" + LOG_WARN;

    public static final String SAVE_FILE_FAILED = "Failed to save file: \"%s\".\n" + LOG_ERR;

    // Auth.
    public static final String MUST_LOGIN = "You must provide %s login info to download story with ID=%s!" + LOG_WARN;

    public static final String LOGIN_FAILED = "\nCouldn't log in to %s. Check your login info.\n" + LOG_ERR;

    // Site Story Process.
    public static final String STORY_DL_FAILED = "Couldn't get %s story with ID=%s." + LOG_ERR_SKIPN;

    public static final String NO_ID_STORY_DL_FAILED = "Couldn't get %s story from \"%s\"." + LOG_ERR_SKIPN;

    public static final String PARTIAL_DL_FAIL = "Skipping this story; some chapters failed to download!\n" + LOG_ERR;

    public static final String NO_EPUB_ON_SITE = "Couldn't find ePUB on %s for story \"%s\"." + LOG_ERR_SKIP;

    // Local Story Process.
    private static final String LS_PRE_DIR = "The local story in folder \"%s\" "; // Common local story error prefix.
    private static final String LS_PRE_TITLE = "The local story \"%s\" "; // Common local story error prefix.

    public static final String INVALID_STORY_DIR = "\"%s\" is not a valid story folder.\n" + LOG_ERR;

    public static final String NO_STORYINFO_JSON = LS_PRE_DIR + "doesn't have a storyinfo.json file." + LOG_ERR_SKIPN;

    public static final String MALFORMED_STORYINFO_JSON = LS_PRE_DIR + "has a malformed storyinfo.json file." +
            LOG_ERR_SKIPN;

    public static final String JSON_NO_ELEM = LS_PRE_DIR + "doesn't have a valid \"%s\" element." + LOG_ERR_SKIPN;

    public static final String JSON_NO_ELEM_TITLE = JSON_NO_ELEM.replace(LS_PRE_DIR, LS_PRE_TITLE);

    public static final String JSON_BAD_ELEM_TITLE = LS_PRE_TITLE + "has a malformed \"%s\" element, skipping the " +
            "element.\n" + LOG_WARN;

    public static final String NO_CHAP_FILES = LS_PRE_TITLE + "has no chapter files." + LOG_ERR_SKIPN;

    public static final String MISSING_CHAP_FILE = LS_PRE_TITLE + "is missing \"%d.html\".\n" + LOG_ERR;

    public static final String MALFORMED_CHAP_FILE = LS_PRE_TITLE + "has a malformed \"%d.html\" file.\n" + LOG_ERR;

    public static final String PARTIAL_READ_FAIL = "Skipping this story; some chapter files couldn't be read.\n" +
            LOG_ERR;

    /*
    Error strings used by exceptions which we don't catch. Don't include log tags, they won't be stripped!
     */
    // Technically we print this one, but it's used before the could be started anyway.
    public static final String INVALID_ARGS = "Bad arguments.";

    public static final String NO_INPUT_PATH = "You must supply an input file path!";

    public static final String HTML_UNEXP_RESP = "Unexpected result when trying to download HTML from \"%s\".\n";

    public static final String CHAP_NUM_NOT_ASSIGNED = "Chapter number hasn't been assigned yet!";

    static final String STALE_UNIT_WORTH = "Unit worth wasn't recalculated prior to use!";

    /*
    Default/specific story detail values.
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
    storyinfo.json element names and expected values.
    These are *technically* human-readable, since users are in charge of creating storyinfo.json files. Also, some
    are used by multiple classes in the localfic package.
     */
    // Top-level element names.
    public static final String J_META = "meta";
    public static final String J_INFO = "info";
    public static final String J_CHAPTER_TITLES = "chapterTitles";

    // "meta" object element names.
    public static final String J_TYPE = "type";
    public static final String J_VERSION = "version";

    // "meta" -> "type" element expected values.
    public static final String J_TYPE_LS = "localstory";

    // "info" object elements names.
    public static final String J_TITLE = "title";
    public static final String J_AUTHOR = "author";
    public static final String J_URL = "url";
    public static final String J_SUMMARY = "summary";
    public static final String J_SERIES = "series";
    public static final String J_FIC_TYPE = "ficType";
    public static final String J_WARNINGS = "warnings";
    public static final String J_RATING = "rating";
    public static final String J_GENRES = "genres";
    public static final String J_CHARACTERS = "characters";
    public static final String J_WORD_COUNT = "wordCount";
    public static final String J_DATE_PUBLISHED = "datePublished";
    public static final String J_DATE_UPDATED = "dateUpdated";
    public static final String J_STATUS = "status";

    /*
    File Template Strings.
    These are long and would be annoying to have in the EpubCreator class.
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
