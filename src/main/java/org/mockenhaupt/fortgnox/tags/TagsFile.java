package org.mockenhaupt.fortgnox.tags;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.util.Set;

public class TagsFile
{
    String fileName;
    String baseName;
    String dirname;

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


        TagYamlData o = mapper.readValue(tagsFile, TagYamlData.class);
        System.err.println(o);

    }
    public Set<String> getTags (String passwordFileName)
    {
        return null;
    }
}
