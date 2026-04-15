package br.com.systemdesign.urlshortening.service;

import br.com.systemdesign.urlshortening.config.AppConstants;
import br.com.systemdesign.urlshortening.dto.CreateShortUrlRequest;
import br.com.systemdesign.urlshortening.dto.UrlResponse;
import br.com.systemdesign.urlshortening.entity.ShortenedUrl;
import br.com.systemdesign.urlshortening.exception.UrlExpiredException;
import br.com.systemdesign.urlshortening.exception.UrlNotFoundException;
import br.com.systemdesign.urlshortening.repository.ShortenedUrlRepository;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@CustomLog
public class UrlShorteningService {

    private final ShortenedUrlRepository repository;

    @Value("${app.base.url}")
    private String baseUrl;

    @Transactional
    public UrlResponse createShortUrl(CreateShortUrlRequest request) {
        log.info("Creating shortened URL for: {}", request.originalUrl());
        LocalDateTime expirationTime = null;
        if (request.expirationSeconds() != null && request.expirationSeconds() > 0) {
            expirationTime = LocalDateTime.now().plusSeconds(request.expirationSeconds());
        }
        var shortenedUrl = new ShortenedUrl(request.originalUrl(), expirationTime);
        var saved = repository.save(shortenedUrl);
        log.info("Shortened URL created successfully. Short Code: {}", saved.getShortUrl());
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public UrlResponse getUrlByShortUrl(String shortCode) {
        log.info("Fetching URL by short code: {}", shortCode);
        var url = repository
                .findByShortUrl(shortCode)
                .orElseThrow(() -> new UrlNotFoundException("URL not found"));
        if (url.isExpired()) throw new UrlExpiredException("URL has expired");
        return mapToResponse(url);
    }

    @Transactional
    public String redirectToOriginalUrl(String shortCode) {
        log.info("Redirecting short code: {}", shortCode);
        var shortenedUrl = repository
                .findByShortUrl(shortCode)
                .orElseThrow(() -> new UrlNotFoundException("URL not found"));
        if (shortenedUrl.isExpired()) throw new UrlExpiredException("URL has expired");
        incrementAccessCountAsync(shortenedUrl);
        return shortenedUrl.getOriginalUrl();
    }

    @Transactional
    public void deleteUrl(String shortCode) {
        log.info("Removing URL with short code: {}", shortCode);
        var url = repository
                .findByShortUrl(shortCode)
                .orElseThrow(() -> new UrlNotFoundException("URL not found"));
        repository.delete(url);
        log.info("URL successfully removed: {}", shortCode);
    }

    @Async("applicationTaskExecutor")
    @Transactional
    public void incrementAccessCountAsync(ShortenedUrl shortenedUrl) {
        try {
            shortenedUrl.incrementAccessCount();
            repository.save(shortenedUrl);
        } catch (Exception e) {
            log.error("Failed to increment access count for URL: {}", shortenedUrl.getShortUrl(), e);
        }
    }

    private UrlResponse mapToResponse(ShortenedUrl shortenedUrl) {
        return new UrlResponse(
                shortenedUrl.getId(),
                shortenedUrl.getShortUrl(),
                shortenedUrl.getOriginalUrl(),
                baseUrl + AppConstants.API_SHORTENER + "/" + shortenedUrl.getShortUrl(),
                shortenedUrl.getCreatedAt(),
                shortenedUrl.getExpiresAt(),
                shortenedUrl.getAccessCount()
        );
    }

}
