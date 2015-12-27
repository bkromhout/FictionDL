package bkromhout.fdl.util;

import bkromhout.fdl.Main;
import bkromhout.fdl.ui.Controller;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.parser.Tag;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Utility class with static methods.
 */
public abstract class Util {

    /**
     * Attempt to verify that a directory path exists (or can be created), then return a Path of it.
     * @param path String path of a directory.
     * @return Path of directory if given path is valid or creatable, otherwise prints line and exits. Returns null if
     * passed null.
     * @throws IllegalArgumentException if passed null or if there are any exceptions that occur.
     */
    public static Path tryGetPath(String path) throws IllegalArgumentException {
        File dir = new File(path);
        if ((!dir.exists() && !dir.mkdirs()) || !dir.isDirectory())
            throw new IllegalArgumentException(String.format(C.INVALID_PATH, dir.getAbsolutePath()));
        return dir.toPath();
    }

    /**
     * Attempt to verify that a file exists and is readable and writable, then return a File of it.
     * @param path String path of a file.
     * @return File if file at given path exists and is readable and writable, otherwise prints line and exits. Returns
     * null if passed null.
     * @throws IllegalArgumentException if passed null or if there are any exceptions that occur.
     */
    public static File tryGetFile(String path) throws IllegalArgumentException {
        File file = new File(path);
        if (!file.exists() || !file.isFile() || !file.canRead() || !file.canWrite())
            throw new IllegalArgumentException(String.format(C.INVALID_PATH, file.getAbsolutePath()));
        return file;
    }

    /**
     * Takes a long time value that was parsed from FanFiction.net, multiplies it by 1000 to make it match the Java long
     * time format, then returns a string with it printed in the format MMM dd, yyyy.
     * @param ffnTime String long value from an FFN data-xutime attribute.
     * @return Date string.
     */
    public static String dateFromFfnTime(String ffnTime) {
        // Add some zeros to make it like a Java long.
        long longFfnTime = Long.parseLong(ffnTime);
        longFfnTime *= 1000;
        // Create a Date object.
        Date date = new Date(longFfnTime);
        // Create the formatter.
        DateFormat df = DateFormat.getDateInstance();
        df.setTimeZone(TimeZone.getTimeZone("US/Pacific"));
        // Return string.
        return df.format(date);
    }

    /**
     * Download an HTML document from the given url
     * @param url url to download.
     * @return A Document object, or null if the url was malformed.
     */
    public static Document downloadHtml(String url) {
        InputStream responseBodyStream = downloadRawHtml(url);
        if (responseBodyStream == null) return null;
        try {
            return Jsoup.parse(responseBodyStream, null, url);
        } catch (IOException e) {
            e.printStackTrace();
            // We're just ignoring the exception really.
            logf(C.PARSE_HTML_FAILED, url);
            return null;
        }
    }

    /**
     * Download a web page using the given OkHttpClient from the given url.
     * @param url url of page to download.
     * @return Raw HTML of the web page as an InputStream, or null if we failed to download the page.
     */
    public static InputStream downloadRawHtml(String url) {
        try {
            Request request = new Request.Builder().url(url).build();
            Response response = C.getHttpClient().newCall(request).execute();
            return response.body().byteStream();
        } catch (IOException e) {
            e.printStackTrace();
            // We're just ignoring the exception really.
            logf(C.HTML_DL_FAILED, url);
            return null;
        }
    }

    /**
     * Do some common HTML cleaning tasks.
     * @param htmlStr HTML string.
     * @return Cleaned HTML string.
     */
    public static String cleanHtmlString(String htmlStr) {
        if (htmlStr == null) return null;
        // Make sure <br> and <hr> tags are closed.
        htmlStr = closeTags(htmlStr, "br");
        htmlStr = closeTags(htmlStr, "hr");
        // Escape pesky characters.
        htmlStr = convertWin1252Chars(htmlStr);
        // Remove any control characters which are still present.
        htmlStr = removeControlChars(htmlStr);
        // Squeaky clean!
        return htmlStr;
    }

    /**
     * Closes any of the given tags in the given html string.
     * @param tag The type of tag, such as hr, or br.
     * @return A string with all of the given tags closed.
     */
    public static String closeTags(String in, String tag) {
        if (in == null) return null;
        return in.replaceAll(String.format("(\\<%s[^>]*?(?<!/))(\\>)", tag), "$1/>");
    }

    /**
     * Escape ampersands that aren't part of code points.
     * @param in The string to escape.
     * @return The escaped string.
     */
    public static String escapeAmps(String in) {
        if (in == null) return null;
        //return in.replaceAll("[&](?!(#|amp;|gt;|lt;|quot;|nbsp;))", "&#x26;");
        return in.replaceAll("&(?![A-Za-z]+[0-9]*;|#[0-9]+;|#x[0-9a-fA-F]+;)", "&#x26;");
    }

    /**
     * Un-Escape ampersands, because epublib is completely idiotic and doesn't check to see if an ampersand is part of a
     * character code, it just escapes it again.
     * @param in String to un-escape.
     * @return Un-escaped string.
     */
    public static String unEscapeAmps(String in) {
        if (in == null) return null;
        return in.replace("&amp;", "&").replace("&#x26;", "&");
    }

    /**
     * Removes any instance of the Unicode replacement character, U+FFFD. Sadly, some sites appear to be littered with
     * this character, indicating that the author's original content was likely not correctly converted from their
     * original character encoding to the site's character encoding :( I think the best choice here is usually to
     * replace it with a non-breaking space, U+00A0.
     * @param in String to fix.
     * @return Fixed string.
     */
    public static String removeFFFDChars(String in) {
        if (in == null) return null;
        return in.replace('\uFFFD', '\u00A0');
    }

    /**
     * Removes any Unicode control characters that still exist in the string. Doesn't remove tab, line feed, or carriage
     * return.
     * @param in String to fix.
     * @return Fixed string.
     */
    public static String removeControlChars(String in) {
        if (in == null) return null;
        return in.replaceAll("[^\\P{Cc}\\t\\r\\n]", "");
    }

    /**
     * Converts characters that, while valid in Windows-1252 are control characters in Unicode, to their corresponding
     * Unicode representations. Also escapes any ampersands not already part of a character code, and converts any
     * Unicode replacement characters to non-breaking spaces.
     * @param in The string to escape.
     * @return The escaped string.
     */
    public static String convertWin1252Chars(String in) {
        if (in == null) return null;
        in = removeFFFDChars(in);
        in = escapeAmps(in);
        return in.replace('\u0096', '–').replace('\u0097', '—').replace('\u0091', '‘').replace('\u0092', '’').replace(
                '\u0093', '“').replace('\u0094', '”').replace('\u0095', '•').replace('\u0085', '…');
    }

    /**
     * Create a filename for an ePUB file based on a title and author.
     * @param title  Title.
     * @param author Author.
     * @return ePUB filename like "[Title] - [Author].epub".
     */
    public static String makeEpubFname(String title, String author) {
        if (title == null || author == null) return null;
        return ensureLegalFilename(String.format("%s - %s.epub", title, author));
    }

    /**
     * Removes or replaces characters which could potentially be illegal, and does a few other things.
     * @param in The file name to fix.
     * @return The fixed file name.
     */
    public static String ensureLegalFilename(String in) {
        if (in == null) return null;
        // Unescape ampersands.
        in = unEscapeAmps(in);
        // Remove problematic characters.
        String out = in.replace("<", "").replace(">", "").replace(":", " -").replace("\"", "").replace("/", "").replace(
                "\\", "").replace("|", "-").replace("?", "").replace("*", "").replace("\0", "");
        // Ensure that the end of the filename isn't a space or period for Windows' sake.
        out = out.trim();
        while (out.charAt(out.length() - 1) == '.') out = out.substring(0, out.length() - 2);
        // Let's make sure that the file doesn't start with a period either, or it'll be hidden on Linux-based OSs.
        while (out.charAt(0) == '.') out = out.replaceFirst("\\.", "");
        // Okay, we should be good now.
        return out;
    }

    /**
     * Log a string. If running from the CLI, goes to System.out. If running from the GUI, goes to the log TextFlow.
     * @param str String to log.
     */
    public static void log(String str) {
        if (str == null) return;
        if (Main.isGui) logString(str + "\n");
        else System.out.println(stripLogStyleTags(str));
    }

    /**
     * Calls Util.log if verbose output is enabled.
     * @param str String to log.
     */
    public static void loud(String str) {
        if (Main.isVerbose) log(str);
    }

    /**
     * Log a formatted string. If running from the CLI, goes to System.out. If running from the GUI, goes to the log
     * TextFlow.
     * @param format Format string.
     * @param args   Objects to substitute into format string.
     */
    public static void logf(String format, Object... args) {
        if (format == null) return;
        if (Main.isGui) logString(String.format(format, args));
        else System.out.printf(stripLogStyleTags(format), args);
    }

    /**
     * Calls Util.logf if verbose output is enabled.
     * @param format Format string.
     * @param args   Objects to substitute into format string.
     */
    public static void loudf(String format, Object... args) {
        if (Main.isVerbose) logf(format, args);
    }

    /**
     * Log a string to a GUI TextFlow, making sure to process any log color indicators (See near the top of the C.java
     * file).
     * @param s String to log.
     */
    private static void logString(String s) {
        Text text = new Text();
        // Process any log color style tags, in order of priority.
        if (s.contains(C.LOG_RED)) text.setFill(Color.ORANGERED);
        else if (s.contains(C.LOG_BLUE)) text.setFill(Color.ROYALBLUE);
        else if (s.contains(C.LOG_GREEN)) text.setFill(Color.FORESTGREEN);
        else if (s.contains(C.LOG_PURPLE)) text.setFill(Color.rgb(152, 118, 170)); // Verbose only.
        else if (s.contains(C.LOG_GOLD)) text.setFill(Color.GOLD); // Verbose only, but usually just for dev use.

        // Process any log style tags.
        if (s.contains(C.LOG_ULINE)) text.setUnderline(true);

        // Strip log tags, then send to the TextFlow.
        text.setText(stripLogStyleTags(s));
        Controller.appendLogText(text);
    }

    /**
     * String log color indicators from a string.
     * @param in String to strip
     * @return Stripped string.
     */
    public static String stripLogStyleTags(String in) {
        return in.replace(C.LOG_RED, "").replace(C.LOG_BLUE, "").replace(C.LOG_GREEN, "").replace(C.LOG_PURPLE, "")
                 .replace(C.LOG_GOLD, "").replace(C.LOG_ULINE, "");
    }

    /**
     * Build a simple regex string which is some number of string literals OR'ed together.
     * @param literals Strings to OR together in the regex. It is assumed that at least one String is supplied.
     * @return Regex string suitable for using with .find() (it doesn't capture any specific groups), or null if no
     * strings were given.
     */
    public static String buildOrRegex(String... literals) {
        // Build regex.
        StringBuilder regex = new StringBuilder();
        for (int i = 0; i < literals.length; i++) {
            regex.append("\\Q").append(literals[i]).append("\\E");
            if (i != literals.length - 1) regex.append('|');
        }
        return regex.toString();
    }

    /**
     * Return a new div element whose children are copies of some range of the given parent's child nodes.
     * @param parent   Element to copy child nodes from.
     * @param startIdx Index in parent's child node list to start copying from (inclusive)
     * @param endIdx   Index in parent's child node list to stop copying at (exclusive)
     * @return A new div Element with copies of nodes from the parent, or null if any of the parameters were invalid.
     */
    public static Element divFromChildCopies(Element parent, int startIdx, int endIdx) {
        // Parameter checks.
        if (parent == null || startIdx < 0 || startIdx >= parent.childNodes().size() || endIdx <= startIdx ||
                endIdx > parent.childNodes().size()) return null;
        // Copy parent's child nodes.
        List<Node> nodeCopies = parent.childNodesCopy();
        // Create the new div.
        Element div = new Element(Tag.valueOf("div"), "");
        // Loop through the copied nodes, starting at the startIdx and up to but not including the endIdx, and append
        // those nodes to the new div.
        for (int i = startIdx; i < endIdx; i++) div.appendChild(nodeCopies.get(i));
        // Return the summary HTML.
        return div;
    }
}
