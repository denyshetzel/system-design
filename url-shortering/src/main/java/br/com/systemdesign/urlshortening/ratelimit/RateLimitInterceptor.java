package br.com.systemdesign.urlshortening.ratelimit;

import br.com.systemdesign.urlshortening.config.AppConstants;
import br.com.systemdesign.urlshortening.exception.RateLimitExceededException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final long ONE_MINUTE_MS = 60_000L;
    private static final String FORWARDED_FOR = "X-Forwarded-For";
    private static final String CREATE_PATH = AppConstants.API_SHORTENER;
    private static final String REDIRECT_SUFFIX = "/redirect";

    private final Map<String, WindowCounter> counters = new ConcurrentHashMap<>();
    private final int createLimitPerMinute;
    private final int redirectLimitPerMinute;

    public RateLimitInterceptor(
            @Value("${app.rate-limit.create-per-minute:60}") int createLimitPerMinute,
            @Value("${app.rate-limit.redirect-per-minute:300}") int redirectLimitPerMinute) {
        this.createLimitPerMinute = createLimitPerMinute;
        this.redirectLimitPerMinute = redirectLimitPerMinute;
    }

    @Override
    public boolean preHandle(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler) {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String ip = extractClientIp(request);

        if ("POST".equalsIgnoreCase(method) && uri.endsWith(CREATE_PATH)) {
            checkLimit("create:" + ip, createLimitPerMinute);
        } else if ("GET".equalsIgnoreCase(method) && uri.endsWith(REDIRECT_SUFFIX)) {
            checkLimit("redirect:" + ip, redirectLimitPerMinute);
        }
        return true;
    }

    private void checkLimit(String key, int limit) {
        long now = System.currentTimeMillis();
        WindowCounter counter = counters.computeIfAbsent(key, k -> new WindowCounter(now, new AtomicInteger(0)));

        synchronized (counter) {
            if (now - counter.windowStartMs >= ONE_MINUTE_MS) {
                counter.windowStartMs = now;
                counter.requests.set(0);
            }

            int current = counter.requests.incrementAndGet();
            if (current > limit) {
                long retryAfterSeconds = Math.max(1L, (ONE_MINUTE_MS - (now - counter.windowStartMs)) / 1_000L);
                throw new RateLimitExceededException("Rate limit exceeded", retryAfterSeconds);
            }
        }
    }

    private String extractClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader(FORWARDED_FOR);
        if (forwardedFor == null || forwardedFor.isBlank()) {
            return request.getRemoteAddr();
        }
        return forwardedFor.split(",")[0].trim();
    }

    private static class WindowCounter {
        private long windowStartMs;
        private final AtomicInteger requests;

        private WindowCounter(long windowStartMs, AtomicInteger requests) {
            this.windowStartMs = windowStartMs;
            this.requests = requests;
        }
    }
}

