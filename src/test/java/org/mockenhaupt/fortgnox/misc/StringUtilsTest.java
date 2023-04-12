package org.mockenhaupt.fortgnox.misc;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StringUtilsTest {

    @Test
    void andMatcher() {
        assertEquals(false, StringUtils.andMatcher(null, (String)null));
        assertEquals(true, StringUtils.andMatcher("hanse", "a"));
        assertEquals(false, StringUtils.andMatcher("hanse", "x"));
        assertEquals(true, StringUtils.andMatcher("hanse", "ANS"));
        assertEquals(true, StringUtils.andMatcher("hanse", "s h a e s"));
    }
}
