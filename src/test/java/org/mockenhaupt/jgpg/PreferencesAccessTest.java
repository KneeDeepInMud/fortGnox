package org.mockenhaupt.jgpg;

import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PreferencesAccessTest
{

    static final String NODE = PreferencesAccessTest.class.getPackage().getName();

    @BeforeEach
    void setUp ()
    {
        PreferencesAccess.getInstance(NODE).clear();
    }


    private Object expectedEvent;

    @org.junit.jupiter.api.Test
    void getPreference ()
    {
        PreferencesAccess pa = PreferencesAccess.getInstance(NODE);

        pa.addPropertyChangeListener(
                propertyChangeEvent -> assertEquals(expectedEvent, propertyChangeEvent.getNewValue())
        );

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

    }
}