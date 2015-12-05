package bkromhout.FictionDL;

/**
 * Constants file.
 */
public class C {
    /*
    General constants.
     */
    public static final String VER = "1.1";
    public static final String VER_STRING = "FictionDL, Version " + VER;

    /*
    Log style tags. Putting these anywhere within a log string will cause the string to be formatted accordingly if
    it's printed to the TextFlow log in the GUI. The tags are stripped before the string is printed. One color and
    multiple styles can apply, and apply to the *whole* string.
     */
    public static final String LOG_RED = "!red!";
    public static final String LOG_BLUE = "!blue!";
    public static final String LOG_GREEN = "!green!";

    /*
    GUI Constants.
     */
    public static final double G_MIN_WIDTH = 525.0;
    public static final double G_MIN_HEIGHT = 300.0;
    public static final String F_CHOOSE_TITLE = "Choose Input File:";
    public static final String D_CHOOSE_TITLE = "Choose Output Directory:";

    /*
    Preference Keys.
     */
    public static final String KEY_IN_FILE_PATH = "key_in_file_path";
    public static final String KEY_OUT_DIR_PATH = "key_out_dir_path";

    /*
    Log output strings.
     */
    // General
    public static final String PARSE_FILE = "Parsing file...";

    // Download Process
    public static final String STARTING_SITE_DL_PROCESS = "\nStarting %s download process...\n" + LOG_BLUE;
    public static final String FETCH_BUILD_MODELS = "Fetching story info from %s and building story models...\n";
    public static final String DL_STORIES_FROM_SITE = "Downloading stories from %s...\n\n";
    public static final String SIYE_SLOW = "SIYE's process is *very slow* due to their horrid HTML structure, please " +
            "be patient ;)";
    public static final String DONE = "Done!" + LOG_GREEN;
    public static final String FINISHED_WITH_SITE = "Finished with %s.\n\n" + LOG_BLUE;
    public static final String ALL_FINISHED = "\nAll Finished! :)" + LOG_GREEN;

    // Stories
    public static final String SAVING_STORY = "Saving Story...";
    public static final String FH_STORY_ON_FFN = "\"%s\" is still available on FanFiction.net; will download from " +
            "there.\n\n";

    // Chapters
    public static final String DL_CHAPS_FOR = "Downloading: \"%s\"\n";
    public static final String SANITIZING_CHAPS = "Sanitizing chapters...";

    // Errors and Warnings
    public static final String INVALID_URL = "Invalid URL: \"%s\"\n." + LOG_RED;
    public static final String INVALID_PATH = "Invalid path: \"%s\"\n." + LOG_RED;
    public static final String PROCESS_LINE_FAILED = "Couldn't process this line from the file: \"%s\"\n" + LOG_RED;
    public static final String HTML_DL_FAILED = "Failed to download HTML from: \"%s\"\n" + LOG_RED;
    public static final String STORY_DL_FAILED = "Couldn't get %s story with ID=%s. Skipping it." + LOG_RED;
    public static final String SOME_CHAPS_FAILED = "Skipping this story; some chapters failed to download!\n" + LOG_RED;
    public static final String SAVE_FILE_FAILED = "Failed to save file: %s\n" + LOG_RED;
    public static final String FH_FFN_CHECK_FAILED = "Failed to check if story is still active on FFN. Oh well, " +
            "never hurts to try!" + LOG_RED;
    public static final String MUST_LOGIN = "You need to provide login info for %s to download story with ID=%s!" +
            LOG_RED;
    public static final String LOGIN_FAILED = "Couldn't log in to %s. Check your login info." + LOG_RED;

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

    /*
    Link strings. Some need parts substituted into them.
     */
    /**
     * Used to search FictionHunt for a story in order to get its summary. Just need to substitute in the title.
     */
    public static final String FH_SEARCH_URL = "http://fictionhunt.com/5/0/0/0/0/0/0/0/0/0/0/%s/1";

    /**
     * FictionHunt story chapter link, just needs the story ID and chapter number substituted into it.
     */
    public static final String FH_C_URL = "http://fictionhunt.com/read/%s/%d";

    /**
     * FFN story link, just needs the story ID string substituted into it.
     */
    public static final String FFN_S_URL = "https://www.fanfiction.net/s/%s/1";

    /**
     * FFN story chapter link, just needs the story ID string and chapter number substituted into it.
     */
    public static final String FFN_C_URL = "https://www.fanfiction.net/s/%s/%d";

    /**
     * SIYE author page link, just needs relative author link string substituted into it.
     */
    public static final String SIYE_A_URL = "http://siye.co.uk/%s";

    /**
     * SIYE story chapter link, just needs the story ID string and chapter number substituted into it.
     */
    public static final String SIYE_C_URL = "http://siye.co.uk/viewstory.php?sid=%s&chapter=%d";

    /**
     * MuggleNet story info link, just needs story ID substituted into it.
     */
    public static final String MN_S_URL = "http://fanfiction.mugglenet.com/viewstory.php?sid=%s&warning=5";

    /**
     * MuggleNet chapter link, needs story ID and chapter number substituted into it.
     */
    public static final String MN_C_URL = "http://fanfiction.mugglenet.com/viewstory.php?sid=%s&chapter=%d&warning=5";

    /**
     * MuggleNet login page link.
     */
    public static final String MN_L_URL = "http://fanfiction.mugglenet.com/user.php?action=login";

    /*
    Site/Comparison strings, usually used to try and determine what type of error is present when the HTML structure
    by itself doesn't provide enough info.
     */
    /**
     * Indicates a URL is malformed.
     */
    public static final String BAD_URL = "BAD_URL";

    /**
     * Error message displayed by MuggleNet when attempting to access a "Professors" rated story while not logged in.
     */
    public static final String MN_REG_USERS_ONLY = "Registered Users Only";

    /*
    RegEx Strings
     */
    /**
     * Regex to obtain website host from URL. Group 2 is the host.
     */
    public static final String HOST_REGEX = "^(http[s]?:\\/\\/)?([^:\\/\\s]+)(\\/.*)?$";

    /**
     * Regex to extract storyId from FictionHunt URL. Use .find() then .group(1).
     */
    public static final String FH_SID_REGEX = "\\/read\\/(\\d*)";

    /**
     * Regex to extract the story ID. Use .find() then .group(1).
     */
    public static final String SIYE_SID_REGEX = "sid=(\\d*)";

    /**
     * Regex to extract SIYE chapter title without the leading "#. " part. Group 2 is the chapter title.
     */
    public static final String SIYE_CHAP_TITLE_REGEX = "(\\d+\\.\\s)(.*)";

    /**
     * Regex to extract story ID from FanFiction.net URL. Use .find() then .group(1).
     */
    public static final String FFN_SID_REGEX = "\\/s\\/(\\d*)";

    /**
     * Regex that matches a FanFiction.net author link of the format "/u/[whatever]"
     */
    public static final String FFN_AUTHOR_LINK_REGEX = "\\/u\\/.*";

    /**
     * Regex to extract FFN chapter title without the leading "#. " part. Group 2 is the chapter title.
     */
    public static final String FFN_CHAP_TITLE_REGEX = SIYE_CHAP_TITLE_REGEX;

    /**
     * Regex to determine if a string contains a valid FFN genre. If .find() returns true, it does.
     */
    public static final String FFN_GENRE_REGEX = "\\QAdventure\\E|\\QAngst\\E|\\QCrime\\E|\\QDrama\\E|\\QFamily\\E" +
            "|\\QFantasy\\E|\\QFriendship\\E|\\QGeneral\\E|\\QHorror\\E|\\QHumor\\E|\\QHurt/Comfort\\E|\\QMystery\\E" +
            "|\\QParody\\E|\\QPoetry\\E|\\QRomance\\E|\\QSci-Fi\\E|\\QSpiritual\\E|\\QSupernatural\\E|\\QSuspense\\E" +
            "|\\QTragedy\\E|\\QWestern\\E";

    /**
     * Regex to extract MuggleNet story ID from a MuggleNet URL. Use .find() then .group(1).
     */
    public static final String MN_SID_REGEX = SIYE_SID_REGEX;

    /**
     * Regex to extract MuggleNet chapter title without the leading "#. " part. Group 2 is the chapter title.
     */
    public static final String MN_CHAP_TITLE_REGEX = SIYE_CHAP_TITLE_REGEX;

    /**
     * Regex to find all ampersands in a piece of HTML which are actual ampersands and not part of a character code.
     */
    public static final String AMP_REGEX = "[\\&](?!(#|amp;|gt;|lt;|quot;|nbsp;))";

    /**
     * Regex to find all unclosed tags, where the tag type is substituted in, no matter their attributes. Used with
     * TAG_REGEX_REPL.
     */
    public static final String TAG_REGEX_FIND = "(\\<%s[^>]*?(?<!\\/))(\\>)";

    /**
     * Used with TAG_REGEX_FIND to replace all > with /> for unclosed tags.
     */
    public static final String TAG_REGEX_REPL = "$1/>";

    /*
    Title Page Part Names (AKA, details for a fic)
     */
    public static final String SUMMARY = "Summary";
    public static final String SERIES = "Series";
    public static final String FIC_TYPE = "Fic Type";
    public static final String WARNINGS = "Warnings";
    public static final String RATING = "Rated";
    public static final String GENRES = "Genres";
    public static final String CHARACTERS = "Characters";
    public static final String WORD_COUNT = "Word Count";
    public static final String CHAP_COUNT = "Chapter Count";
    public static final String DATE_PUBL = "Date Published";
    public static final String DATE_LUPD = "Date Last Updated";
    public static final String STATUS = "Status";

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

    /**
     * For FictionHunt stories, used in place of a summary if we can't find the story on the first page of search
     * results (which is what we do to try and get summaries from FictionHunt).
     */
    public static final String FH_NO_SUMMARY = "(Couldn't get summary from FictionHunt, sorry!)";

    /*
    File Template Strings
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
