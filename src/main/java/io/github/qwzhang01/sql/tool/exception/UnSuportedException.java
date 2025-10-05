package io.github.qwzhang01.sql.tool.exception;

/**
 * Unsupported operation exception thrown when attempting to use features
 * that are not yet implemented or supported by the current parser implementation.
 * This exception extends ParseException and indicates that while the SQL syntax
 * may be valid, the specific feature is not supported by this version of the parser.
 *
 * @author Avin Zhang
 * @since 1.0.0
 */
public class UnSuportedException extends ParseException {

    /**
     * Constructs a new unsupported operation exception with the specified detail message
     *
     * @param message the detail message explaining what feature is not supported
     */
    public UnSuportedException(String message) {
        super(message);
    }

    /**
     * Constructs a new unsupported operation exception with the specified detail message and cause
     *
     * @param message the detail message explaining what feature is not supported
     * @param cause   the cause of the exception
     */
    public UnSuportedException(String message, Throwable cause) {
        super(message, cause);
    }
}