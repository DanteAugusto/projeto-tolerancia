package com.tolerancia.IMDTravel.component;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class CorrelationAndLoggingFilterTest {

    @Test
    void shouldReuseProvidedCorrelationIdAndClearMdcAfterwards() throws Exception {
        CorrelationAndLoggingFilter filter = new CorrelationAndLoggingFilter();
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/buyTicket");
        request.addHeader(CorrelationAndLoggingFilter.HEADER, "cid-externo-123");
        MockHttpServletResponse response = new MockHttpServletResponse();

        AtomicReference<String> cidInsideChain = new AtomicReference<>();
        FilterChain chain = (req, res) -> cidInsideChain.set(MDC.get("cid"));

        filter.doFilter(request, response, chain);

        assertEquals("cid-externo-123", cidInsideChain.get());
        assertEquals("cid-externo-123", response.getHeader(CorrelationAndLoggingFilter.HEADER));
        assertNull(MDC.get("cid"));
    }

    @Test
    void shouldGenerateCorrelationIdWhenMissing() throws Exception {
        CorrelationAndLoggingFilter filter = new CorrelationAndLoggingFilter();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/sales");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (req, res) -> {});

        String generated = response.getHeader(CorrelationAndLoggingFilter.HEADER);
        assertNotNull(generated);
        assertFalse(generated.isBlank());
        assertNull(MDC.get("cid"));
    }
}
