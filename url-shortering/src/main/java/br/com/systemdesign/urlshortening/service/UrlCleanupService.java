package br.com.systemdesign.urlshortening.service;

import br.com.systemdesign.urlshortening.repository.ShortenedUrlRepository;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@CustomLog
public class UrlCleanupService {

    private final ShortenedUrlRepository repository;

    @Scheduled(cron = "${app.cleanup.expired-urls-cron:0 */15 * * * *}")
    @Transactional
    public void cleanupExpiredUrls() {
        long deleted = repository.deleteByExpiresAtBefore(LocalDateTime.now());
        if (deleted > 0) {
            log.info("Expired URLs cleanup completed. Deleted records: {}", deleted);
        } else {
            log.debug("Expired URLs cleanup completed. No records to delete.");
        }
    }
}

