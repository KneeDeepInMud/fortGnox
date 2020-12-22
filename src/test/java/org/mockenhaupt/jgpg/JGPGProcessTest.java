package org.mockenhaupt.jgpg;


import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;

class JGPGProcessTest
{

    static JGPGProcess jgpgProcess;
    @BeforeAll
    static void init ()
    {
        jgpgProcess = new JGPGProcess(null);
    }

    @Test
    void getShortFileName ()
    {
        assertEquals("", jgpgProcess.getShortFileName("", "", true));
    }
}