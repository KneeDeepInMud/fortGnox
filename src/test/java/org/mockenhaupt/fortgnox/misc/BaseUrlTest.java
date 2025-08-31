package org.mockenhaupt.fortgnox.misc;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BaseUrlTest {

    @Test
    void returnsNullForNullInput() {
        assertNull(StringUtils.baseUrl(null));
    }

    @Test
    void returnsEmptyForEmptyOrWhitespace() {
        assertEquals("", StringUtils.baseUrl(""));
        assertEquals("", StringUtils.baseUrl("   \t\n  "));
    }

    @Test
    void preservesSchemeAndHostOnly() {
        assertEquals("https://www.youtube.com", StringUtils.baseUrl("https://www.youtube.com/watch?v=1"));
        assertEquals("http://sub.domain.co.uk", StringUtils.baseUrl("http://sub.domain.co.uk/path/to/res"));
        assertEquals("https://api.example.org", StringUtils.baseUrl("https://user:pass@api.example.org:8443/v1/endpoint?x=1#frag"));
    }

    @Test
    void infersHttpWhenNoScheme() {
        assertEquals("http://www.example.com", StringUtils.baseUrl("www.example.com/path?q=1"));
        assertEquals("http://example.com", StringUtils.baseUrl("example.com"));
    }

    @Test
    void ipAndCase() {
        assertEquals("http://192.168.0.1", StringUtils.baseUrl("http://192.168.0.1/dashboard"));
        assertEquals("http://xn--exämple-qmc.com", StringUtils.baseUrl("XN--exämple-qmc.com"));
    }

    @Test
    void bestEffortNonUrl() {
        assertEquals("http://my-host", StringUtils.baseUrl("my-host/some/path?x=1"));
    }
}
