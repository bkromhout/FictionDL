package bkromhout;

/**
 * Constants
 */
public class C {
    /**
     * Shamelessly taken from p0oody-files :D
     */
    public static final String CSS =
            "#chapText{\ttext-align: left;\tfont: 1em Calibri;\tline-height: 1.05em;" +
            "}#chapTitle{\tfont: bold 1.2em Calibri;\ttext-align: left;\tline-height: 1.25em;}#ficTitle{\tfont: bold " +
            "1.7em Calibri;\ttext-align: center;}#ficAuthor{\tfont: 1.4em Calibri;\ttext-align: center;}#footer {   " +
            "position: absolute;   bottom: 0;   width: 100%;   height :60px;   /* Height of the footer */   font: 1em" +
            " Calibri}body{\ttext-align: left;\tfont: 1em Calibri;\tline-height: 1.05em;}";

    /**
     * Title Page.
     * Has a number of areas for replacement using String.format():
     * -Title of story
     * -Author of story
     * -Rating
     * -Word count
     * -Chapter count
     */
    public static final String TITLE_PAGE =
            "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
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
     * Chapter page.
     * Has a number of areas for replacement using String.format():
     * -Chapter title
     * -Chapter title
     * -Chapter text (HTML!)
     */
    public static final String CHAPTER_PAGE =
            "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
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
