package io.github.qwzhang01.sql.tool.model;

/**
 * 字段信息类
 */
public class FieldInfo {

    /**
     * 表名
     */
    private String tableName;

    /**
     * 表别名
     */
    private String tableAlias;

    /**
     * 字段名
     */
    private String fieldName;

    /**
     * 完整字段表达式（如：t1.user_name, user.id等）
     */
    private String fullExpression;

    public FieldInfo() {
    }

    public FieldInfo(String fullExpression) {
        this.fullExpression = fullExpression;
        parseExpression(fullExpression);
    }

    public FieldInfo(String tableName, String tableAlias, String fieldName) {
        this.tableName = tableName;
        this.tableAlias = tableAlias;
        this.fieldName = fieldName;
        this.fullExpression = buildFullExpression();
    }

    /**
     * 解析字段表达式
     */
    private void parseExpression(String expression) {
        if (expression == null || expression.trim().isEmpty()) {
            return;
        }

        expression = expression.trim();

        // 检查是否包含点号（表示有表名或别名）
        if (expression.contains(".")) {
            String[] parts = expression.split("\\.", 2);
            this.tableAlias = parts[0].trim();
            this.fieldName = parts[1].trim();
        } else {
            this.fieldName = expression;
        }
    }

    /**
     * 构建完整表达式
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

    // Getter和Setter方法
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