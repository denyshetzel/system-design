package br.com.systemdesign.urlshortening.util;

import lombok.experimental.UtilityClass;

import java.time.LocalDateTime;

@UtilityClass
public final class Validations {

    public static LocalDateTime calculateExpiration(Long expirationSeconds) {
        return (expirationSeconds != null && expirationSeconds > 0)
                ? LocalDateTime.now().plusSeconds(expirationSeconds)
                : null;
    }

}
