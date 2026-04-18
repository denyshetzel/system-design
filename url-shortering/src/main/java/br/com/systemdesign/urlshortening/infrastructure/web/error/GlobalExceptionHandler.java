package br.com.systemdesign.urlshortening.infrastructure.web.error;

import br.com.systemdesign.urlshortening.exception.RateLimitExceededException;
import br.com.systemdesign.urlshortening.exception.UrlExpiredException;
import br.com.systemdesign.urlshortening.exception.UrlNotFoundException;
import br.com.systemdesign.urlshortening.infrastructure.web.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Collectors;

@ControllerAdvice
@RequiredArgsConstructor
@CustomLog
public class GlobalExceptionHandler {

    private static final String REQUEST_ID_HEADER = "X-Request-Id";

    private final MessageSource messageSource;

    @ExceptionHandler(UrlExpiredException.class)
    public ResponseEntity<ErrorResponse> handleUrlExpiredException(
            UrlExpiredException ex,
            HttpServletRequest request) {
        log.warn("URL expired exception: {}", ex.getMessage(), ex);
        String message = messageSource.getMessage("error.url.expired", null, LocaleContextHolder.getLocale());
        return buildErrorResponse(HttpStatus.GONE, message, "URL_EXPIRED", request);
    }

    @ExceptionHandler(UrlNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUrlNotFoundException(
            UrlNotFoundException ex,
            HttpServletRequest request) {
        log.warn("URL not found exception: {}", ex.getMessage(), ex);
        String message = messageSource.getMessage("error.url.not.found", null, LocaleContextHolder.getLocale());
        return buildErrorResponse(HttpStatus.NOT_FOUND, message, "URL_NOT_FOUND", request);
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleRateLimitExceeded(
            RateLimitExceededException ex,
            HttpServletRequest request) {
        log.warn("Rate limit exceeded for request: {}", request.getRequestURI());
        return buildErrorResponse(
                HttpStatus.TOO_MANY_REQUESTS,
                ex.getMessage(),
                "RATE_LIMIT_EXCEEDED",
                request,
                ex.getRetryAfterSeconds()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::buildFieldErrorMessage)
                .collect(Collectors.joining("; "));

        return buildErrorResponse(HttpStatus.BAD_REQUEST, message, "VALIDATION_ERROR", request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request) {
        String message = ex.getConstraintViolations()
                .stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.joining("; "));

        return buildErrorResponse(HttpStatus.BAD_REQUEST, message, "VALIDATION_ERROR", request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Invalid request body", "INVALID_REQUEST_BODY", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception ex,
            HttpServletRequest request) {
        log.error("Unexpected exception", ex);
        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred",
                "INTERNAL_SERVER_ERROR",
                request
        );
    }

    private String buildFieldErrorMessage(FieldError error) {
        return error.getField() + ": " + error.getDefaultMessage();
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(
            HttpStatus status,
            String message,
            String code,
            HttpServletRequest request) {
        return buildErrorResponse(status, message, code, request, null);
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(
            HttpStatus status,
            String message,
            String code,
            HttpServletRequest request,
            Long retryAfterSeconds) {
        String requestId = resolveRequestId(request);
        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(status.value())
                .message(message)
                .error(code)
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .requestId(requestId)
                .build();

        ResponseEntity.BodyBuilder builder = ResponseEntity.status(status)
                .header(REQUEST_ID_HEADER, requestId);
        if (retryAfterSeconds != null) {
            builder.header("Retry-After", String.valueOf(retryAfterSeconds));
        }
        return builder.body(errorResponse);
    }

    private String resolveRequestId(HttpServletRequest request) {
        String requestId = request.getHeader(REQUEST_ID_HEADER);
        if (requestId == null || requestId.isBlank()) {
            return UUID.randomUUID().toString();
        }
        return requestId;
    }
}
