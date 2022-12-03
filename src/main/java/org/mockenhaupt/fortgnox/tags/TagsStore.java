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
        String dirname = TagsStore.getFullPathNoSeparator(fileName);
        TagsFile hitFile = tagsForPath.get(dirname);
        return hitFile != null && hitFile.tagMatchesPattern(baseName, pattern);
    }

    public static void registerTags (TagsFile tagsFile)
    {
        String dirName = TagsStore.getFullPathNoSeparator(tagsFile.getFileName());
        System.err.println("XXX registerTags " +  dirName + " " + tagsFile.getFileName());
        tagsForPath.put(dirName, tagsFile);
    }

    public static String getTagsOfFile (String fileName)
    {
        return getTagsOfFile(fileName, false);
    }

    public static String getTagsOfFile (String fileName, boolean plainList)
    {
        if (fileName == null || fileName.isEmpty())
        {
            return "";
        }
        String dirname = TagsStore.getFullPathNoSeparator(fileName);
        TagsFile tagsFile = tagsForPath.get(dirname);
        String retVal = "";
        System.err.println("   XXX getTagsOfFile tagsFile " +tagsFile + " " + dirname);
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

        System.err.println("XXX saveTagsForFile store:" + TagsStore.toString2());
        System.err.println("XXX2 getTagsOfFile retval:" + retVal);

        return retVal;
    }

    public static void saveTagsForFile (String editEntry, String newTags) throws IOException
    {
        System.err.println("XXX saveTagsForFile " + editEntry + " - " + newTags);
        String tagsFileName = TagsStore.getFullPathNoSeparator(editEntry) + File.separator + "fgtags.yml";
        System.err.println("XXX saveTagsForFile " + tagsFileName + " - " + newTags);
        TagsFile tagsFile = new TagsFile(tagsFileName, true);
        tagsFile.saveTags(editEntry, newTags);
        System.err.println("XXX saveTagsForFile " + TagsStore.toString2());
    }

    public static String getFullPathNoSeparator (String path)
    {
        String retPath = FilenameUtils.getFullPathNoEndSeparator(path);
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            if (retPath.endsWith(":"))
            {
                retPath += File.separator;
            }
        }

        return retPath;
    }
    public static String toString2 ()
    {
        StringBuilder sb = new StringBuilder();
        tagsForPath.forEach((s, tagsFile) ->
        {
            sb.append(s);
            sb.append("=");
            sb.append(tagsFile);
            sb.append('\n');
        });
        return "TagsStore{" +
                sb.toString() +
                "}";
    }
}
