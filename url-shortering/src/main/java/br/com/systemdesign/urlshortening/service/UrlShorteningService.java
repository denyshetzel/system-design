package br.com.systemdesign.urlshortening.service;

import br.com.systemdesign.urlshortening.config.AppConstants;
import br.com.systemdesign.urlshortening.dto.CreateShortUrlRequest;
import br.com.systemdesign.urlshortening.dto.UrlResponse;
import br.com.systemdesign.urlshortening.entity.ShortenedUrl;
import br.com.systemdesign.urlshortening.exception.UrlNotFoundException;
import br.com.systemdesign.urlshortening.repository.ShortenedUrlRepository;
import br.com.systemdesign.urlshortening.util.Validations;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@CustomLog
public class UrlShorteningService {

    private final ShortenedUrlRepository repository;

    @Value("${app.base.url}")
    private String baseUrl;

    @Retryable(
            retryFor = DataIntegrityViolationException.class,
            maxAttempts = 5,
            backoff = @Backoff(delay = 100, multiplier = 2)
    )
    @Transactional
    public UrlResponse createShortUrl(CreateShortUrlRequest request) {
        var expirationTime = Validations.calculateExpiration(request.expirationSeconds());
        var shortenedUrl = new ShortenedUrl(request.originalUrl(), expirationTime);
        var saved = repository.saveAndFlush(shortenedUrl);
        log.debug("Shortened URL created successfully. Short Code: {}", saved.getShortUrl());
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public UrlResponse getUrlByShortUrl(String shortCode) {
        log.debug("Fetching URL by short code: {}", shortCode);
        return repository.findByShortUrl(shortCode)
                .filter(url -> !url.isExpired())
                .map(this::mapToResponse)
                .orElseThrow(() -> new UrlNotFoundException("URL not found or expired"));
    }

    @Transactional
    public String redirectToOriginalUrl(String shortCode) {
        log.debug("Redirecting short code: {}", shortCode);
        var shortenedUrl = repository.findByShortUrl(shortCode)
                .filter(url -> !url.isExpired())
                .orElseThrow(() -> new UrlNotFoundException("URL not found or expired"));
        int updated = repository.incrementAccessCountById(shortenedUrl.getId());
        if (updated == 0) {
            log.warn("Access count was not incremented for short code: {}", shortCode);
        }
        return shortenedUrl.getOriginalUrl();
    }

    @Transactional
    public void deleteUrl(String shortCode) {
        log.debug("Removing URL with short code: {}", shortCode);
        var url = repository
                .findByShortUrl(shortCode)
                .orElseThrow(() -> new UrlNotFoundException("URL not found"));
        repository.delete(url);
        log.debug("URL successfully removed: {}", shortCode);
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
