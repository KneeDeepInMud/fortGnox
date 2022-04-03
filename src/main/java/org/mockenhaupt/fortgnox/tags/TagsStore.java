package org.mockenhaupt.fortgnox.tags;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class TagsStore
{

    public static void read (String tagsFileName)
    {
        File tagsFile = new File(tagsFileName);
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

        try
        {
            mapper.readValue(tagsFile, Object.class);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }


    public static void rebuild (List<String> secretdirs)
    {
    }
}
