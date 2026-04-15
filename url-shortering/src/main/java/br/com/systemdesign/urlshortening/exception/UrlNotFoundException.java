package br.com.systemdesign.urlshortening.exception;

public class UrlNotFoundException extends RuntimeException {
    
    public UrlNotFoundException(String message) {
        super(message);
    }

    public UrlNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

}

