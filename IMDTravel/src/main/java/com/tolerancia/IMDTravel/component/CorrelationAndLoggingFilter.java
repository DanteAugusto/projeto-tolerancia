package com.tolerancia.IMDTravel.component;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationAndLoggingFilter extends OncePerRequestFilter {

    public static final String HEADER = "X-Correlation-Id";
    private static final Logger log = LoggerFactory.getLogger(CorrelationAndLoggingFilter.class);

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String cid = request.getHeader(HEADER);
        if (cid == null || cid.isBlank()) {
            cid = UUID.randomUUID().toString();
        }

        MDC.put("cid", cid);
        response.setHeader(HEADER, cid);

        long startNanos = System.nanoTime();
        int status = response.getStatus();
        try {
            filterChain.doFilter(request, response);
            status = response.getStatus();
        } catch (Exception e) {
            status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
            throw e;
        } finally {
            long elapsedMs = (System.nanoTime() - startNanos) / 1_000_000;
            if (!"/health".equals(request.getRequestURI()) || status != HttpServletResponse.SC_OK) {
                MDC.put("ms", String.valueOf(elapsedMs));
                log.info("{} {} -> {}", request.getMethod(), request.getRequestURI(), status);
            }
            MDC.clear();
        }
    }
}
