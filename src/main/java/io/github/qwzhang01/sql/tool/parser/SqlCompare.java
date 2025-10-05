package io.github.qwzhang01.sql.tool.parser;

/**
 * SQL Comparison interface for database-specific field and identifier equality checks.
 * This interface provides methods to compare SQL identifiers according to
 * database-specific rules such as case sensitivity and identifier quoting.
 *
 * <p>Different databases have different comparison rules:
 * <ul>
 * <li>MySQL: Case-insensitive by default, supports backtick quoting</li>
 * <li>PostgreSQL: Case-sensitive, supports double-quote quoting</li>
 * <li>SQL Server: Case-insensitive by default, supports bracket quoting</li>
 * <li>Oracle: Case-insensitive by default, converts to uppercase</li>
 * </ul>
 *
 * <p>Currently implemented for MySQL. Future versions will support
 * SQL Server, Oracle, PostgreSQL, and other major database systems.
 *
 * @author Avin Zhang
 * @since 1.0.0
 */
public interface SqlCompare {

    /**
     * Compares two SQL identifiers for equality according to database-specific rules
     *
     * @param mark1 the first SQL identifier to compare
     * @param mark2 the second SQL identifier to compare
     * @return true if the identifiers are considered equal by database rules
     */
    boolean equal(String mark1, String mark2);
}