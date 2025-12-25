package io.github.qwzhang01.sql.tool.exception;


/**
 * Exception thrown when SQL is illegal and cannot be parsed or converted.
 * This exception extends ParseException and includes the problematic SQL string
 * for debugging and error reporting purposes.
 *
 * @author Avin Zhang
 * @since 1.0.0
 */
public class SqlIllegalException extends ParseException {
    /**
     * The illegal SQL statement that caused this exception
     */
    private String sql;

    /**
     * Constructs a new illegal SQL exception with the specified detail message, cause, and SQL
     *
     * @param message the detail message explaining the illegal SQL
     * @param cause   the cause of the exception
     * @param sql     the illegal SQL statement that caused this exception
     */
    public SqlIllegalException(String message, Throwable cause, String sql) {
        super(message + ": " + sql, cause);
        this.sql = sql;
    }

    /**
     * Gets the illegal SQL statement that caused this exception
     *
     * @return the illegal SQL string
     */
    public String getSql() {
        return sql;
    }

    /**
     * Sets the illegal SQL statement
     *
     * @param sql the illegal SQL string to set
     */
    public void setSql(String sql) {
        this.sql = sql;
    }
}