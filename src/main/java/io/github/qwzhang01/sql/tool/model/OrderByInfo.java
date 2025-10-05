package io.github.qwzhang01.sql.tool.model;

/**
 * ORDER BY clause information representing sorting specifications.
 * This class contains details about column sorting including direction,
 * table references, and position in the ORDER BY clause.
 *
 * @author Avin Zhang
 * @since 1.0.0
 */
public class OrderByInfo {

    /**
     * The column name to sort by
     */
    private String columnName;

    /**
     * The table name containing the sort column
     */
    private String tableName;

    /**
     * The table alias used in the SQL statement
     */
    private String tableAlias;

    /**
     * The sort direction (ASC or DESC)
     */
    private Direction direction;

    /**
     * The position of this sort column in the ORDER BY clause (1-based)
     */
    private int position;

    // Constructors

    /**
     * Default constructor with ascending sort direction
     */
    public OrderByInfo() {
        this.direction = Direction.ASC; // Default to ascending
    }

    /**
     * Constructor with column name, defaults to ascending sort
     *
     * @param columnName the column to sort by
     */
    public OrderByInfo(String columnName) {
        this.columnName = columnName;
        this.direction = Direction.ASC;
    }

    /**
     * Constructor with column name and sort direction
     *
     * @param columnName the column to sort by
     * @param direction  the sort direction
     */
    public OrderByInfo(String columnName, Direction direction) {
        this.columnName = columnName;
        this.direction = direction;
    }

    /**
     * Constructor with column name, table name, and sort direction
     *
     * @param columnName the column to sort by
     * @param tableName  the table containing the column
     * @param direction  the sort direction
     */
    public OrderByInfo(String columnName, String tableName, Direction direction) {
        this.columnName = columnName;
        this.tableName = tableName;
        this.direction = direction;
    }

    // Getter and Setter methods
    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getTableAlias() {
        return tableAlias;
    }

    public void setTableAlias(String tableAlias) {
        this.tableAlias = tableAlias;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    @Override
    public String toString() {
        return "OrderByInfo{" +
                "columnName='" + columnName + '\'' +
                ", tableName='" + tableName + '\'' +
                ", tableAlias='" + tableAlias + '\'' +
                ", direction=" + direction +
                ", position=" + position +
                '}';
    }

    /**
     * Sort direction enumeration for ORDER BY clauses.
     * Defines ascending and descending sort orders.
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
}