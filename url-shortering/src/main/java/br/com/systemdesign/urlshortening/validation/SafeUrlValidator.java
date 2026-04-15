package br.com.systemdesign.urlshortening.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.net.URI;
import java.util.regex.Pattern;

public class SafeUrlValidator implements ConstraintValidator<SafeUrl, String> {

    private static final int MAX_LENGTH = 2000;
    private static final Pattern CONTROL_CHARS = Pattern.compile("[\\p{Cntrl}]");

    @Override
    public void initialize(SafeUrl constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(String url, ConstraintValidatorContext context) {
        if (url == null || url.trim().isEmpty())  return true;
        if (url.length() > MAX_LENGTH) return false;
        if (CONTROL_CHARS.matcher(url).find()) return false; // blocks CR/LF injection etc.
        try {
            var uri = URI.create(url.trim());
            var scheme = uri.getScheme();
            return scheme != null && (scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https")) && uri.getHost() != null;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

}
