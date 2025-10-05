package io.github.qwzhang01.sql.tool.enums;

public enum SqlType {
    /**
     * SELECT statement for data retrieval
     */
    SELECT,
    /**
     * INSERT statement for data insertion
     */
    INSERT,
    /**
     * UPDATE statement for data modification
     */
    UPDATE,
    /**
     * DELETE statement for data removal
     */
    DELETE,
    /**
     * CREATE statement for schema creation
     */
    CREATE,
    /**
     * DROP statement for schema removal
     */
    DROP,
    /**
     * ALTER statement for schema modification
     */
    ALTER,
    /**
     * TRUNCATE statement for table truncation
     */
    TRUNCATE
}
