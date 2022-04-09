package org.mockenhaupt.fortgnox.tags;

import org.apache.commons.io.FilenameUtils;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class TagsStore
{
    static Map<String, TagsFile> tagsForPath = new HashMap<>();

    public static boolean matchesTag (String fileName, String pattern)
    {
        String baseName = FilenameUtils.getBaseName(fileName);
        String dirname = FilenameUtils.getFullPathNoEndSeparator(fileName);
        TagsFile hitFile = tagsForPath.get(dirname);
        return hitFile != null && hitFile.tagMatchesPattern(baseName, pattern);
    }

    public static void registerTags (TagsFile tagsFile)
    {
        tagsForPath.put(tagsFile.getDirname(), tagsFile);
    }
}
