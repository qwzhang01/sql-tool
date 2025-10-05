package io.github.qwzhang01.sql.tool.model;

/**
 * Column information class representing database column metadata and SQL column references.
 * This class stores comprehensive information about columns including names, types,
 * constraints, and relationships to tables.
 *
 * @author Avin Zhang
 * @since 1.0.0
 */
public class ColumnInfo {

    /**
     * The column name as it appears in the database or SQL statement
     */
    private String columnName;

    /**
     * The table name that contains this column
     */
    private String tableName;

    /**
     * The table alias used in the SQL statement
     */
    private String tableAlias;

    /**
     * The column alias assigned in SELECT statements
     */
    private String alias;

    /**
     * The data type of the column (VARCHAR, INT, etc.)
     */
    private String dataType;

    /**
     * Whether this column is part of the primary key
     */
    private boolean isPrimaryKey;

    /**
     * Whether this column allows NULL values
     */
    private boolean nullable;

    /**
     * The default value for this column
     */
    private String defaultValue;

    // Constructors

    /**
     * Default constructor
     */
    public ColumnInfo() {
    }

    /**
     * Constructor with column name only
     *
     * @param columnName the name of the column
     */
    public ColumnInfo(String columnName) {
        this.columnName = columnName;
    }

    /**
     * Constructor with column name and table name
     *
     * @param columnName the name of the column
     * @param tableName  the name of the table
     */
    public ColumnInfo(String columnName, String tableName) {
        this.columnName = columnName;
        this.tableName = tableName;
    }

    /**
     * Constructor with column name, table name, and alias
     *
     * @param columnName the name of the column
     * @param tableName  the name of the table
     * @param alias      the column alias
     */
    public ColumnInfo(String columnName, String tableName, String alias) {
        this.columnName = columnName;
        this.tableName = tableName;
        this.alias = alias;
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

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public boolean isPrimaryKey() {
        return isPrimaryKey;
    }

    public void setPrimaryKey(boolean primaryKey) {
        isPrimaryKey = primaryKey;
    }

    public boolean isNullable() {
        return nullable;
    }

    public void setNullable(boolean nullable) {
        this.nullable = nullable;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Override
    public String toString() {
        return "ColumnInfo{" +
                "columnName='" + columnName + '\'' +
                ", tableName='" + tableName + '\'' +
                ", tableAlias='" + tableAlias + '\'' +
                ", alias='" + alias + '\'' +
                ", dataType='" + dataType + '\'' +
                ", isPrimaryKey=" + isPrimaryKey +
                ", nullable=" + nullable +
                ", defaultValue='" + defaultValue + '\'' +
                '}';
    }
}