package br.com.systemdesign.urlshortening.service;

import br.com.systemdesign.urlshortening.application.port.in.CreateShortUrlUseCase;
import br.com.systemdesign.urlshortening.application.port.in.DeleteShortUrlUseCase;
import br.com.systemdesign.urlshortening.application.port.in.GetShortUrlUseCase;
import br.com.systemdesign.urlshortening.application.port.in.RedirectShortUrlUseCase;
import br.com.systemdesign.urlshortening.application.port.in.UrlShorteningFacade;
import br.com.systemdesign.urlshortening.application.model.CreateShortUrlCommand;
import br.com.systemdesign.urlshortening.application.model.ShortenedUrlView;
import br.com.systemdesign.urlshortening.application.port.out.ShortenedLinkStore;
import br.com.systemdesign.urlshortening.config.AppConstants;
import br.com.systemdesign.urlshortening.domain.model.ShortenedLink;
import br.com.systemdesign.urlshortening.exception.UrlNotFoundException;
import br.com.systemdesign.urlshortening.validation.Validations;
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
public class UrlShorteningService implements
        UrlShorteningFacade,
        CreateShortUrlUseCase,
        GetShortUrlUseCase,
        RedirectShortUrlUseCase,
        DeleteShortUrlUseCase {

    private final ShortenedLinkStore store;

    @Value("${app.base.url}")
    private String baseUrl;

    @Retryable(
            retryFor = DataIntegrityViolationException.class,
            maxAttempts = 5,
            backoff = @Backoff(delay = 100, multiplier = 2)
    )
    @Transactional
    public ShortenedUrlView createShortUrl(CreateShortUrlCommand command) {
        var expirationTime = Validations.calculateExpiration(command.expirationSeconds());
        var saved = store.save(command.originalUrl(), expirationTime);
        log.debug("Shortened URL created successfully. Short Code: {}", saved.shortCode());
        return mapToResponse(saved);
    }

    @Transactional
    public ShortenedUrlView getUrlByShortUrl(String shortCode) {
        log.debug("Fetching URL by short code: {}", shortCode);
        var shortenedUrl = store.findByShortCode(shortCode)
                .filter(url -> !url.isExpired())
                .orElseThrow(() -> new UrlNotFoundException("URL not found or expired"));

        Long updatedAccessCount = store.incrementAccessCountByIdReturning(shortenedUrl.id());
        if (updatedAccessCount == null) {
            log.warn("Access count was not incremented for short code: {}", shortCode);
            throw new UrlNotFoundException("URL not found or expired");
        }

        var updatedLink = new ShortenedLink(
                shortenedUrl.id(),
                shortenedUrl.shortCode(),
                shortenedUrl.originalUrl(),
                shortenedUrl.createdAt(),
                shortenedUrl.expiresAt(),
                updatedAccessCount
        );
        return mapToResponse(updatedLink);
    }

    @Transactional
    public String redirectToOriginalUrl(String shortCode) {
        log.debug("Redirecting short code: {}", shortCode);
        var shortenedUrl = store.findByShortCode(shortCode)
                .filter(url -> !url.isExpired())
                .orElseThrow(() -> new UrlNotFoundException("URL not found or expired"));
        Long updated = store.incrementAccessCountByIdReturning(shortenedUrl.id());
        if (updated == null) {
            log.warn("Access count was not incremented for short code: {}", shortCode);
        }
        return shortenedUrl.originalUrl();
    }

    @Transactional
    public void deleteUrl(String shortCode) {
        log.debug("Removing URL with short code: {}", shortCode);
        var url = store
                .findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException("URL not found"));
        store.deleteById(url.id());
        log.debug("URL successfully removed: {}", shortCode);
    }

    private ShortenedUrlView mapToResponse(ShortenedLink shortenedUrl) {
        return new ShortenedUrlView(
                shortenedUrl.id(),
                shortenedUrl.shortCode(),
                shortenedUrl.originalUrl(),
                baseUrl + AppConstants.API_SHORTENER + "/" + shortenedUrl.shortCode(),
                shortenedUrl.createdAt(),
                shortenedUrl.expiresAt(),
                shortenedUrl.accessCount()
        );
    }

}
