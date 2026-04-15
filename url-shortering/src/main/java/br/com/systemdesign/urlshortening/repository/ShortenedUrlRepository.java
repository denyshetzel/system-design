package br.com.systemdesign.urlshortening.repository;

import br.com.systemdesign.urlshortening.entity.ShortenedUrl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShortenedUrlRepository extends JpaRepository<ShortenedUrl, Long> {
    Optional<ShortenedUrl> findByShortUrl(String shortUrl);
    List<ShortenedUrl> findByExpiresAtBefore(LocalDateTime dateTime);
}
