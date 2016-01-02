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
    public static final String VER_STRING = "FictionDL, Version 4.0.0";

    /**
     * System-specific line separator.
     */
    public static final String N = System.lineSeparator();
    private static final String NN = N + N;


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

    public static final String ALL_FINISHED = N + "All Finished! :)" + LOG_GREEN;

    public static final String RUN_RESULTS = "This run generated %f total units of work." + N + LOG_LOUD;

    // Site Story Process
    public static final String STARTING_SITE_DL_PROCESS = N + "Starting %s download process..." + N + LOG_BLUE;

    public static final String STARTING_SITE_AUTH_PROCESS = N + "Attempting to log in to %s..." + LOG_BLUE;

    public static final String FETCH_BUILD_MODELS =
            "Fetching all stories' details from %s to build story models..." + N;

    public static final String DL_STORIES_FROM_SITE = "Downloading stories from %s..." + NN;

    public static final String DL_CONTENT_FOR = "Downloading: \"%s\"" + N; // For downloaders which extend ParsingDL.

    public static final String DL_EPUB_FOR = "Downloading ePUB for: \"%s\"..."; // For downloaders which extend EpubDL.

    public static final String FINISHED_WITH_SITE = "Finished with %s." + NN + LOG_BLUE;

    // Local Story Process
    public static final String STARTING_LOCAL_STORY_PROCESS = N + "Starting local story creation process..." + LOG_BLUE;

    public static final String VALIDATING_LOCAL_STORY_DIRS = "Validating local story folders...";

    public static final String CREATING_LOCAL_STORIES = "Creating local stories..." + N;

    public static final String CHECKING_LOCAL_STORY = "Checking story in: \"%s\"" + N + LOG_LOUD;

    public static final String PROCESSING_LOCAL_STORY = "Processing: \"%s\"" + N;

    public static final String READING_CHAP_FILE = "Reading chapter file: \"%s\"" + N + LOG_LOUD;

    public static final String FINISHED_WITH_LOCAL_STORIES = "Finished with local stories." + N + LOG_BLUE;

    // Ao3-specific.
    public static final String AO3_PRE_DL = "Ao3 stories occasionally fail to download, just try them again.";

    // FictionHunt-specific.
    public static final String FH_ON_FFN =
            "\"%s\" is still available on FanFiction.net; will download from there." + NN;

    public static final String FH_FFN_CHECK_FAILED = "Failed to check if story is still active on FFN. Oh well, " +
            "never hurts to try!" + LOG_WARN;

    // SIYE-specific.
    public static final String SIYE_PRE_DL = "SIYE's process is *very slow* due to their horrid HTML structure, " +
            "please be patient ;)";

    /*
    Warning and Error log strings. (Site-specific warnings and errors may be above)
    Warning strings should have LOG_WARN appended, and error strings should have LOG_ERR appended.
     */
    // Common suffixes for when we're going to skip a story.
    private static final String LOG_ERR_SKIP = " Skipping it." + LOG_ERR;
    private static final String LOG_ERR_SKIPN = LOG_ERR_SKIP + N;

    // General.
    public static final String INVALID_PATH = "Invalid path: \"%s\"." + N + LOG_ERR;

    public static final String INVALID_URL = "Invalid URL: \"%s\"." + LOG_ERR;

    // Parsing.
    public static final String PROCESS_LINE_FAILED = "Couldn't process this line from %s file: \"%s\"." + N + LOG_WARN;

    public static final String PARSE_HTML_FAILED = "Couldn't parse HTML for \"%s\"." + LOG_WARN;

    // General Network.
    static final String HTML_DL_FAILED = "Failed to download HTML from: \"%s\"." + N + LOG_WARN;

    public static final String SAVE_FILE_FAILED = "Failed to save file: \"%s\"." + N + LOG_ERR;

    // Auth.
    public static final String LOGIN_FAILED = N + "Couldn't log in to %s. Check your login info." + N + LOG_ERR;

    // Site Story Process.
    public static final String STORY_DL_FAILED = "Couldn't get %s story with ID=%s." + LOG_ERR_SKIP;

    public static final String NO_ID_STORY_DL_FAILED = "Couldn't get %s story from \"%s\"." + LOG_ERR_SKIP;

    public static final String NO_EPUB_ON_SITE = "Couldn't find ePUB on %s for story \"%s\"." + LOG_ERR_SKIP;

    public static final String MUST_LOGIN = "You must provide %s login info to download \"%s\"." + LOG_WARN;

    public static final String UNEXP_SITE_ERR = "Unexpected %s site error: \"%s\", skipping this story." + LOG_ERR;

    public static final String UNEXP_STORY_ERR = "Unexpected exception while trying to make a story model for \"%s\":" +
            N + "%s" + LOG_ERR + N;

    public static final String PARTIAL_DL_FAIL = "Skipping this story, some chapters failed to download!" + N + LOG_ERR;

    // Local Story Process.
    private static final String LS_PRE_DIR = "The local story in folder \"%s\" "; // Common local story error prefix.
    private static final String LS_PRE_TITLE = "The local story \"%s\" "; // Common local story error prefix.

    public static final String INVALID_STORY_DIR = "\"%s\" is not a valid story folder." + N + LOG_ERR;

    public static final String NO_STORYINFO_JSON = LS_PRE_DIR + "doesn't have a storyinfo.json file." + LOG_ERR_SKIPN;

    public static final String MALFORMED_STORYINFO_JSON = LS_PRE_DIR + "has a malformed storyinfo.json file." +
            LOG_ERR_SKIPN;

    public static final String JSON_NO_ELEM = LS_PRE_DIR + "doesn't have a valid \"%s\" element." + LOG_ERR_SKIPN;

    public static final String JSON_NO_ELEM_TITLE = JSON_NO_ELEM.replace(LS_PRE_DIR, LS_PRE_TITLE);

    public static final String JSON_BAD_ELEM_TITLE = LS_PRE_TITLE + "has a malformed \"%s\" element, skipping the " +
            "element." + N + LOG_WARN;

    public static final String NO_CHAP_FILES = LS_PRE_TITLE + "has no chapter files." + LOG_ERR_SKIP;

    public static final String MISSING_CHAP_FILE = LS_PRE_TITLE + "is missing \"%d.html\"." + N + LOG_ERR;

    public static final String MALFORMED_CHAP_FILE = LS_PRE_TITLE + "has a malformed \"%d.html\" file." + N + LOG_ERR;

    public static final String PARTIAL_READ_FAIL = "Skipping this story, some chapter files couldn't be read." +
            LOG_ERR;

    // EPub Creation Process.
    public static final String NOT_A_URL = "\"%s\" is not a valid URL, excluding from the title page." + N + LOG_WARN;

    /*
    Error strings used by exceptions which we don't catch. Don't include log tags, they won't be stripped!
     */
    // Technically we print this one, but it's used before the could be started anyway.
    public static final String INVALID_ARGS = "Bad arguments.";

    public static final String NO_INPUT_PATH = "You must supply an input file path!";

    public static final String UNEXP_HTML_RESP = "Unexpected result when trying to download HTML from \"%s\"." + N;

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
    public static final String CHAPTER_PAGE = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" + N +
            "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\"" + N +
            "  \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">" + N +
            N +
            "<html xmlns=\"http://www.w3.org/1999/xhtml\">" + N +
            "<head>" + N +
            "  <link href=\"../Styles/style.css\" rel=\"stylesheet\" type=\"text/css\" />" + N +
            "  <meta content=\"http://www.w3.org/1999/xhtml; charset=utf-8\" http-equiv=\"Content-Type\" />" + N +
            "  <title>%s</title>" + N +
            "</head>" + N +
            N +
            "<body>" + N +
            "  <h1 id=\"chapTitle\">%s</h1>" + N +
            N +
            "  <div id=\"chapText\">" + N +
            "%s" + N +
            "  </div>" + N +
            "</body>" + N +
            "</html>";

    /**
     * The first portion of a title page. Has places to substitute in the title and author.
     */
    public static final String TITLE_PAGE_START = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" + N +
            "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\"" + N +
            "  \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">" + N +
            N +
            "<html xmlns=\"http://www.w3.org/1999/xhtml\">" + N +
            "<head>" + N +
            "  <link href=\"../Styles/style.css\" rel=\"stylesheet\" type=\"text/css\" />" + N +
            "  <meta content=\"http://www.w3.org/1999/xhtml; charset=UTF-8\" http-equiv=\"Content-Type\" />" + N +
            "  <title> </title>" + N +
            "</head>" + N +
            N +
            "<body>" + N +
            "<center>" + N +
            "  <div id=\"ficTitle\" style=\"font-size: 200%%;\">" + N +
            "    %s" + N +
            "  </div>" + N +
            N +
            "  <div id=\"ficAuthor\" style=\"font-size: 150%%;\">" + N +
            "    By: %s" + N +
            "  </div>" + N +
            "</center>" + N;

    /**
     * A part of the title page. Has a place for the name and value of a string detail.
     */
    public static final String TITLE_PAGE_S_PART =
            N + "<p style=\"margin: 0.25em 0em;\"><strong><u>%s</u>:</strong> %s</p>" + N;

    /**
     * A part of the title page. Has a place for the name and value of a numeric detail.
     */
    public static final String TITLE_PAGE_D_PART =
            N + "<p style=\"margin: 0.25em 0em;\"><strong><u>%s</u>:</strong> %,d</p>" + N;

    /**
     * The end part of the title page.
     */
    public static final String TITLE_PAGE_END = N + "</body>" + N + "</html>";
}
