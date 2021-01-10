package org.mockenhaupt.fortgnox.misc;

import javax.swing.ImageIcon;
import java.awt.Image;
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
        public String revisedList = "";
        public Map<String, String> directoryRecipientMap = new HashMap<>();
        public List<String> directoryList = new ArrayList<>();
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

    public static ImageIcon getScaledIcon (ImageIcon imageIcon, int size)
    {
        return getScaledIcon(imageIcon, size, size);
    }

    public static ImageIcon getScaledIcon (Class<?> clazz, String resource, int size)
    {
        return getScaledIcon(new ImageIcon(clazz.getResource(resource)), size, size);
    }

    public static ImageIcon getScaledIcon (ImageIcon imageIcon, int w, int h)
    {
        Image image = imageIcon.getImage(); // transform it
        Image newimg = image.getScaledInstance(w, h, java.awt.Image.SCALE_SMOOTH); // scale it the smooth way
        return new ImageIcon(newimg);  // transform it back
    }

}
