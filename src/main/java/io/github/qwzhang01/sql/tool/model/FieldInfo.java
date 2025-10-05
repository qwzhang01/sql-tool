package io.github.qwzhang01.sql.tool.model;

/**
 * Field information class providing detailed analysis of database field references.
 * This class parses and stores information about field expressions including
 * table names, aliases, and field names from SQL statements.
 *
 * @author Avin Zhang
 * @since 1.0.0
 */
public class FieldInfo {

    /**
     * The actual table name (may be null if only alias is used)
     */
    private String tableName;

    /**
     * The table alias used in the SQL statement
     */
    private String tableAlias;

    /**
     * The field/column name
     */
    private String fieldName;

    /**
     * Complete field expression as it appears in SQL (e.g., t1.user_name, user.id)
     */
    private String fullExpression;

    /**
     * Default constructor
     */
    public FieldInfo() {
    }

    /**
     * Constructor that parses a field expression
     *
     * @param fullExpression the complete field expression to parse
     */
    public FieldInfo(String fullExpression) {
        this.fullExpression = fullExpression;
        parseExpression(fullExpression);
    }

    /**
     * Constructor with explicit field components
     *
     * @param tableName  the table name
     * @param tableAlias the table alias
     * @param fieldName  the field name
     */
    public FieldInfo(String tableName, String tableAlias, String fieldName) {
        this.tableName = tableName;
        this.tableAlias = tableAlias;
        this.fieldName = fieldName;
        this.fullExpression = buildFullExpression();
    }

    /**
     * Parses a field expression to extract table and field components
     *
     * @param expression the field expression to parse
     */
    private void parseExpression(String expression) {
        if (expression == null || expression.trim().isEmpty()) {
            return;
        }

        expression = expression.trim();

        // Check if expression contains dot (indicating table name or alias)
        if (expression.contains(".")) {
            String[] parts = expression.split("\\.", 2);
            this.tableAlias = parts[0].trim();
            this.fieldName = parts[1].trim();
        } else {
            this.fieldName = expression;
        }
    }

    /**
     * Builds the complete field expression from components
     *
     * @return the complete field expression
     */
    private String buildFullExpression() {
        if (tableAlias != null && !tableAlias.isEmpty()) {
            return tableAlias + "." + fieldName;
        } else if (tableName != null && !tableName.isEmpty()) {
            return tableName + "." + fieldName;
        } else {
            return fieldName;
        }
    }

    // Getter and Setter methods
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

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFullExpression() {
        return fullExpression;
    }

    public void setFullExpression(String fullExpression) {
        this.fullExpression = fullExpression;
        parseExpression(fullExpression);
    }

    @Override
    public String toString() {
        return "FieldInfo{" +
                "tableName='" + tableName + '\'' +
                ", tableAlias='" + tableAlias + '\'' +
                ", fieldName='" + fieldName + '\'' +
                ", fullExpression='" + fullExpression + '\'' +
                '}';
    }
}