package org.mockenhaupt.fortgnox.tags;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class TagsFile
{
    private final String fileName;
    private final String baseName;
    private final String dirname;
    private TagYamlData yamlData;
    private final File tagsFile;
    public TagsFile (String fileName) throws IOException
    {
        this(fileName, false);
    }

    public TagsFile (String fileName, boolean create) throws IOException
    {
        this.fileName = fileName;
        this.baseName = FilenameUtils.getBaseName(fileName);
        this.dirname = FilenameUtils.getFullPathNoEndSeparator(fileName);
        tagsFile = new File(fileName);
        if (!tagsFile.exists() && create)
        {
             ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
             mapper.writeValue(tagsFile, new TagYamlData());
        }

        parse();
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

    private void parse () throws IOException
    {
        File tagsFile = new File(this.fileName);
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

        yamlData = mapper.readValue(tagsFile, TagYamlData.class);
    }

    public Set<String> getTags (String passwordFileName)
    {
        if (Optional.ofNullable(yamlData).map(TagYamlData::getTags).isPresent())
        {
            return yamlData.tags.get(passwordFileName);
        }
        return null;
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

    public void saveTags (String editEntry, String newTags) throws IOException
    {
        if (yamlData.getTags() == null)
        {
            yamlData.setTags(new HashMap<>());
        }
        String baseName = FilenameUtils.getBaseName(editEntry);
        Set<String> stringSet = new HashSet<>();
        if (newTags != null && !newTags.isEmpty())
        {
            stringSet.addAll(Arrays.asList(newTags.split(" ")));
            yamlData.getTags().put(baseName, stringSet);
        }
        else
        {
            yamlData.getTags().remove(baseName);
        }

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.writeValue(tagsFile, yamlData);
    }
}
