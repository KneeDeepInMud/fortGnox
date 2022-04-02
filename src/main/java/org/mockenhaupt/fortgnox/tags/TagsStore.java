package org.mockenhaupt.fortgnox.tags;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.io.IOException;

public class TagsStore
{

    public void read (String  tagsFileName)
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


}
