package br.com.systemdesign.urlshortening.application.port.in;

public interface DeleteShortUrlUseCase {
    void deleteUrl(String shortCode);
}
