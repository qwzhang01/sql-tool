package io.github.qwzhang01.sql.tool.enums;

/**
 * Field type enumeration.
 */
public enum FieldType {
    SELECT,     // SELECT field
    INSERT,     // INSERT field
    UPDATE_SET, // UPDATE SET field
    CONDITION   // WHERE/HAVING condition field
}
