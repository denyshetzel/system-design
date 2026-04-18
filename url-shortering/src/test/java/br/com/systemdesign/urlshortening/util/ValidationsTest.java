package br.com.systemdesign.urlshortening.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ValidationsTest {

    @Test
    void shouldReturnNullWhenExpirationIsMissing() {
        assertThat(Validations.calculateExpiration(null)).isNull();
    }

    @Test
    void shouldReturnNullWhenExpirationIsNonPositive() {
        assertThat(Validations.calculateExpiration(0L)).isNull();
        assertThat(Validations.calculateExpiration(-1L)).isNull();
    }

    @Test
    void shouldCalculateExpirationWhenPositiveValueIsProvided() {
        LocalDateTime before = LocalDateTime.now();
        LocalDateTime result = Validations.calculateExpiration(60L);
        LocalDateTime after = LocalDateTime.now();

        assertThat(result).isAfter(before.plusSeconds(59));
        assertThat(result).isBefore(after.plusSeconds(61));
    }
}

