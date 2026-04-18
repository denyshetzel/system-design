package br.com.systemdesign.urlshortening.application.model;

public record CreateShortUrlCommand(
        String originalUrl,
        Long expirationSeconds
) {
}
