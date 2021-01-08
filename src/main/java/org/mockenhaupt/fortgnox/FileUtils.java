package org.mockenhaupt.fortgnox;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class FileUtils
{
    public static final String RID_SEPARATOR = "@@";

    public static String getFileContent (String filePath)
    {
        StringBuilder contentBuilder = new StringBuilder();

        try (Stream<String> stream = Files.lines(Paths.get(filePath), StandardCharsets.UTF_8))
        {
            stream.forEach(s -> contentBuilder.append(s).append("\n"));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return contentBuilder.toString();
    }

    public static class ParsedDirectories
    {
        String revisedList = "";
        Map<String, String> directoryRecipientMap = new HashMap<>();
        List<String> directoryList = new ArrayList<>();
    }

    public static ParsedDirectories splitDirectoryString (String concatDirs)
    {
        ParsedDirectories ret = new ParsedDirectories();

        if (concatDirs.contains(";"))
        {
            String revisedProp = "";
            // prop is an array
            for (String token : concatDirs.split(";"))
            {
                if (!token.isEmpty())
                {
                    String dir;
                    String rid;
                    if (token.contains(RID_SEPARATOR)) {
                        dir = null;
                        rid = null;
                        String[] dirRid = token.split(RID_SEPARATOR);
                        if (dirRid.length > 1)
                        {
                            dir = dirRid[0];
                            rid = dirRid[1];
                            ret.directoryRecipientMap.put(dir, rid);
                        }
                    }
                    else
                    {
                        dir = token;
                        rid = null;
                    }
                    if (dir != null)
                    {
                        File f = new File(dir);
                        if (f.exists() && f.isDirectory())
                        {
                            ret.directoryList.add(f.getAbsolutePath());
                            revisedProp += dir;
                            if (rid != null)
                            {
                                revisedProp += RID_SEPARATOR + rid;
                            }
                            revisedProp += ";";
                        }
                    }
                }
            }
            revisedProp = revisedProp.replaceAll(";$", "");
            ret.revisedList = revisedProp;
        }
        else
        {
            ret.directoryList.add(concatDirs);
            ret.revisedList = concatDirs;
        }
        return ret;
    }

}
