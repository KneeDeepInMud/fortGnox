package org.mockenhaupt.fortgnox.tags;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
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
        System.err.println("XXX registerTags " +  tagsFile.getDirname());
        tagsForPath.put(tagsFile.getDirname(), tagsFile);
    }

    public static String getTagsOfFile (String fileName)
    {
        return getTagsOfFile(fileName, false);
    }

    public static String getTagsOfFile (String fileName, boolean plainList)
    {
        System.err.println("XXX getTagsOfFile " + fileName);
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
            Set<String> tagsOfFile = tagsFile.getTags(baseName);
            if (tagsOfFile != null)
            {
                if (plainList)
                {
                    String tagList = String.join(" ", tagsFile.getTags(baseName));
                    retVal = tagList.isEmpty() ? "" : tagList;
                }
                else
                {
                    String tagList = String.join(", ", tagsFile.getTags(baseName));
                    retVal = tagList.isEmpty() ? "" : " (" + tagList + ")";
                }
            }
        }

        System.err.println("XXX2 getTagsOfFile " + retVal);

        return retVal;
    }

    public static void saveTagsForFile (String editEntry, String newTags) throws IOException
    {
        String tagsFileName = FilenameUtils.getFullPathNoEndSeparator(editEntry) + File.separator + "fgtags.yml";
        System.err.println("XXX saveTagsForFile " + tagsFileName + " - " + newTags);
        TagsFile tagsFile = new TagsFile(tagsFileName, true);
        tagsFile.saveTags(editEntry, newTags);
    }
}
