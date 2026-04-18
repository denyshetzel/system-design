package br.com.systemdesign.urlshortening.infrastructure.persistence;

import br.com.systemdesign.urlshortening.application.port.out.ShortenedLinkStore;
import br.com.systemdesign.urlshortening.domain.model.ShortenedLink;
import br.com.systemdesign.urlshortening.infrastructure.persistence.jpa.entity.ShortenedUrl;
import br.com.systemdesign.urlshortening.infrastructure.persistence.jpa.repository.ShortenedUrlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ShortenedUrlPersistenceAdapter implements ShortenedLinkStore {

    private final ShortenedUrlRepository repository;

    @Override
    public ShortenedLink save(String originalUrl, LocalDateTime expiresAt) {
        ShortenedUrl saved = repository.saveAndFlush(new ShortenedUrl(originalUrl, expiresAt));
        return toDomain(saved);
    }

    @Override
    public Optional<ShortenedLink> findByShortCode(String shortCode) {
        return repository.findByShortUrl(shortCode).map(this::toDomain);
    }

    @Override
    public int incrementAccessCountById(Long id) {
        return repository.incrementAccessCountById(id);
    }

    @Override
    public Long incrementAccessCountByIdReturning(Long id) {
        return repository.incrementAccessCountByIdReturning(id);
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    @Override
    public long deleteExpiredBefore(LocalDateTime dateTime) {
        return repository.deleteByExpiresAtBefore(dateTime);
    }

    private ShortenedLink toDomain(ShortenedUrl entity) {
        return new ShortenedLink(
                entity.getId(),
                entity.getShortUrl(),
                entity.getOriginalUrl(),
                entity.getCreatedAt(),
                entity.getExpiresAt(),
                entity.getAccessCount()
        );
    }
}
