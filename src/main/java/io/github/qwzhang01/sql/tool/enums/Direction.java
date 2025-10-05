package io.github.qwzhang01.sql.tool.enums;


/**
 * Sort direction enumeration for ORDER BY clauses.
 * Defines ascending and descending sort orders.
 *
 * @author avinzhang
 */
public enum Direction {
    /**
     * Ascending sort order (default)
     */
    ASC("ASC"),
    /**
     * Descending sort order
     */
    DESC("DESC");

    private final String sqlKeyword;

    /**
     * Constructor for Direction with SQL keyword
     *
     * @param sqlKeyword the SQL keyword representation
     */
    Direction(String sqlKeyword) {
        this.sqlKeyword = sqlKeyword;
    }

    /**
     * Creates a Direction from string representation
     *
     * @param directionStr the string representation of direction
     * @return the corresponding Direction, defaults to ASC if not found
     */
    public static Direction fromString(String directionStr) {
        if (directionStr == null) return ASC;

        String normalized = directionStr.toUpperCase().trim();
        for (Direction dir : values()) {
            if (dir.getSqlKeyword().equals(normalized) || dir.name().equals(normalized)) {
                return dir;
            }
        }
        return ASC; // Default to ascending
    }

    /**
     * Gets the SQL keyword for this direction
     *
     * @return the SQL keyword string
     */
    public String getSqlKeyword() {
        return sqlKeyword;
    }
}
