package org.mockenhaupt.fortgnox.misc;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UrlToPlainStringTest {

    @Test
    void returnsNullForNullInput() {
        assertNull(StringUtils.urlToPlainString(null));
    }

    @Test
    void returnsEmptyForEmptyOrWhitespace() {
        assertEquals("", StringUtils.urlToPlainString(""));
        assertEquals("", StringUtils.urlToPlainString("   \t\n  "));
    }

    @Test
    void youtubeExample() {
        String in = "https://www.youtube.com/watch?v=Zj1dkQo4WcQ&ab_channel=MeidasTouch";
        assertEquals("youtube_com", StringUtils.urlToPlainString(in));
    }

    @Test
    void subdomainAndMultiTld() {
        String in = "http://sub.domain.co.uk/path/to/resource";
        assertEquals("sub_domain_co_uk", StringUtils.urlToPlainString(in));
    }

    @Test
    void removesPortCredentialsAndFragments() {
        String in = "https://user:pass@api.example.org:8443/v1/endpoint?x=1#frag";
        assertEquals("api_example_org", StringUtils.urlToPlainString(in));
    }

    @Test
    void handlesNoSchemeAndWww() {
        assertEquals("example_com", StringUtils.urlToPlainString("www.example.com/path?q=1"));
        assertEquals("example_com", StringUtils.urlToPlainString("example.com"));
    }

    @Test
    void ipAddressAndWeirdChars() {
        assertEquals("192_168_0_1", StringUtils.urlToPlainString("http://192.168.0.1/dashboard"));
        assertEquals("xn__exmple_qmc_com", StringUtils.urlToPlainString("XN--exämple-qmc.com"));
    }

    @Test
    void bestEffortNonUrl() {
        // Should strip path-like portions and normalize
        assertEquals("my_host", StringUtils.urlToPlainString("my-host/some/path?x=1"));
    }
}
