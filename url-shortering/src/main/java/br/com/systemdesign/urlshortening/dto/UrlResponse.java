package br.com.systemdesign.urlshortening.dto;

import java.time.LocalDateTime;

public record UrlResponse(
        Long id,
        String shortCode,
        String originalUrl,
        String shortUrl,
        LocalDateTime createdAt,
        LocalDateTime expiresAt,
        Long accessCount
) {
}
