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


    public static String getClipboardUrlPasswordFileName () {
        try {
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
                return urlToPlainString(url);
            }
            return "";
        } catch (HeadlessException | IllegalStateException e) {
            return "";
        } catch (UnsupportedFlavorException | IOException e) {
            return "";
        }
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

}
