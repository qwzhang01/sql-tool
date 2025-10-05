package io.github.qwzhang01.sql.tool.enums;

/**
 * JOIN type enumeration defining all supported JOIN operations.
 * Each type corresponds to a specific SQL JOIN syntax and behavior.
 *
 * @author avinzhang
 */
public enum JoinType {
    /**
     * INNER JOIN - returns only matching rows from both tables
     */
    INNER_JOIN("INNER JOIN"),
    /**
     * LEFT JOIN - returns all rows from left table and matching rows from right table
     */
    LEFT_JOIN("LEFT JOIN"),
    /**
     * RIGHT JOIN - returns all rows from right table and matching rows from left table
     */
    RIGHT_JOIN("RIGHT JOIN"),
    /**
     * FULL JOIN - returns all rows from both tables
     */
    FULL_JOIN("FULL JOIN"),
    /**
     * CROSS JOIN - returns Cartesian product of both tables
     */
    CROSS_JOIN("CROSS JOIN"),
    /**
     * LEFT OUTER JOIN - alias for LEFT JOIN
     */
    LEFT_OUTER_JOIN("LEFT OUTER JOIN"),
    /**
     * RIGHT OUTER JOIN - alias for RIGHT JOIN
     */
    RIGHT_OUTER_JOIN("RIGHT OUTER JOIN"),
    /**
     * FULL OUTER JOIN - alias for FULL JOIN
     */
    FULL_OUTER_JOIN("FULL OUTER JOIN");

    private final String sqlKeyword;

    /**
     * Constructor for JOIN type with SQL keyword
     *
     * @param sqlKeyword the SQL keyword representation
     */
    JoinType(String sqlKeyword) {
        this.sqlKeyword = sqlKeyword;
    }

    /**
     * Creates a JoinType from string representation
     *
     * @param joinTypeStr the string representation of join type
     * @return the corresponding JoinType or null if not found
     */
    public static JoinType fromString(String joinTypeStr) {
        if (joinTypeStr == null) {
            return null;
        }

        String normalized = joinTypeStr.toUpperCase().trim();
        for (JoinType type : values()) {
            if (type.getSqlKeyword().equals(normalized) ||
                    type.name().equals(normalized.replace(" ", "_"))) {
                return type;
            }
        }
        return null;
    }

    /**
     * Gets the SQL keyword for this join type
     *
     * @return the SQL keyword string
     */
    public String getSqlKeyword() {
        return sqlKeyword;
    }
}