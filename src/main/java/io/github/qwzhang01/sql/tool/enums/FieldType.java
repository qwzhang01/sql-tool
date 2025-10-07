package io.github.qwzhang01.sql.tool.enums;

/**
 * Enumeration defining different types of fields based on their usage context in SQL statements.
 * This classification helps in processing and validating fields according to their specific roles.
 *
 * @author avinzhang
 */
public enum FieldType {
    /**
     * SELECT field - columns specified in SELECT clause for data retrieval
     * Example: SELECT name, age, email FROM users
     */
    SELECT,

    /**
     * INSERT field - columns specified in INSERT statements for data insertion
     * Example: INSERT INTO users (name, age, email) VALUES (?, ?, ?)
     */
    INSERT,

    /**
     * UPDATE SET field - columns specified in UPDATE SET clause for data modification
     * Example: UPDATE users SET name = ?, age = ? WHERE id = ?
     */
    UPDATE_SET,

    /**
     * CONDITION field - fields used in WHERE, HAVING, or JOIN conditions
     * Example: WHERE age > 18 AND status = 'active'
     */
    CONDITION_WHERE,
    /**
     * JOIN field - fields used in JOIN conditions
     */
    CONDITION_JOIN
}
