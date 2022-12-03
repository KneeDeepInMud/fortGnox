package org.mockenhaupt.fortgnox.tags;

import java.util.Map;
import java.util.Set;

public class TagYamlData
{
    Map<String, Set<String>> tags;

    public Map<String, Set<String>> getTags ()
    {
        return tags;
    }

    public void setTags (Map<String, Set<String>> _tags)
    {
        this.tags = _tags;
    }


    @Override
    public String toString ()
    {
        StringBuilder sb = new StringBuilder();
        if (tags != null)
        {
            tags.forEach((key, value) ->
            {
                sb.append(key);
                sb.append('=');
                sb.append(value);
                sb.append("; ");
            });
        }
        return "TagYamlData{" +
                "tags=" +  sb.toString() +
                '}';
    }
}
