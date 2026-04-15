package br.com.systemdesign.urlshortening.dto;

import br.com.systemdesign.urlshortening.validation.SafeUrl;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

public record CreateShortUrlRequest(
    @NotBlank(message = "{validation.url.original.required}")
    @URL(message = "{validation.url.original.invalid}")
    @SafeUrl
    String originalUrl,
    Integer expirationSeconds
) {}
