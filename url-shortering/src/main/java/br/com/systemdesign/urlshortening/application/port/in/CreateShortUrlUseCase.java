package br.com.systemdesign.urlshortening.application.port.in;

import br.com.systemdesign.urlshortening.application.model.CreateShortUrlCommand;
import br.com.systemdesign.urlshortening.application.model.ShortenedUrlView;

public interface CreateShortUrlUseCase {
    ShortenedUrlView createShortUrl(CreateShortUrlCommand command);
}
