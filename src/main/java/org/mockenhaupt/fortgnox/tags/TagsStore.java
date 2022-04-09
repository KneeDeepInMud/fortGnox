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

    public static String getTagsOfFile (String fileName)
    {
        if (fileName == null || fileName.isEmpty())
        {
            return "";
        }
        String dirname = FilenameUtils.getFullPathNoEndSeparator(fileName);
        TagsFile tagsFile = tagsForPath.get(dirname);
        String retVal = "";
        if (tagsFile != null)
        {
            String baseName = FilenameUtils.getBaseName(fileName);
            String tagList = String.join(", ", tagsFile.getTags(baseName));
            if (tagList != null && !tagList.isEmpty())
            {
                retVal = " (" + tagList + ")";
            }
        }
        return retVal;
    }
}
