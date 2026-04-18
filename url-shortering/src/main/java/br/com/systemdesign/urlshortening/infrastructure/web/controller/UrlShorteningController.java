package br.com.systemdesign.urlshortening.infrastructure.web.controller;

import br.com.systemdesign.urlshortening.application.model.CreateShortUrlCommand;
import br.com.systemdesign.urlshortening.application.model.ShortenedUrlView;
import br.com.systemdesign.urlshortening.application.port.in.UrlShorteningFacade;
import br.com.systemdesign.urlshortening.config.AppConstants;
import br.com.systemdesign.urlshortening.infrastructure.web.dto.CreateShortUrlRequest;
import br.com.systemdesign.urlshortening.infrastructure.web.dto.UrlResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(AppConstants.API_SHORTENER)
@RequiredArgsConstructor
@CustomLog
@Validated
public class UrlShorteningController {

    private final UrlShorteningFacade facade;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<UrlResponse> createShortUrl(@Valid @RequestBody CreateShortUrlRequest request) {
        log.debug("Creating shortened URL for: {}", request.originalUrl());
        var response = facade.createShortUrl(
                new CreateShortUrlCommand(request.originalUrl(), request.expirationSeconds())
        );
        log.debug("Shortened URL created with code: {}", response.shortCode());
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(response));
    }

    @GetMapping("/{shortUrl}")
    public ResponseEntity<UrlResponse> getUrl(
            @PathVariable
            @NotBlank(message = "{validation.shortcode.required}")
            @Size(min = 1, max = 10, message = "Short code must be between 1 and 10 characters")
            String shortUrl) {
        log.debug("Retrieving URL details for code: {}", shortUrl);
        var response = facade.getUrlByShortUrl(shortUrl);
        return ResponseEntity.ok(toDto(response));
    }

    @GetMapping("/{shortUrl}/redirect")
    public ResponseEntity<Void> redirectToOriginalUrl(
            @PathVariable
            @NotBlank(message = "{validation.shortcode.required}")
            @Size(min = 1, max = 10, message = "Short code must be between 1 and 10 characters")
            String shortUrl) {
        log.debug("Redirecting short code: {}", shortUrl);
        var originalUrl = facade.redirectToOriginalUrl(shortUrl);
        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", originalUrl)
                .cacheControl(CacheControl.noCache())
                .build();
    }

    @DeleteMapping("/{shortUrl}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteUrl(
            @PathVariable
            @NotBlank(message = "{validation.shortcode.required}")
            @Size(min = 1, max = 10, message = "Short code must be between 1 and 10 characters")
            String shortUrl) {
        log.debug("Deleting URL with code: {}", shortUrl);
        facade.deleteUrl(shortUrl);
        return ResponseEntity.noContent().build();
    }

    private UrlResponse toDto(ShortenedUrlView view) {
        return new UrlResponse(
                view.id(),
                view.shortCode(),
                view.originalUrl(),
                view.shortUrl(),
                view.createdAt(),
                view.expiresAt(),
                view.accessCount()
        );
    }

}
