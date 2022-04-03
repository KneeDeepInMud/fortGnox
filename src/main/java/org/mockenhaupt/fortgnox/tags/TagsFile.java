package org.mockenhaupt.fortgnox.tags;

import org.apache.commons.io.FilenameUtils;

import java.util.Set;

public class TagsFile
{
    String fileName;
    String baseName;

    public TagsFile (String fileName)
    {
        this.fileName = fileName;
        this.baseName = FilenameUtils.getBaseName(fileName);
    }


    public Set<String> getTags (String passwordFileName)
    {
        return null;
    }
}
