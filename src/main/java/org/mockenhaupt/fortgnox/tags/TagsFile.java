package org.mockenhaupt.fortgnox.tags;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.util.Set;

public class TagsFile
{
    private final String fileName;
    private final String baseName;
    private final String dirname;
    private TagYamlData yamlData;

    public TagsFile (String fileName) throws IOException
    {
        this.fileName = fileName;
        this.baseName = FilenameUtils.getBaseName(fileName);
        this.dirname = FilenameUtils.getFullPathNoEndSeparator(fileName);
        parse(fileName);
    }

    public String getFileName ()
    {
        return fileName;
    }

    public String getBaseName ()
    {
        return baseName;
    }

    public String getDirname ()
    {
        return dirname;
    }

    private void parse (String tagsFileName) throws IOException
    {
        File tagsFile = new File(tagsFileName);
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

        yamlData = mapper.readValue(tagsFile, TagYamlData.class);
    }
    public Set<String> getTags (String passwordFileName)
    {
        return yamlData.tags.get(passwordFileName);
    }


    public boolean hasTagsFor (String baseName)
    {
        if (yamlData != null)
        {
            return yamlData.tags.get(baseName) != null;
        }
        return false;
    }

    public boolean tagMatchesPattern (String baseName, String pattern)
    {
        Set<String> tagsForFile = getTags(baseName);
        return tagsForFile != null && tagsForFile.stream().anyMatch(tag -> tag.toLowerCase().contains(pattern));
    }
}
