package br.com.systemdesign.urlshortening.application.port.in;

public interface RedirectShortUrlUseCase {
    String redirectToOriginalUrl(String shortCode);
}
