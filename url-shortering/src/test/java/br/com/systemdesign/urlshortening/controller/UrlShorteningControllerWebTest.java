package br.com.systemdesign.urlshortening.controller;

import br.com.systemdesign.urlshortening.dto.CreateShortUrlRequest;
import br.com.systemdesign.urlshortening.dto.UrlResponse;
import br.com.systemdesign.urlshortening.service.UrlShorteningService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class UrlShorteningControllerWebTest {
    @Mock
    private UrlShorteningService service;

    @InjectMocks
    private UrlShorteningController controller;

    @Test
    void shouldCreateShortUrl() {
        UrlResponse response = new UrlResponse(
                1L,
                "abc123",
                "https://example.com",
                "http://localhost/shortening/api/shortener/abc123",
                LocalDateTime.now(),
                null,
                0L
        );
        when(service.createShortUrl(any(CreateShortUrlRequest.class))).thenReturn(response);

        CreateShortUrlRequest request = new CreateShortUrlRequest("https://example.com", 3600L);
        ResponseEntity<UrlResponse> result = controller.createShortUrl(request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().shortCode()).isEqualTo("abc123");
    }

    @Test
    void shouldRedirectToOriginalUrl() {
        when(service.redirectToOriginalUrl("abc123")).thenReturn("https://example.com/page");

        ResponseEntity<Void> result = controller.redirectToOriginalUrl("abc123");

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.FOUND);
        assertThat(result.getHeaders().getLocation()).hasToString("https://example.com/page");
    }

    @Test
    void shouldDeleteUrl() {
        ResponseEntity<Void> result = controller.deleteUrl("abc123");

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(service).deleteUrl("abc123");
    }
}
