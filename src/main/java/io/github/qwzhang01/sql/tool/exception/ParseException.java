package io.github.qwzhang01.sql.tool.exception;

/**
 * SQL parsing exception thrown when SQL statement cannot be parsed correctly.
 * This exception indicates that the provided SQL statement contains syntax errors,
 * unsupported constructs, or other parsing issues that prevent successful analysis.
 *
 * @author Avin Zhang
 * @since 1.0.0
 */
public class ParseException extends RuntimeException {

    /**
     * Constructs a new parse exception with the specified detail message
     *
     * @param message the detail message explaining the parsing error
     */
    public ParseException(String message) {
        super(message);
    }

    /**
     * Constructs a new parse exception with the specified detail message and cause
     *
     * @param message the detail message explaining the parsing error
     * @param cause   the cause of the parsing error
     */
    public ParseException(String message, Throwable cause) {
        super(message, cause);
    }
}