package bkromhout;

/**
 * Constants
 */
public class C {
    /*
    Log output strings.
     */
    // General
    public static final String USAGE = "Usage: java -jar FictionHuntDL.jar <path to URL list txt file>";
    public static final String CHECK_AND_PARSE_FILE = "Checking and parsing file...";
    public static final String PROCESSED_FILE = "Processed file.\n";

    // Download Process
    public static final String STARTING_SITE_DL_PROCESS = "Starting %s download process...\n";
    public static final String DONE = "Done!\n";
    public static final String FINISHED_WITH_SITE = "Finished with %s.\n";
    public static final String ALL_FINISHED = "All Finished! :)";

    // Stories
    public static final String FETCH_BUILD_MODELS = "Fetching stories from %s and building story models...\n";
    public static final String DL_STORIES_FROM_SITE = "Downloading stories from %s...\n";
    public static final String DL_SAVE_EPUB_FOR_STORY = "Downloading and saving ePUB file for: \"%s\"\n";
    public static final String SAVING_STORY = "Saving Story...";
    public static final String FH_STORY_ON_FFN = "Story still available on Fanfiction.net; will download from there.";

    // Chapters
    public static final String DL_CHAPS_FOR = "Downloading chapters for: \"%s\"\n";
    public static final String SANITIZING_CHAPS = "Sanitizing chapters...";

    // Errors and Warnings
    public static final String INVALID_PATH = "Invalid path.";
    public static final String INVALID_URL = "Invalid URL.";
    public static final String PROCESS_LINE_FAILED = "Couldn't process this line from the file: \"%s\"\n";
    public static final String HTML_DL_FAILED = "Failed to download HTML from: \"%s\"\n";
    public static final String ENTRY_PT_DL_FAILED = "Couldn't download story entry point!";
    public static final String FFN_DL_FAILED = "Couldn't download the Fanfiction.net story with ID=%s. Skipping it.\n";
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
     * Fanfiction.net story link, just needs the story ID string substituted into it. We use the mobile site because
     * it's easier to parse.
     */
    public static final String FFN_LINK = "https://m.fanfiction.net/s/%s";

    /**
     * p0ody-files.com download link, just needs the FFN story ID substituted into it.
     */
    public static final String PF_DL_LINK = "http://www.p0ody-files.com/ff_to_ebook/mobile/makeEpub.php?id=%s";

    /*
    RegEx Strings
     */
    /**
     * Regex to obtain website host from URL. Group 2 is the host.
     */
    public static final String HOST_REGEX = "^(http[s]?:\\/\\/)?([^:\\/\\s]+)(\\/.*)?$";

    /**
     * Regex to help obtain file name from a Content-Disposition header (Do Matcher.find() and then Matcher.group()).
     */
    public static final String PF_FNAME_REGEX = "(?<=filename=\").*?(?=\")";

    /**
     * Regex to extract storyId from FictionHunt URL. Group 5 is the ID.
     */
    public static final String FICTIONHUNT_REGEX =
            "^(http[s]?:\\/\\/(([^:\\/\\s]*\\.)?fictionhunt.com\\/read\\/))?([^:\\/\\s]+)?(\\/\\d*)?$";

    /**
     * Regex to extract story ID from Fanfiction.net URL. Group 4 is the ID.
     */
    public static final String FFN_REGEX = "^(http[s]?:\\/\\/((www|m)\\.fanfiction.net\\/s\\/))?(\\d+)?(\\/.*)?$";

    /**
     * Regex to extract the story ID
     */
    public static final String SIYE_REGEX = ""; // TODO make this!

    /*
    File Template Strings
     */
    /**
     * Shamelessly taken from p0oody-files :D
     */
    public static final String CSS = "#chapText{\ttext-align: left;\tfont: 1em Calibri;\tline-height: 1.05em;" +
            "}#chapTitle{\tfont: bold 1.2em Calibri;\ttext-align: left;\tline-height: 1.25em;" +
            "}#ficTitle{\tfont: bold " +
            "1.7em Calibri;\ttext-align: center;}#ficAuthor{\tfont: 1.4em Calibri;\ttext-align: center;" +
            "}#footer {   " +
            "position: absolute;   bottom: 0;   width: 100%;   height :60px;   /* Height of the footer */   " +
            "font: 1em" +
            " Calibri}body{\ttext-align: left;\tfont: 1em Calibri;\tline-height: 1.05em;}";

    /**
     * Title Page. Has a number of areas for replacement using String.format(): -Title of story -Author of story -Rating
     * -Word count -Chapter count
     */
    public static final String TITLE_PAGE = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
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
            "  <p><strong>Rated:</strong> %s</p>\n" +
            "\n" +
            "  <p><strong>Word Count:</strong> %d</p>\n" +
            "\n" +
            "  <p><strong>Chapters:</strong> %d</p>\n" +
            "\n" +
            "</body>\n" +
            "</html>";

    /**
     * Chapter page. Has a number of areas for replacement using String.format(): -Chapter title -Chapter title -Chapter
     * text (HTML!)
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
