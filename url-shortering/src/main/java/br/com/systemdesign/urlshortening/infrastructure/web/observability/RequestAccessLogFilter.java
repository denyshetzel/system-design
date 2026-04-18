package br.com.systemdesign.urlshortening.infrastructure.web.observability;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.CustomLog;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
@CustomLog
public class RequestAccessLogFilter extends OncePerRequestFilter {

    private static final String REQUEST_ID_HEADER = "X-Request-Id";
    private static final String FORWARDED_FOR = "X-Forwarded-For";

    @Value("${app.observability.request-log-sample-percent:1.0}")
    private double samplePercent;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.contains("/actuator")
                || path.contains("/swagger-ui")
                || path.contains("/api-docs");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        long startNs = System.nanoTime();
        boolean sampled = shouldSample();

        try {
            filterChain.doFilter(request, response);
        } finally {
            if (!sampled) {
                return;
            }
            long durationMs = (System.nanoTime() - startNs) / 1_000_000;
            String requestId = request.getHeader(REQUEST_ID_HEADER);
            String clientIp = extractClientIp(request);

            log.info(
                    "http_request method={} path={} status={} durationMs={} ip={} requestId={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus(),
                    durationMs,
                    clientIp,
                    requestId == null ? "-" : requestId
            );
        }
    }

    private boolean shouldSample() {
        if (samplePercent <= 0) {
            return false;
        }
        if (samplePercent >= 100) {
            return true;
        }
        return ThreadLocalRandom.current().nextDouble(100.0) < samplePercent;
    }

    private String extractClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader(FORWARDED_FOR);
        if (forwardedFor == null || forwardedFor.isBlank()) {
            return request.getRemoteAddr();
        }
        return forwardedFor.split(",")[0].trim();
    }
}
