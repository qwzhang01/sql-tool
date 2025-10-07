package io.github.qwzhang01.sql.tool.enums;

/**
 * Enumeration defining the position where SQL conditions can appear.
 * This helps categorize conditions based on their context within SQL statements.
 * <p>
 * Supported positions: WHERE clause, JOIN conditions, INSERT values, and UPDATE SET clauses.
 *
 * @author avinzhang
 */
public enum ConditionPosition {
    /**
     * WHERE clause condition - filters rows in SELECT, UPDATE, or DELETE statements
     * Example: SELECT * FROM users WHERE age > 18
     */
    WHERE,

    /**
     * JOIN condition - specifies how tables are related in JOIN operations
     * Example: SELECT * FROM users u JOIN orders o ON u.id = o.user_id
     */
    JOIN,

    /**
     * INSERT condition - used in INSERT statements with conditional logic
     * Example: INSERT INTO table (col1, col2) VALUES (?, ?) WHERE condition
     */
    INSERT,

    /**
     * SET clause condition - used in UPDATE statements to set column values
     * Example: UPDATE users SET status = 'active' WHERE id = ?
     */
    SET
}