package br.com.systemdesign.urlshortening.service;

import br.com.systemdesign.urlshortening.application.port.out.ShortenedLinkStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UrlCleanupServiceTest {

    @Mock
    private ShortenedLinkStore store;

    @InjectMocks
    private UrlCleanupService service;

    @Test
    void shouldDeleteExpiredUrls() {
        when(store.deleteExpiredBefore(any(LocalDateTime.class))).thenReturn(3L);

        service.cleanupExpiredUrls();

        verify(store, times(1)).deleteExpiredBefore(any(LocalDateTime.class));
    }
}
