package br.com.systemdesign.urlshortening.entity;

import br.com.systemdesign.urlshortening.util.ShortUrlGenerator;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "shortened_urls", indexes = {
    @Index(name = "idx_short_url", columnList = "short_url", unique = true),
    @Index(name = "idx_expires_at", columnList = "expires_at"),
    @Index(name = "idx_created_at", columnList = "created_at")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ShortenedUrl {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "short_url", nullable = false, unique = true)
    private String shortUrl;

    @Column(name = "original_url", nullable = false, length = 2048)
    private String originalUrl;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "access_count", nullable = false)
    private Long accessCount = 0L;

    public ShortenedUrl(String originalUrl, LocalDateTime expiresAt) {
        this.originalUrl = originalUrl;
        this.expiresAt = expiresAt;
        this.accessCount = 0L;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (shortUrl == null) {
            shortUrl = ShortUrlGenerator.generate();
        }
    }

    /**
     * Increments access count
     */
    public void incrementAccessCount() {
        this.accessCount++;
    }

    /**
     * Checks if URL is expired
     */
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
}
