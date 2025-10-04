package io.github.qwzhang01.sql.tool.model;

import java.util.List;
import java.util.Map;

/**
 * SQL信息对象，包含SQL解析后的详细信息
 */
public class SqlInfo {

    /**
     * 原始SQL语句
     */
    private String originalSql;
    /**
     * SQL类型
     */
    private SqlType sqlType;
    /**
     * 主表信息
     */
    private TableInfo mainTable;
    /**
     * 关联表信息列表（用于JOIN查询）
     */
    private List<JoinInfo> joinTables;
    /**
     * 查询字段列表
     */
    private List<ColumnInfo> selectColumns;
    /**
     * WHERE条件列表
     */
    private List<WhereCondition> whereConditions;
    /**
     * GROUP BY字段
     */
    private List<String> groupByColumns;
    /**
     * HAVING条件
     */
    private String havingCondition;
    /**
     * ORDER BY信息
     */
    private List<OrderByInfo> orderByColumns;
    /**
     * LIMIT信息
     */
    private LimitInfo limitInfo;
    /**
     * 参数映射（参数名 -> 参数值）
     */
    private Map<String, Object> parameterMap;
    /**
     * 子查询信息
     */
    private List<SqlInfo> subQueries;
    /**
     * INSERT/UPDATE的字段值映射
     */
    private Map<String, Object> columnValues;
    /**
     * INSERT语句的列名列表
     */
    private List<String> insertColumns;
    /**
     * INSERT语句的值列表
     */
    private List<Object> insertValues;
    /**
     * UPDATE语句的更新值映射
     */
    private Map<String, Object> updateValues;

    // 构造函数
    public SqlInfo() {
    }

    public SqlInfo(String originalSql, SqlType sqlType) {
        this.originalSql = originalSql;
        this.sqlType = sqlType;
    }

    // Getter和Setter方法
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
     * SQL类型枚举
     */
    public enum SqlType {
        SELECT, INSERT, UPDATE, DELETE, CREATE, DROP, ALTER, TRUNCATE
    }
}