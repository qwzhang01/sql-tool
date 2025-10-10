package io.github.qwzhang01.sql.tool.model;

import java.util.List;

/**
 * Table information class representing database table metadata and references.
 * This class stores comprehensive information about tables including names,
 * aliases, and hierarchical database/schema structure.
 *
 * @author Avin Zhang
 * @since 1.0.0
 */
public class SqlTable {

    /**
     * The table name as it appears in the database
     */
    private String tableName;

    /**
     * The table alias used in SQL statements
     */
    private String alias;

    /**
     * The database name (optional, for multi-database environments)
     */
    private String database;

    /**
     * The schema name (optional, for databases supporting schemas)
     */
    private String schema;

    /**
     * 嵌套查询，只有别名，没有名字，包含的子表数组
     * todo 如果是子查询，用属性方式表示
     */
    private List<SqlTable> childTables;

    /**
     * Default constructor
     */
    public SqlTable() {
    }

    /**
     * Constructor with table name only
     *
     * @param tableName the name of the table
     */
    public SqlTable(String tableName) {
        this.tableName = tableName;
    }

    /**
     * Constructor with table name and alias
     *
     * @param tableName the name of the table
     * @param alias     the table alias
     */
    public SqlTable(String tableName, String alias) {
        this.tableName = tableName;
        this.alias = alias;
    }

    public List<SqlTable> getChildTables() {
        return childTables;
    }

    public void setChildTables(List<SqlTable> childTables) {
        this.childTables = childTables;
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
     * Gets the fully qualified table name including database and schema
     *
     * @return the complete table name in format: [database].[schema].tableName
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