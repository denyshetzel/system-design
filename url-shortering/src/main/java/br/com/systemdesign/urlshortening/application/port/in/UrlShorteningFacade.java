package br.com.systemdesign.urlshortening.application.port.in;

import br.com.systemdesign.urlshortening.application.model.CreateShortUrlCommand;
import br.com.systemdesign.urlshortening.application.model.ShortenedUrlView;

public interface UrlShorteningFacade {
    ShortenedUrlView createShortUrl(CreateShortUrlCommand command);

    ShortenedUrlView getUrlByShortUrl(String shortCode);

    String redirectToOriginalUrl(String shortCode);

    void deleteUrl(String shortCode);
}
