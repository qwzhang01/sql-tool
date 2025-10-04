package io.github.qwzhang01.sql.tool.model;

/**
 * 字段信息
 */
public class ColumnInfo {

    /**
     * 字段名
     */
    private String columnName;

    /**
     * 表名
     */
    private String tableName;

    /**
     * 表别名
     */
    private String tableAlias;

    /**
     * 字段别名
     */
    private String alias;

    /**
     * 字段类型
     */
    private String dataType;

    /**
     * 是否为主键
     */
    private boolean isPrimaryKey;

    /**
     * 是否允许为空
     */
    private boolean nullable;

    /**
     * 默认值
     */
    private String defaultValue;

    // 构造函数
    public ColumnInfo() {
    }

    public ColumnInfo(String columnName) {
        this.columnName = columnName;
    }

    public ColumnInfo(String columnName, String tableName) {
        this.columnName = columnName;
        this.tableName = tableName;
    }

    public ColumnInfo(String columnName, String tableName, String alias) {
        this.columnName = columnName;
        this.tableName = tableName;
        this.alias = alias;
    }

    // Getter和Setter方法
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