package org.mockenhaupt.fortgnox;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PreferencesAccessTest
{

    static final String NODE = FgPreferences.PREFERENCE_NODE_TEST;

    @BeforeEach
    public void setUp ()
    {
        PreferencesAccess.UNIT_TEST = true;
        PreferencesAccess.getInstance(NODE).clear();
    }


    private Object expectedEvent;

    @org.junit.jupiter.api.Test
    void getPreference ()
    {
        PreferencesAccess pa = PreferencesAccess.getInstance(NODE);

        PropertyChangeListener p = propertyChangeEvent -> assertEquals(expectedEvent, propertyChangeEvent.getNewValue());

        pa.addPropertyChangeListener(p);

        String expected = "default_string";
        expectedEvent = expected;
        String result = pa.getPreference("string", "default_string");
        assertEquals(expected, result);
        result = pa.getPreference("string", "OOO");
        assertEquals(expected, result); // not OOO



        expected = "newString";
        expectedEvent = expected;
        pa.putPreference("string", expected);
        result = pa.getPreference("string", "XXXXX");
        assertEquals(expected, result);

        expectedEvent = "new_value";
        assertEquals(pa, pa.putPreference("string", "new_value"));
        assertEquals("new_value", pa.getPreference("string", ""));

        expectedEvent = 5;
        assertEquals(5, pa.getPreference("int", 5));

        expectedEvent = 6;
        assertEquals(pa, pa.putPreference("int", 6));
        assertEquals(6, pa.getPreference("int", 666));

        pa.removePropertyChangeListener(p);
    }

    @Test
    void putLongProperty() {
        PreferencesAccess pa = PreferencesAccess.getInstance(NODE);

        PropertyChangeListener p = propertyChangeEvent ->
                assertEquals(FgPreferences.PREF_EXCEPTION, propertyChangeEvent.getPropertyName()
                );

        pa.addPropertyChangeListener(p);

        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("{");
        int max = 1024 * 3;
        for (int i = 0 ; i < max; ++i)
        {
            String key = String.format("key_%05d", i);
            String val = String.format("val_%05d", i);
            jsonBuilder.append('"');
            jsonBuilder.append(key);
            jsonBuilder.append('"');
            jsonBuilder.append(':');
            jsonBuilder.append(val);
            if (i < max - 1)
            {
                jsonBuilder.append(',');
            }
        }
        jsonBuilder.append("}");
        pa.put("BIGJSON", jsonBuilder.toString());
        pa.removePropertyChangeListener(p);

    }
}
