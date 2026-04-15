package br.com.systemdesign.urlshortening.exception;

import br.com.systemdesign.urlshortening.dto.ErrorResponse;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;

@ControllerAdvice
@RequiredArgsConstructor
@CustomLog
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    @ExceptionHandler(UrlExpiredException.class)
    public ResponseEntity<ErrorResponse> handleUrlExpiredException(
            UrlExpiredException ex,
            WebRequest request) {
        
        log.warn("URL expired exception: {}", ex.getMessage(), ex);
        
        String message = messageSource.getMessage("error.url.expired", null, LocaleContextHolder.getLocale());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.GONE.value())
                .message(message)
                .error("URL_EXPIRED")
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.GONE);
    }

    @ExceptionHandler(UrlNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUrlNotFoundException(
            UrlNotFoundException ex,
            WebRequest request) {
        
        log.warn("URL not found exception: {}", ex.getMessage(), ex);
        
        String message = messageSource.getMessage("error.url.not.found", null, LocaleContextHolder.getLocale());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.NOT_FOUND.value())
                .message(message)
                .error("URL_NOT_FOUND")
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception ex,
            WebRequest request) {
        
        log.error("Unexpected exception: {}", ex);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message("An unexpected error occurred")
                .error("INTERNAL_SERVER_ERROR")
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

