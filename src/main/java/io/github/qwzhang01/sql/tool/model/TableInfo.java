package io.github.qwzhang01.sql.tool.model;

/**
 * 表信息
 */
public class TableInfo {

    /**
     * 表名
     */
    private String tableName;

    /**
     * 表别名
     */
    private String alias;

    /**
     * 数据库名（可选）
     */
    private String database;

    /**
     * 模式名（可选）
     */
    private String schema;

    public TableInfo() {
    }

    public TableInfo(String tableName) {
        this.tableName = tableName;
    }

    public TableInfo(String tableName, String alias) {
        this.tableName = tableName;
        this.alias = alias;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    /**
     * 获取完整表名
     */
    public String getFullTableName() {
        StringBuilder sb = new StringBuilder();
        if (database != null && !database.isEmpty()) {
            sb.append(database).append(".");
        }
        if (schema != null && !schema.isEmpty()) {
            sb.append(schema).append(".");
        }
        sb.append(tableName);
        return sb.toString();
    }

    @Override
    public String toString() {
        return "TableInfo{" +
                "tableName='" + tableName + '\'' +
                ", alias='" + alias + '\'' +
                ", database='" + database + '\'' +
                ", schema='" + schema + '\'' +
                '}';
    }
}