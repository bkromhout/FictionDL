package bkromhout.fdl.util;

import bkromhout.fdl.Main;
import bkromhout.fdl.ex.StoryinfoJsonException;
import bkromhout.fdl.ui.Controller;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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
import java.util.List;

/**
 * Utility class with static methods.
 */
public abstract class Util {

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
        if (s.contains(C.LOG_ERR)) text.setFill(Color.ORANGERED); // Errors.
        else if (s.contains(C.LOG_WARN)) text.setFill(Color.GOLD); // Warnings.
        else if (s.contains(C.LOG_LOUD)) text.setFill(Color.rgb(152, 118, 170)); // Verbose; Purple.
        else if (s.contains(C.LOG_BLUE)) text.setFill(Color.ROYALBLUE); // Info.
        else if (s.contains(C.LOG_GREEN)) text.setFill(Color.FORESTGREEN); // Info.

        // Process any log style tags.
        if (s.contains(C.LOG_ULINE)) text.setUnderline(true);

        // Potentially prepend line type, strip tags, then print.
        s = prependLogLineType(s);
        text.setText(stripLogStyleTags(s));
        Controller.appendLogText(text);
    }

    /**
     * If running in verbose, non-GUI mode, prepend the log line with a type indicator, such as "I:" or "W:".
     * @param s String to log.
     * @return Prepended log line. If we aren't running in verbose, non-GUI mode, return the same string, unchanged.
     */
    private static String prependLogLineType(String s) {
        if (!(Main.isVerbose && !Main.isGui)) return s;

        // Prepend line types based on log tags.
        if (s.contains(C.LOG_ERR)) return "E: " + s;
        else if (s.contains(C.LOG_WARN)) return "W: " + s;
        else if (s.contains(C.LOG_LOUD)) return "V: " + s;
        else return "I: " + s;
    }

    /**
     * String log color indicators from a string.
     * @param s String to strip
     * @return Stripped string.
     */
    private static String stripLogStyleTags(String s) {
        return s.replace(C.LOG_ERR, "").replace(C.LOG_BLUE, "").replace(C.LOG_GREEN, "").replace(C.LOG_LOUD, "")
                .replace(C.LOG_WARN, "").replace(C.LOG_ULINE, "");
    }

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
     * Download an HTML document from the given url.
     * <p>
     * This method closes any resources it holds before returning.
     * @param url url to download.
     * @return A Document object, or null if the url was malformed.
     */
    public static Document getHtml(String url) {
        // Use Jsoup to parse the HTML, being sure to close resources before returning. (Note that while this is
        // typed as an InputStream, whose close method "does nothing", it is actually an Okio type whose close method
        // does do something.
        try (InputStream responseBodyStream = getRawHtmlStream(url)) {
            if (responseBodyStream == null) return null;
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
     * <p>
     * Note that while this returns an InputStream type, it is actually an Okio type which subclasses InputStream.
     * Callers should always be sure to call its {@code close} method when finished using it to ensure that it cannot
     * leak.
     * @param url url of page to download.
     * @return Raw HTML of the web page as an InputStream, or null if we failed to download the page or the status code
     * wasn't in the range of [200..300).
     */
    private static InputStream getRawHtmlStream(String url) {
        Response response = getRawHtml(url);
        if (response == null) return null;

        try {
            return response.body().byteStream();
        } catch (IOException e) {
            e.printStackTrace();
            // We're just ignoring the exception really.
            logf(C.HTML_DL_FAILED, url);
            return null;
        }
    }

    /**
     * Download a web page using the given OkHttpClient from the given url.
     * <p>
     * This method will close the Response's ResponseBody before returning null if we were able to get a valid Response
     * but the status code did not indicate success.
     * @param url url of page to download.
     * @return OkHttp Response, or null if we failed to download the page or the status code wasn't in the range of
     * [200..300).
     */
    private static Response getRawHtml(String url) {
        try {
            Request request = new Request.Builder().url(url).build();
            Response response = C.getHttpClient().newCall(request).execute();
            if (response.isSuccessful()) return response;

            // If the request wasn't successful, close the ResponseBody before returning null so that it cannot leak.
            response.body().close();
            return null;
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
        // Replace unicode replacement/null characters with non breaking spaces.
        htmlStr = htmlStr.replace('\uFFFD', '\u00A0');
        // Escape ampersands that aren't part of entities.
        //htmlStr = htmlStr.replaceAll("[&](?!(#|amp;|gt;|lt;|quot;|nbsp;))", "&#x26;");
        htmlStr = htmlStr.replaceAll("&(?![A-Za-z]+[0-9]*;|#[0-9]+;|#x[0-9a-fA-F]+;)", "&#x26;");
        // Convert pesky Win-1252 characters to their correct unicode equivalents.
        htmlStr = htmlStr.replace('\u0096', '–') // En dash (U+2013)
                         .replace('\u0097', '—') // Em dash (U+2014)
                         .replace('\u0091', '‘') // Left single quotation mark (U+2018)
                         .replace('\u0092', '’') // Right single quotation mark (U+2019)
                         .replace('\u0093', '“') // Left double quotation mark (U+201C)
                         .replace('\u0094', '”') // Right double quotation mark (U+201D)
                         .replace('\u0095', '•') // Bullet (U+2022)
                         .replace('\u0085', '…'); // Horizontal ellipses (U+2026)
        // Convert incorrect sequences of UTF-16 characters which were actually UTF-8 to a single, correct character.
        htmlStr = htmlStr.replace("\u00E2\u0080\u0093", "–") // En dash (U+2013)
                         .replace("\u00E2\u0080\u0094", "—") // Em dash (U+2014)
                         .replace("\u00E2\u0080\u0098", "‘") // Left single quotation mark (U+2018)
                         .replace("\u00E2\u0080\u0099", "’") // Right single quotation mark (U+2019)
                         .replace("\u00E2\u0080\u009C", "“") // Left double quotation mark (U+201C)
                         .replace("\u00E2\u0080\u009D", "”") // Right double quotation mark (U+201D)
                         .replace("\u00E2\u0080\u00A2", "•") // Bullet (U+2022)
                         .replace("\u00E2\u0080\u00A6", "…"); // Horizontal ellipses (U+2026)
        // Remove any control characters which are still present, except CR, LF, and tab.
        htmlStr = htmlStr.replaceAll("[^\\P{Cc}\\t\\r\\n]", "");
        // Squeaky clean!
        return htmlStr;
    }

    /**
     * Closes any of the given tags in the given html string.
     * @param tag The type of tag, such as hr, or br.
     * @return A string with all of the given tags closed.
     */
    private static String closeTags(String in, String tag) {
        if (in == null) return null;
        return in.replaceAll(String.format("(\\<%s[^>]*?(?<!/))(\\>)", tag), "$1/>");
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
    private static String ensureLegalFilename(String in) {
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

    /**
     * Get the value of an element in {@code json} called {@code elemName} as a String.
     * <p>
     * Note the distinction between returning null and throwing an exception here.<br/>Null is returned if we either
     * don't have what we need to access the element, or if the element doesn't exist.<br/>An exception is thrown if the
     * element exists, be it is not in the form we expected (and so, some exception was thrown which we caught).
     * @param json     JSON object which contains an element called {@code elemName}.
     * @param elemName Name of the JSON element to get the String value of.
     * @return Returns the value of the {@code elemName} element as a String.<br/>If either of {@code json} or {@code
     * elemName} are null, {@code elemName} is the empty string, {@code json} does not contain an element named {@code
     * elemName}, or the value of the element is {@code null}, returns null instead.
     * @throws StoryinfoJsonException if we cannot return the value of the element as a String. The exception message
     *                                will be {@code elemName}.
     */
    public static String getJsonStr(JsonObject json, String elemName) throws StoryinfoJsonException {
        // Parameter checks.
        if (json == null || elemName == null || elemName.isEmpty() || !json.has(elemName)) return null;
        // Element checks.
        JsonElement elem = json.get(elemName);
        if (elem.isJsonNull()) return null;

        // Try to get a String value from the element, catching any exceptions thrown if this isn't a string element.
        try {
            return elem.getAsJsonPrimitive().getAsString();
        } catch (IllegalStateException | ClassCastException e) {
            throw new StoryinfoJsonException(elemName, e);
        }
    }

    /**
     * Get the value of an element in {@code json} called {@code elemName} as an integer.
     * <p>
     * Note the distinction between returning null and throwing an exception here.<br/>Null is returned if we either
     * don't have what we need to access the element, or if the element doesn't exist.<br/>An exception is thrown if the
     * element exists, be it is not in the form we expected (and so, some exception was thrown which we caught).
     * @param json     JSON object which contains an element called {@code elemName}.
     * @param elemName Name of the JSON element to get the integer value of.
     * @return Returns the value of the {@code elemName} element as an integer.<br/>If either of {@code json} or {@code
     * elemName} are null, {@code elemName} is the empty string, {@code json} does not contain an element named {@code
     * elemName}, or the value of the element is {@code null}, returns -1 instead.
     * @throws StoryinfoJsonException if we cannot return the value of the element as a integer. The exception message
     *                                will be {@code elemName}.
     */
    public static int getJsonInt(JsonObject json, String elemName) throws StoryinfoJsonException {
        // Parameter checks.
        if (json == null || elemName == null || elemName.isEmpty() || !json.has(elemName)) return -1;
        // Element checks.
        JsonElement elem = json.get(elemName);
        if (elem.isJsonNull()) return -1;

        // Try to get an int value from the element, catching any exceptions thrown if this isn't a number element.
        try {
            return elem.getAsJsonPrimitive().getAsInt();
        } catch (IllegalStateException | ClassCastException | NumberFormatException e) {
            throw new StoryinfoJsonException(elemName, e);
        }
    }
}
