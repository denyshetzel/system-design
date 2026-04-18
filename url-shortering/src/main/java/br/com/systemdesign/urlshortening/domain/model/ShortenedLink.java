package br.com.systemdesign.urlshortening.domain.model;

import java.time.LocalDateTime;

public record ShortenedLink(
        Long id,
        String shortCode,
        String originalUrl,
        LocalDateTime createdAt,
        LocalDateTime expiresAt,
        Long accessCount
) {
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
}
