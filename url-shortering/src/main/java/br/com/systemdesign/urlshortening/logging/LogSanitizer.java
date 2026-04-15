package br.com.systemdesign.urlshortening.logging;

/**
 * Removes control characters and trims overly long inputs before logging.
 */
public final class LogSanitizer {

    private static final int MAX_LENGTH = 512; // keep logs concise

    private LogSanitizer() {}

    public static String sanitize(String input) {
        if (input == null) return null;
        // Remove control chars and trim
        String sanitized = input.replaceAll("\\p{Cntrl}", "").trim();
        // Neutralize common injection vectors for log viewers / consoles
        sanitized = sanitized
                .replace("<", "[")
                .replace(">", "]")
                .replace("`", "'")
                .replace("${", "\\${")
                .replace("%{", "\\%{");
        if (sanitized.length() > MAX_LENGTH) {
            sanitized = sanitized.substring(0, MAX_LENGTH) + "...";
        }
        return sanitized;
    }
}
