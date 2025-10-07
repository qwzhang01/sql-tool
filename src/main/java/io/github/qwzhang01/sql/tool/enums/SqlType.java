package io.github.qwzhang01.sql.tool.enums;

/**
 * Enumeration defining different types of SQL statements.
 * This classification helps in parsing, validating, and processing SQL statements
 * based on their operation type and intended purpose.
 *
 * @author avinzhang
 */
public enum SqlType {
    /**
     * SELECT statement for data retrieval and querying.
     * Used to fetch data from one or more tables with optional filtering, sorting, and grouping.
     * Example: SELECT name, age FROM users WHERE age > 18 ORDER BY name
     */
    SELECT,

    /**
     * INSERT statement for data insertion.
     * Used to add new records to a table with specified column values.
     * Example: INSERT INTO users (name, age, email) VALUES ('John', 25, 'john@example.com')
     */
    INSERT,

    /**
     * UPDATE statement for data modification.
     * Used to modify existing records in a table based on specified conditions.
     * Example: UPDATE users SET age = 26 WHERE name = 'John'
     */
    UPDATE,

    /**
     * DELETE statement for data removal.
     * Used to remove records from a table based on specified conditions.
     * Example: DELETE FROM users WHERE age < 18
     */
    DELETE,

    /**
     * CREATE statement for schema creation.
     * Used to create database objects like tables, indexes, views, or databases.
     * Example: CREATE TABLE users (id INT PRIMARY KEY, name VARCHAR(100))
     */
    CREATE,

    /**
     * DROP statement for schema removal.
     * Used to permanently remove database objects like tables, indexes, or databases.
     * Example: DROP TABLE users
     */
    DROP,

    /**
     * ALTER statement for schema modification.
     * Used to modify the structure of existing database objects.
     * Example: ALTER TABLE users ADD COLUMN email VARCHAR(255)
     */
    ALTER,

    /**
     * TRUNCATE statement for table truncation.
     * Used to quickly remove all records from a table while preserving its structure.
     * More efficient than DELETE for removing all rows.
     * Example: TRUNCATE TABLE users
     */
    TRUNCATE
}
