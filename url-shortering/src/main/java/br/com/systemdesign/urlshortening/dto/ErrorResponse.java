package br.com.systemdesign.urlshortening.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public record ErrorResponse(
        int status,
        String message,
        String error,
        LocalDateTime timestamp,
        String path
) { }

