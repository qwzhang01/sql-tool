package io.github.qwzhang01.sql.tool.enums;

/**
 * Enumeration defining different types of tables based on their role in SQL queries.
 * This classification helps in query parsing and optimization by identifying
 * how tables are used within the query structure.
 *
 * @author avinzhang
 */
public enum TableType {
    /**
     * Main table - the primary table in a SQL statement.
     * This is typically the table specified in the FROM clause of SELECT statements,
     * or the target table in INSERT, UPDATE, or DELETE operations.
     * Example: SELECT * FROM users (users is the main table)
     */
    MAIN,

    /**
     * JOIN table - a table that is joined with other tables in the query.
     * These tables are specified in JOIN clauses and are used to combine
     * data from multiple tables based on related columns.
     * Example: SELECT * FROM users u JOIN orders o ON u.id = o.user_id (orders is a join table)
     */
    JOIN,

    /**
     * SUBQUERY table - a table that appears within a subquery or derived table.
     * These are nested SELECT statements that act as temporary tables
     * within the main query context.
     * Example: SELECT * FROM (SELECT * FROM users WHERE age > 18) AS adult_users (the subquery acts as a table)
     */
    SUBQUERY
}