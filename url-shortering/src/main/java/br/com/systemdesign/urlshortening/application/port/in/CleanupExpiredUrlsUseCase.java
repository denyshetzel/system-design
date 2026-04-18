package br.com.systemdesign.urlshortening.application.port.in;

public interface CleanupExpiredUrlsUseCase {
    long cleanupExpiredUrls();
}
