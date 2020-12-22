package org.mockenhaupt.jgpg;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class DebugWindowTest
{

    static DebugWindow instance;

    @BeforeAll
    static void setUp ()
    {
        instance = DebugWindow.get();
    }


    @Test
    void debugCategories ()
    {
        instance.setDebugMask(0);

        instance.enableDebugCategory(DebugWindow.CAT_FILTER);
        instance.isEnabled(DebugWindow.CAT_FILTER);
        Assertions.assertEquals(true, instance.isEnabled(DebugWindow.CAT_FILTER));
        Assertions.assertEquals(false, instance.isEnabled(DebugWindow.CAT_GPG));
        Assertions.assertEquals(false, instance.isEnabled(DebugWindow.CAT_FAVORITES));

        instance.enableDebugCategory(DebugWindow.CAT_GPG);
        Assertions.assertEquals(true, instance.isEnabled(DebugWindow.CAT_FILTER));
        Assertions.assertEquals(true, instance.isEnabled(DebugWindow.CAT_GPG));
        Assertions.assertEquals(false, instance.isEnabled(DebugWindow.CAT_FAVORITES));

        instance.disableDebugCategory(DebugWindow.CAT_FILTER);
        Assertions.assertEquals(false, instance.isEnabled(DebugWindow.CAT_FILTER));
        Assertions.assertEquals(true, instance.isEnabled(DebugWindow.CAT_GPG));
        Assertions.assertEquals(false, instance.isEnabled(DebugWindow.CAT_FAVORITES));

        instance.disableDebugCategory(DebugWindow.CAT_FILTER);
        Assertions.assertEquals(false, instance.isEnabled(DebugWindow.CAT_FILTER));
        Assertions.assertEquals(true, instance.isEnabled(DebugWindow.CAT_GPG));
        Assertions.assertEquals(false, instance.isEnabled(DebugWindow.CAT_FAVORITES));

        instance.enableDebugCategory(DebugWindow.CAT_FAVORITES);
        Assertions.assertEquals(false, instance.isEnabled(DebugWindow.CAT_FILTER));
        Assertions.assertEquals(true, instance.isEnabled(DebugWindow.CAT_GPG));
        Assertions.assertEquals(true, instance.isEnabled(DebugWindow.CAT_FAVORITES));

        instance.disableDebugCategory(DebugWindow.CAT_FAVORITES);
        Assertions.assertEquals(false, instance.isEnabled(DebugWindow.CAT_FILTER));
        Assertions.assertEquals(true, instance.isEnabled(DebugWindow.CAT_GPG));
        Assertions.assertEquals(false, instance.isEnabled(DebugWindow.CAT_FAVORITES));

        instance.disableDebugCategory(DebugWindow.CAT_GPG);
        Assertions.assertEquals(false, instance.isEnabled(DebugWindow.CAT_FILTER));
        Assertions.assertEquals(false, instance.isEnabled(DebugWindow.CAT_GPG));
        Assertions.assertEquals(false, instance.isEnabled(DebugWindow.CAT_FAVORITES));
    }

}