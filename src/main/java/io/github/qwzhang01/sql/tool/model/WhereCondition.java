package io.github.qwzhang01.sql.tool.model;

import java.util.List;

/**
 * WHERE条件信息
 */
public class WhereCondition {

    /**
     * 左操作数（字段名）- 保持向后兼容
     */
    private String leftOperand;

    /**
     * 字段信息对象
     */
    private FieldInfo fieldInfo;

    /**
     * 操作符（=, >, <, >=, <=, !=, LIKE, IN, BETWEEN等）
     */
    private String operator;

    /**
     * 右操作数（值或参数）
     */
    private Object rightOperand;

    /**
     * 值的数量（针对IN、BETWEEN等操作符）
     */
    private Integer valueCount;

    /**
     * 逻辑连接符（AND, OR）
     */
    private String logicalOperator;

    /**
     * 条件类型
     */
    private ConditionType conditionType;

    /**
     * 子条件（用于复杂的嵌套条件）
     */
    private List<WhereCondition> subConditions;

    // 构造函数
    public WhereCondition() {
    }

    public WhereCondition(String leftOperand, String operator, Object rightOperand) {
        this.leftOperand = leftOperand;
        this.fieldInfo = new FieldInfo(leftOperand);
        this.operator = operator;
        this.rightOperand = rightOperand;
        this.conditionType = ConditionType.SIMPLE;
        this.valueCount = calculateValueCount(operator, rightOperand);
    }

    public WhereCondition(String leftOperand, String operator, Object rightOperand, String logicalOperator) {
        this.leftOperand = leftOperand;
        this.fieldInfo = new FieldInfo(leftOperand);
        this.operator = operator;
        this.rightOperand = rightOperand;
        this.logicalOperator = logicalOperator;
        this.conditionType = ConditionType.SIMPLE;
        this.valueCount = calculateValueCount(operator, rightOperand);
    }

    public WhereCondition(FieldInfo fieldInfo, String operator, Object rightOperand) {
        this.fieldInfo = fieldInfo;
        this.leftOperand = fieldInfo != null ? fieldInfo.getFullExpression() : null;
        this.operator = operator;
        this.rightOperand = rightOperand;
        this.conditionType = ConditionType.SIMPLE;
        this.valueCount = calculateValueCount(operator, rightOperand);
    }

    // Getter和Setter方法
    public String getLeftOperand() {
        return leftOperand;
    }

    public void setLeftOperand(String leftOperand) {
        this.leftOperand = leftOperand;
        this.fieldInfo = new FieldInfo(leftOperand);
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public Object getRightOperand() {
        return rightOperand;
    }

    public void setRightOperand(Object rightOperand) {
        this.rightOperand = rightOperand;
        this.valueCount = calculateValueCount(this.operator, rightOperand);
    }

    public String getLogicalOperator() {
        return logicalOperator;
    }

    public void setLogicalOperator(String logicalOperator) {
        this.logicalOperator = logicalOperator;
    }

    public ConditionType getConditionType() {
        return conditionType;
    }

    public void setConditionType(ConditionType conditionType) {
        this.conditionType = conditionType;
    }

    public List<WhereCondition> getSubConditions() {
        return subConditions;
    }

    public void setSubConditions(List<WhereCondition> subConditions) {
        this.subConditions = subConditions;
    }

    public FieldInfo getFieldInfo() {
        return fieldInfo;
    }

    public void setFieldInfo(FieldInfo fieldInfo) {
        this.fieldInfo = fieldInfo;
        this.leftOperand = fieldInfo != null ? fieldInfo.getFullExpression() : null;
    }

    public Integer getValueCount() {
        return valueCount;
    }

    public void setValueCount(Integer valueCount) {
        this.valueCount = valueCount;
    }

    /**
     * 检查条件是否为空
     */
    public boolean isEmpty() {
        return leftOperand == null || leftOperand.trim().isEmpty();
    }

    /**
     * 检查条件是否包含指定文本
     */
    public boolean contains(String text) {
        if (text == null) return false;

        return (leftOperand != null && leftOperand.contains(text)) ||
                (operator != null && operator.contains(text)) ||
                (rightOperand != null && rightOperand.toString().contains(text));
    }

    /**
     * 计算值的数量
     */
    private Integer calculateValueCount(String operator, Object rightOperand) {
        if (operator == null || rightOperand == null) {
            return null;
        }

        String op = operator.toUpperCase().trim();

        switch (op) {
            case "IN":
            case "NOT IN":
                if (rightOperand instanceof List) {
                    return ((List<?>) rightOperand).size();
                } else if (rightOperand instanceof String) {
                    String str = (String) rightOperand;
                    // 简单计算逗号分隔的值数量
                    if (str.contains(",")) {
                        return str.split(",").length;
                    }
                }
                return 1;

            case "BETWEEN":
            case "NOT BETWEEN":
                return 2; // BETWEEN 总是有两个值

            case "IS NULL":
            case "IS NOT NULL":
                return 0; // NULL 检查不需要值

            default:
                return 1; // 其他操作符通常有一个值
        }
    }

    @Override
    public String toString() {
        return "WhereCondition{" +
                "leftOperand='" + leftOperand + '\'' +
                ", fieldInfo=" + fieldInfo +
                ", operator='" + operator + '\'' +
                ", rightOperand=" + rightOperand +
                ", valueCount=" + valueCount +
                ", logicalOperator='" + logicalOperator + '\'' +
                ", conditionType=" + conditionType +
                ", subConditions=" + subConditions +
                '}';
    }

    /**
     * 条件类型枚举
     */
    public enum ConditionType {
        SIMPLE,     // 简单条件：field = value
        COMPLEX,    // 复杂条件：包含子条件
        IN,         // IN条件
        BETWEEN,    // BETWEEN条件
        LIKE,       // LIKE条件
        EXISTS,     // EXISTS条件
        IS_NULL,    // IS NULL条件
        IS_NOT_NULL // IS NOT NULL条件
    }
}