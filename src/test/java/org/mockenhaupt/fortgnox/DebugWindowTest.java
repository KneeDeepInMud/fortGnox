package org.mockenhaupt.fortgnox;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.mockenhaupt.fortgnox.DebugWindow.Category.FAV;
import static org.mockenhaupt.fortgnox.DebugWindow.Category.FILTER;
import static org.mockenhaupt.fortgnox.DebugWindow.Category.GPG;

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

        instance.enableDebugCategory(FILTER);
        instance.isEnabled(DebugWindow.CAT_FILTER);
        Assertions.assertEquals(true, instance.isEnabled(DebugWindow.CAT_FILTER));
        Assertions.assertEquals(false, instance.isEnabled(DebugWindow.CAT_GPG));
        Assertions.assertEquals(false, instance.isEnabled(DebugWindow.CAT_FAVORITES));

        instance.enableDebugCategory(GPG);
        Assertions.assertEquals(true, instance.isEnabled(DebugWindow.CAT_FILTER));
        Assertions.assertEquals(true, instance.isEnabled(DebugWindow.CAT_GPG));
        Assertions.assertEquals(false, instance.isEnabled(DebugWindow.CAT_FAVORITES));

        instance.disableDebugCategory(FILTER);
        Assertions.assertEquals(false, instance.isEnabled(DebugWindow.CAT_FILTER));
        Assertions.assertEquals(true, instance.isEnabled(DebugWindow.CAT_GPG));
        Assertions.assertEquals(false, instance.isEnabled(DebugWindow.CAT_FAVORITES));

        instance.disableDebugCategory(FILTER);
        Assertions.assertEquals(false, instance.isEnabled(DebugWindow.CAT_FILTER));
        Assertions.assertEquals(true, instance.isEnabled(DebugWindow.CAT_GPG));
        Assertions.assertEquals(false, instance.isEnabled(DebugWindow.CAT_FAVORITES));

        instance.enableDebugCategory(FAV);
        Assertions.assertEquals(false, instance.isEnabled(DebugWindow.CAT_FILTER));
        Assertions.assertEquals(true, instance.isEnabled(DebugWindow.CAT_GPG));
        Assertions.assertEquals(true, instance.isEnabled(DebugWindow.CAT_FAVORITES));

        instance.disableDebugCategory(FAV);
        Assertions.assertEquals(false, instance.isEnabled(DebugWindow.CAT_FILTER));
        Assertions.assertEquals(true, instance.isEnabled(DebugWindow.CAT_GPG));
        Assertions.assertEquals(false, instance.isEnabled(DebugWindow.CAT_FAVORITES));

        instance.disableDebugCategory(GPG);
        Assertions.assertEquals(false, instance.isEnabled(DebugWindow.CAT_FILTER));
        Assertions.assertEquals(false, instance.isEnabled(DebugWindow.CAT_GPG));
        Assertions.assertEquals(false, instance.isEnabled(DebugWindow.CAT_FAVORITES));
    }

}