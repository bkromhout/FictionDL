package bkromhout.FictionDL;

/**
 * Constants file.
 */
public class C {
    /*
    General Strings.
     */
    public static final String VER = "1.0";
    public static final String VER_STRING = "FictionDL Version " + VER;
    public static final String USAGE = "Usage: java -jar FictionDL.jar \"<path to URL list txt file>\"";

    /*
    Log output strings.
     */
    // General
    public static final String CHECK_AND_PARSE_FILE = "Checking and parsing file...";
    public static final String FH_NO_SUMMARY = "(Couldn't get summary from FictionHunt, sorry!)";

    // Download Process
    public static final String STARTING_SITE_DL_PROCESS = "\nStarting %s download process...\n";
    public static final String FETCH_BUILD_MODELS = "Fetching story info from %s and building story models...\n";
    public static final String DL_STORIES_FROM_SITE = "Downloading stories from %s...\n\n";
    public static final String DONE = "Done!";
    public static final String FINISHED_WITH_SITE = "Finished with %s.\n\n";
    public static final String ALL_FINISHED = "\nAll Finished! :)";

    // Stories
    public static final String DL_SAVE_EPUB_FOR_STORY = "Downloading and saving ePUB file for: \"%s\"...";
    public static final String SAVING_STORY = "Saving Story...";
    public static final String FH_STORY_ON_FFN = "\"%s\" is still available on Fanfiction.net; will download from " +
            "there.\n";

    // Chapters
    public static final String DL_CHAPS_FOR = "Downloading: \"%s\"\n";
    public static final String SANITIZING_CHAPS = "Sanitizing chapters...";

    // Errors and Warnings
    public static final String INVALID_PATH = "Invalid path.";
    public static final String INVALID_URL = "Invalid URL.";
    public static final String PROCESS_LINE_FAILED = "Couldn't process this line from the file: \"%s\"\n";
    public static final String HTML_DL_FAILED = "Failed to download HTML from: \"%s\"\n";
    public static final String STORY_DL_FAILED = "Couldn't get %s story with ID=%s. Skipping it.";
    public static final String SOME_CHAPS_FAILED = "Skipping this story; some chapters failed to download!\n";
    public static final String SAVE_FILE_FAILED = "Failed to save file: %s\n";
    public static final String CREATE_DIR_FAILED = "Couldn't create dir to save files at \"%s\"\n";
    public static final String NO_URLS_FFNDL = "No URLs were passed to this FanfictionNetDL when it was created. " +
            "Perhaps another downloader is using it?";
    public static final String FH_FFN_CHECK_FAILED = "Failed to check if story is still active on FFN. Oh well, " +
            "never hurts to try!";

    /*
    Link template strings.
     */
    /**
     * Used to search FictionHunt for a story in order to get its summary. Just need to substitute in the title.
     */
    public static final String FH_SEARCH_URL = "http://fictionhunt.com/5/0/0/0/0/0/0/0/0/0/0/%s/1";

    /**
     * Fanfiction.net story link, just needs the story ID string substituted into it. We use the mobile site because
     * it's easier to parse.
     */
    public static final String FFN_URL = "https://m.fanfiction.net/s/%s";

    /**
     * p0ody-files.com download link, just needs the FFN story ID substituted into it.
     */
    public static final String PF_DL_URL = "http://www.p0ody-files.com/ff_to_ebook/mobile/makeEpub.php?id=%s";

    /**
     * SIYE first chapter link, used as an entry point, just needs the story ID string substituted into it.
     */
    public static final String SIYE_INFO_URL = "http://siye.co.uk/viewstory.php?sid=%s&chapter=1";

    /**
     * SIYE author page link, just needs relative author link string substituted into it.
     */
    public static final String SIYE_AUTHOR_URL = "http://siye.co.uk/%s";

    /**
     * SIYE story chapter link, just needs the story ID string and chapter number substituted into it. This will point
     * to the printable version of some chapter of a story.
     */
    public static final String SIYE_CHAP_URL = "http://siye.co.uk/viewstory.php?action=printable&sid=%s&chapter=%d";

    /*
    RegEx Strings
     */
    /**
     * Regex to obtain website host from URL. Group 2 is the host.
     */
    public static final String HOST_REGEX = "^(http[s]?:\\/\\/)?([^:\\/\\s]+)(\\/.*)?$";

    /**
     * Regex to help obtain file name from a Content-Disposition header. Use .find() then .group();
     */
    public static final String PF_FNAME_REGEX = "(?<=filename=\").*?(?=\")";

    /**
     * Regex to extract storyId from FictionHunt URL. Use .find() then .group(1).
     */
    public static final String FICTIONHUNT_REGEX = "\\/read\\/(\\d*)";

    /**
     * Regex to extract story ID from Fanfiction.net URL. Use .find() then .group(1).
     */
    public static final String FFN_REGEX = "\\/s\\/(\\d*)";

    /**
     * Regex to extract the story ID. Use .find() then .group(1).
     */
    public static final String SIYE_SID_REGEX = "sid=(\\d*)";

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
     * Title page with summary. Has a number of areas for replacement using String.format(): Title of story, Author of
     * story, Summary, Rating, Word count, Chapter count.
     */
    public static final String TITLE_PAGE_SUMMARY = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
            "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\"\n" +
            "  \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">\n" +
            "\n" +
            "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
            "<head>\n" +
            "  <link href=\"../Styles/style.css\" rel=\"stylesheet\" type=\"text/css\"/>\n" +
            "  <meta content=\"http://www.w3.org/1999/xhtml; charset=UTF-8\" http-equiv=\"Content-Type\"/>\n" +
            "  <title> </title>\n" +
            "</head>\n" +
            "\n" +
            "<body>\n" +
            "  <div id=\"ficTitle\">\n" +
            "    %s\n" +
            "  </div>\n" +
            "\n" +
            "  <div id=\"ficAuthor\">\n" +
            "    By: %s\n" +
            "  </div>\n" +
            "\n" +
            "  <p><strong>Summary:</strong> %s</p>\n" +
            "\n" +
            "  <p><strong>Rated:</strong> %s</p>\n" +
            "\n" +
            "  <p><strong>Word Count:</strong> %d</p>\n" +
            "\n" +
            "  <p><strong>Chapters:</strong> %d</p>\n" +
            "\n" +
            "</body>\n" +
            "</html>";

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
            "  <link href=\"../Styles/style.css\" rel=\"stylesheet\" type=\"text/css\"/>\n" +
            "  <meta content=\"http://www.w3.org/1999/xhtml; charset=utf-8\" http-equiv=\"Content-Type\"/>\n" +
            "  <title>%s</title>\n" +
            "</head>\n" +
            "\n" +
            "<body>\n" +
            "  <h1 id=\"chapTitle\">%s</h1>\n" +
            "\n" +
            "  <div id=\"chapText\">\n" +
            "%s\n" +
            "</div>\n" +
            "</body>\n" +
            "</html>";
}
