package io.github.qwzhang01.sql.tool.model;

import java.util.List;
import java.util.Map;

/**
 * SQL information object containing detailed information after SQL parsing.
 * This class serves as the main container for all parsed SQL components including
 * tables, columns, conditions, and other SQL elements.
 *
 * @author Avin Zhang
 * @since 1.0.0
 */
public class SqlInfo {

    /**
     * Original SQL statement as provided by the user
     */
    private String originalSql;

    /**
     * Type of SQL statement (SELECT, INSERT, UPDATE, DELETE, etc.)
     */
    private SqlType sqlType;

    /**
     * Main table information for the SQL statement
     */
    private TableInfo mainTable;

    /**
     * List of joined table information (used for JOIN queries)
     */
    private List<JoinInfo> joinTables;

    /**
     * List of selected columns in SELECT statements
     */
    private List<ColumnInfo> selectColumns;

    /**
     * List of WHERE conditions with detailed field analysis
     */
    private List<WhereCondition> whereConditions;

    /**
     * List of GROUP BY columns
     */
    private List<String> groupByColumns;

    /**
     * HAVING condition clause
     */
    private String havingCondition;

    /**
     * List of ORDER BY information including sort direction
     */
    private List<OrderByInfo> orderByColumns;

    /**
     * LIMIT information including offset and row count
     */
    private LimitInfo limitInfo;

    /**
     * Parameter mapping (parameter name -> parameter value) for prepared statements
     */
    private Map<String, Object> parameterMap;

    /**
     * List of subquery information for nested queries
     */
    private List<SqlInfo> subQueries;

    /**
     * Column-value mapping for INSERT/UPDATE statements
     */
    private Map<String, Object> columnValues;

    /**
     * List of column names for INSERT statements
     */
    private List<String> insertColumns;

    /**
     * List of values for INSERT statements
     */
    private List<Object> insertValues;

    /**
     * Update value mapping for UPDATE statements
     */
    private Map<String, Object> updateValues;

    /**
     * Default constructor
     */
    public SqlInfo() {
    }

    /**
     * Constructor with original SQL and SQL type
     *
     * @param originalSql the original SQL statement
     * @param sqlType     the type of SQL statement
     */
    public SqlInfo(String originalSql, SqlType sqlType) {
        this.originalSql = originalSql;
        this.sqlType = sqlType;
    }

    // Getter and Setter methods
    public String getOriginalSql() {
        return originalSql;
    }

    public void setOriginalSql(String originalSql) {
        this.originalSql = originalSql;
    }

    public SqlType getSqlType() {
        return sqlType;
    }

    public void setSqlType(SqlType sqlType) {
        this.sqlType = sqlType;
    }

    public TableInfo getMainTable() {
        return mainTable;
    }

    public void setMainTable(TableInfo mainTable) {
        this.mainTable = mainTable;
    }

    public List<JoinInfo> getJoinTables() {
        return joinTables;
    }

    public void setJoinTables(List<JoinInfo> joinTables) {
        this.joinTables = joinTables;
    }

    public List<ColumnInfo> getSelectColumns() {
        return selectColumns;
    }

    public void setSelectColumns(List<ColumnInfo> selectColumns) {
        this.selectColumns = selectColumns;
    }

    public List<WhereCondition> getWhereConditions() {
        return whereConditions;
    }

    public void setWhereConditions(List<WhereCondition> whereConditions) {
        this.whereConditions = whereConditions;
    }

    public List<String> getGroupByColumns() {
        return groupByColumns;
    }

    public void setGroupByColumns(List<String> groupByColumns) {
        this.groupByColumns = groupByColumns;
    }

    public String getHavingCondition() {
        return havingCondition;
    }

    public void setHavingCondition(String havingCondition) {
        this.havingCondition = havingCondition;
    }

    public List<OrderByInfo> getOrderByColumns() {
        return orderByColumns;
    }

    public void setOrderByColumns(List<OrderByInfo> orderByColumns) {
        this.orderByColumns = orderByColumns;
    }

    public LimitInfo getLimitInfo() {
        return limitInfo;
    }

    public void setLimitInfo(LimitInfo limitInfo) {
        this.limitInfo = limitInfo;
    }

    public Map<String, Object> getParameterMap() {
        return parameterMap;
    }

    public void setParameterMap(Map<String, Object> parameterMap) {
        this.parameterMap = parameterMap;
    }

    public List<SqlInfo> getSubQueries() {
        return subQueries;
    }

    public void setSubQueries(List<SqlInfo> subQueries) {
        this.subQueries = subQueries;
    }

    public Map<String, Object> getColumnValues() {
        return columnValues;
    }

    public void setColumnValues(Map<String, Object> columnValues) {
        this.columnValues = columnValues;
    }

    public List<String> getInsertColumns() {
        return insertColumns;
    }

    public void setInsertColumns(List<String> insertColumns) {
        this.insertColumns = insertColumns;
    }

    public List<Object> getInsertValues() {
        return insertValues;
    }

    public void setInsertValues(List<Object> insertValues) {
        this.insertValues = insertValues;
    }

    public Map<String, Object> getUpdateValues() {
        return updateValues;
    }

    public void setUpdateValues(Map<String, Object> updateValues) {
        this.updateValues = updateValues;
    }

    @Override
    public String toString() {
        return "SqlInfo{" +
                "originalSql='" + originalSql + '\'' +
                ", sqlType=" + sqlType +
                ", mainTable=" + mainTable +
                ", joinTables=" + joinTables +
                ", selectColumns=" + selectColumns +
                ", whereConditions=" + whereConditions +
                ", groupByColumns=" + groupByColumns +
                ", havingCondition=" + havingCondition +
                ", orderByColumns=" + orderByColumns +
                ", limitInfo=" + limitInfo +
                ", parameterMap=" + parameterMap +
                ", subQueries=" + subQueries +
                ", columnValues=" + columnValues +
                ", insertColumns=" + insertColumns +
                ", insertValues=" + insertValues +
                ", updateValues=" + updateValues +
                '}';
    }

    /**
     * SQL statement type enumeration.
     * Defines the different types of SQL statements that can be parsed.
     */
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
}