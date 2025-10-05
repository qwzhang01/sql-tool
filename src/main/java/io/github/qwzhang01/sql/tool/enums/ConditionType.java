package io.github.qwzhang01.sql.tool.enums;

/**
 * Condition type enumeration for categorizing different types of WHERE conditions.
 * This helps in processing and optimizing different condition patterns.
 */
public enum ConditionType {
    /**
     * Simple condition: field = value
     */
    SIMPLE,
    /**
     * Complex condition: contains sub-conditions with parentheses
     */
    COMPLEX,
    /**
     * IN condition: field IN (value1, value2, ...)
     */
    IN,
    /**
     * BETWEEN condition: field BETWEEN value1 AND value2
     */
    BETWEEN,
    /**
     * LIKE condition: field LIKE pattern
     */
    LIKE,
    /**
     * EXISTS condition: EXISTS (subquery)
     */
    EXISTS,
    /**
     * IS NULL condition: field IS NULL
     */
    IS_NULL,
    /**
     * IS NOT NULL condition: field IS NOT NULL
     */
    IS_NOT_NULL
}