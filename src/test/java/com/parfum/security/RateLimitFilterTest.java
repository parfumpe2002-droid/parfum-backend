package com.parfum.security;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

class RateLimitFilterTest {
    private final RateLimitFilter filter = new RateLimitFilter(new ObjectMapper());

    @Test
    void prefersCloudflareConnectingIpAndIgnoresSpoofedForwardedFor() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "1.2.3.4, 5.6.7.8");
        request.addHeader("CF-Connecting-IP", "203.0.113.20");
        request.setRemoteAddr("10.0.0.4");
        assertEquals("203.0.113.20", filter.clientIp(request));
    }

    @Test
    void fallsBackToRemoteAddressWhenCloudflareHeaderIsMissingOrInvalid() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("CF-Connecting-IP", "not-an-ip");
        request.addHeader("X-Forwarded-For", "1.2.3.4");
        request.setRemoteAddr("10.0.0.9");
        assertEquals("10.0.0.9", filter.clientIp(request));
    }
}
