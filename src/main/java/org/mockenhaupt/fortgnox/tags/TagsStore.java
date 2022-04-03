package org.mockenhaupt.fortgnox.tags;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class TagsStore
{




    public static boolean matchesTag (String fileName, String pattern)
    {
        String baseName = FilenameUtils.getBaseName(fileName);
        String dirname = FilenameUtils.getPathNoEndSeparator(fileName);
        return false;
    }

    public static void add (TagsFile tagsFile)
    {

    }
}
