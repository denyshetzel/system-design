package br.com.systemdesign.urlshortening.application.model;

import java.time.LocalDateTime;

public record ShortenedUrlView(
        Long id,
        String shortCode,
        String originalUrl,
        String shortUrl,
        LocalDateTime createdAt,
        LocalDateTime expiresAt,
        Long accessCount
) {
}
