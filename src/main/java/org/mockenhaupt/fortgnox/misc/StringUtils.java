package org.mockenhaupt.fortgnox.misc;

import java.util.Arrays;
import java.util.List;

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
}
