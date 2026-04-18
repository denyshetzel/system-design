package br.com.systemdesign.urlshortening.repository;

import br.com.systemdesign.urlshortening.entity.ShortenedUrl;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface ShortenedUrlRepository extends JpaRepository<ShortenedUrl, Long> {
    Optional<ShortenedUrl> findByShortUrl(String shortUrl);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update ShortenedUrl s set s.accessCount = s.accessCount + 1 where s.id = :id")
    int incrementAccessCountById(@Param("id") Long id);

    long deleteByExpiresAtBefore(LocalDateTime dateTime);
}
