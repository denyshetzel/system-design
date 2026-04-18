package br.com.systemdesign.urlshortening.application.port.in;

import br.com.systemdesign.urlshortening.application.model.ShortenedUrlView;

public interface GetShortUrlUseCase {
    ShortenedUrlView getUrlByShortUrl(String shortCode);
}
