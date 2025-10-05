package io.github.qwzhang01.sql.tool.model;

import io.github.qwzhang01.sql.tool.enums.Direction;

/**
 * ORDER BY clause information representing sorting specifications.
 * This class contains details about column sorting including direction,
 * table references, and position in the ORDER BY clause.
 *
 * @author Avin Zhang
 * @since 1.0.0
 */
public class SqlOrderBy {

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
    public SqlOrderBy() {
        this.direction = Direction.ASC; // Default to ascending
    }

    /**
     * Constructor with column name, defaults to ascending sort
     *
     * @param columnName the column to sort by
     */
    public SqlOrderBy(String columnName) {
        this.columnName = columnName;
        this.direction = Direction.ASC;
    }

    /**
     * Constructor with column name and sort direction
     *
     * @param columnName the column to sort by
     * @param direction  the sort direction
     */
    public SqlOrderBy(String columnName, Direction direction) {
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
    public SqlOrderBy(String columnName, String tableName, Direction direction) {
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

}