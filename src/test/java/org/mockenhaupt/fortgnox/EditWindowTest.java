package org.mockenhaupt.fortgnox;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EditWindowTest
{


//    @Test
    void insertChangeTag ()
    {
        // crap tests, used for development only
        {
            String in = "abcd\ncdef $Changed: 02020$ ababab\nabab";
            String out = EditWindow.insertChangeTag(in);
            assertNotEquals(in, out);
        }
        {
            String in = "abcd\ncdef cdef $Changed: 2022-05-01T01:54:27.298397+02:00[Europe/Berlin]$ ababab\nababab";
            String out = EditWindow.insertChangeTag(in);
            assertNotEquals(in, out);
        }
        {
            String in = "abcd\ncdef $Changed:$ ababab\nabab$Changed:$ab";
            String out = EditWindow.insertChangeTag(in);
            assertNotEquals(in, out);
        }
        {
            String in = "abcd\ncdef";
            String out = EditWindow.insertChangeTag(in);
            assertEquals(in, out);
        }

    }
}
