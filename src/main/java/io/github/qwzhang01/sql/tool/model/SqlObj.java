package io.github.qwzhang01.sql.tool.model;

import io.github.qwzhang01.sql.tool.enums.SqlType;

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
public class SqlObj {

    /**
     * 格式化以后的sql
     * <p>
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
    private SqlTable mainTable;

    /**
     * List of joined table information (used for JOIN queries)
     */
    private List<SqlJoin> joinTables;

    /**
     * List of selected columns in SELECT statements
     */
    private List<SqlField> selectColumns;

    /**
     * List of WHERE conditions with detailed field analysis
     */
    private List<SqlCondition> whereConditions;

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
    private List<SqlOrderBy> orderByColumns;

    /**
     * LIMIT information including offset and row count
     */
    private SqlLimit sqlLimit;

    /**
     * List of subquery information for nested queries
     */
    private List<SqlObj> subQueries;

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
    public SqlObj() {
    }

    /**
     * Constructor with original SQL and SQL type
     *
     * @param originalSql the original SQL statement
     * @param sqlType     the type of SQL statement
     */
    public SqlObj(String originalSql, SqlType sqlType) {
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

    public SqlTable getMainTable() {
        return mainTable;
    }

    public void setMainTable(SqlTable mainTable) {
        this.mainTable = mainTable;
    }

    public List<SqlJoin> getJoinTables() {
        return joinTables;
    }

    public void setJoinTables(List<SqlJoin> joinTables) {
        this.joinTables = joinTables;
    }

    public List<SqlField> getSelectColumns() {
        return selectColumns;
    }

    public void setSelectColumns(List<SqlField> selectColumns) {
        this.selectColumns = selectColumns;
    }

    public List<SqlCondition> getWhereConditions() {
        return whereConditions;
    }

    public void setWhereConditions(List<SqlCondition> whereConditions) {
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

    public List<SqlOrderBy> getOrderByColumns() {
        return orderByColumns;
    }

    public void setOrderByColumns(List<SqlOrderBy> orderByColumns) {
        this.orderByColumns = orderByColumns;
    }

    public SqlLimit getLimitInfo() {
        return sqlLimit;
    }

    public void setLimitInfo(SqlLimit sqlLimit) {
        this.sqlLimit = sqlLimit;
    }


    public List<SqlObj> getSubQueries() {
        return subQueries;
    }

    public void setSubQueries(List<SqlObj> subQueries) {
        this.subQueries = subQueries;
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
                ", limitInfo=" + sqlLimit +
                ", subQueries=" + subQueries +
                ", insertColumns=" + insertColumns +
                ", insertValues=" + insertValues +
                ", updateValues=" + updateValues +
                '}';
    }
}