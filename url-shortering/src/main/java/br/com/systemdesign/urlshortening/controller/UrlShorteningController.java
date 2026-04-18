package br.com.systemdesign.urlshortening.controller;

import br.com.systemdesign.urlshortening.config.AppConstants;
import br.com.systemdesign.urlshortening.dto.CreateShortUrlRequest;
import br.com.systemdesign.urlshortening.dto.UrlResponse;
import br.com.systemdesign.urlshortening.service.UrlShorteningService;
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

    private final UrlShorteningService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<UrlResponse> createShortUrl(@Valid @RequestBody CreateShortUrlRequest request) {
        log.debug("Creating shortened URL for: {}", request.originalUrl());
        var response = service.createShortUrl(request);
        log.debug("Shortened URL created with code: {}", response.shortCode());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{shortUrl}")
    public ResponseEntity<UrlResponse> getUrl(
            @PathVariable
            @NotBlank(message = "{validation.shortcode.required}")
            @Size(min = 1, max = 10, message = "Short code must be between 1 and 10 characters")
            String shortUrl) {
        log.debug("Retrieving URL details for code: {}", shortUrl);
        var response = service.getUrlByShortUrl(shortUrl);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{shortUrl}/redirect")
    public ResponseEntity<Void> redirectToOriginalUrl(
            @PathVariable
            @NotBlank(message = "{validation.shortcode.required}")
            @Size(min = 1, max = 10, message = "Short code must be between 1 and 10 characters")
            String shortUrl) {
        log.debug("Redirecting short code: {}", shortUrl);
        var originalUrl = service.redirectToOriginalUrl(shortUrl);
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
        service.deleteUrl(shortUrl);
        return ResponseEntity.noContent().build();
    }

}
