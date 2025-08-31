package org.mockenhaupt.fortgnox.misc;

import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {
    public static boolean andMatcher (String haystack, List<String> needles)
    {
        if (haystack == null || needles == null)
        {
            return false;
        }
        long  c = needles.stream().filter(needle -> haystack.toLowerCase().contains(needle.toLowerCase())).count();

        return c == needles.size();
    }

    public static boolean andMatcher (String haystack, String needles)
    {
        if (haystack == null || needles == null)
        {
            return false;
        }

        return andMatcher(haystack, Arrays.asList(needles.split("\\s+")));
    }


    public static String trimEnd(String toTrim)
    {
        if (toTrim == null ) return toTrim;

        String trimmed = toTrim.replaceAll("\\s+$", "");
        return trimmed;
    }

    /**
     * Get a URL-like string from the clipboard, convert it to a plain string suitable for
     * use as a password file name, or return an empty string if no URL is found or an error occurs.
     */
    public static String getClipboardUrlPasswordFileName () {
        return urlToPlainString(getUrlFromClipboard());
    }

    public static String getUrlFromClipboard()
    {
        try
        {
            return getUrlFromClipboardP();
        }
        catch (UnsupportedFlavorException e)
        {
            return "";
        }
        catch (IOException e)
        {
            return "";
        }
    }

    private static String getUrlFromClipboardP() throws UnsupportedFlavorException, IOException
    {

        Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
        if (cb == null) return "";
        if (!cb.isDataFlavorAvailable(DataFlavor.stringFlavor)) return "";
        String text = (String) cb.getData(DataFlavor.stringFlavor);
        if (text == null) return "";
        String s = text.trim();
        if (s.isEmpty()) return "";

        // Find first URL-like substring. Support typical schemes.
        Pattern p = Pattern.compile("(?i)(?:[a-z][a-z0-9+.-]*://)[^\n\r\t ]+|(?:www\\.[^\n\r\t ]+)");
        Matcher m = p.matcher(s);
        if (m.find()) {
            String url = m.group();
            return url;
        }
        return "";
    }


    /**
     * Convert a URL to a plain string consisting of the hostname only with non-alphanumeric
     * characters replaced by underscores. Examples:
     *  - https://www.youtube.com/watch?v=... -> youtube_com
     *  - http://sub.domain.co.uk/path -> sub_domain_co_uk
     * If the input is not a well-formed URL, a best-effort normalization will be applied:
     *  - strips leading scheme-like prefix and path/query, keeps host-like portion.
     */
    public static String urlToPlainString(final String url) {
        if (url == null) return null;
        String s = url.trim();
        if (s.isEmpty()) return "";

        // Remove scheme if present (e.g., http://, https://, ftp://)
        s = s.replaceFirst("^[a-zA-Z][a-zA-Z0-9+.-]*://", "");

        // If credentials are present (user:pass@host), drop them
        int atIdx = s.indexOf('@');
        if (atIdx >= 0) {
            s = s.substring(atIdx + 1);
        }

        // Cut off path/query/fragment after the host
        int slashIdx = s.indexOf('/');
        if (slashIdx >= 0) s = s.substring(0, slashIdx);
        int qIdx = s.indexOf('?');
        if (qIdx >= 0) s = s.substring(0, qIdx);
        int hashIdx = s.indexOf('#');
        if (hashIdx >= 0) s = s.substring(0, hashIdx);

        // Remove port if present
        int colonIdx = s.indexOf(':');
        if (colonIdx >= 0) s = s.substring(0, colonIdx);

        // Remove common www prefix
        if (s.toLowerCase().startsWith("www.")) {
            s = s.substring(4);
        }

        // Lowercase and replace non-alphanumeric with underscores
        s = s.toLowerCase();
        s = s.replaceAll("[^a-z0-9]", "_");
        // Collapse multiple underscores
        s = s.replaceAll("_+", "_");
        // Trim leading/trailing underscores
        s = s.replaceAll("^_+|_+$", "");

        return s;
    }

    
    /**
     * Return the base URL (scheme + host) of the given URL-like string.
     * - Keeps the scheme if present; if missing but a host is present, assumes http.
     * - Removes credentials, port, path, query, and fragment.
     * - Returns null for null input; empty string for empty/whitespace input.
     */
    public static String baseUrl(final String url) {
        if (url == null) return null;
        String s = url.trim();
        if (s.isEmpty()) return "";

        String scheme = null;
        // Extract scheme if present
        Matcher schemeMatcher = Pattern.compile("^([a-zA-Z][a-zA-Z0-9+.-]*):\\/\\/").matcher(s);
        if (schemeMatcher.find()) {
            scheme = schemeMatcher.group(1).toLowerCase();
            s = s.substring(schemeMatcher.end());
        }

        // Drop credentials if any
        int atIdx = s.indexOf('@');
        if (atIdx >= 0) {
            s = s.substring(atIdx + 1);
        }

        // Cut off path/query/fragment
        int slashIdx = s.indexOf('/');
        if (slashIdx >= 0) s = s.substring(0, slashIdx);
        int qIdx = s.indexOf('?');
        if (qIdx >= 0) s = s.substring(0, qIdx);
        int hashIdx = s.indexOf('#');
        if (hashIdx >= 0) s = s.substring(0, hashIdx);

        // Remove port
        int colonIdx = s.indexOf(':');
        if (colonIdx >= 0) s = s.substring(0, colonIdx);

        // If starts with www., keep it (since we want valid URL) – but tests for plain string removed it.
        // For baseUrl we generally keep the host as-is but normalize case to lower for consistency.
        String host = s.trim();
        if (host.isEmpty()) return "";
        host = host.toLowerCase();

        // If no scheme but looks like a host (e.g., example.com or www.example.com or 1.2.3.4), assume http
        if (scheme == null) {
            scheme = "http";
        }

        return scheme + "://" + host;
    }

}
