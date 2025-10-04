package io.github.qwzhang01.sql.tool.model;

import java.util.List;

/**
 * WHERE条件信息
 */
public class WhereCondition {

    /**
     * 左操作数（字段名）
     */
    private String leftOperand;

    /**
     * 操作符（=, >, <, >=, <=, !=, LIKE, IN, BETWEEN等）
     */
    private String operator;

    /**
     * 右操作数（值或参数）
     */
    private Object rightOperand;

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
        this.operator = operator;
        this.rightOperand = rightOperand;
        this.conditionType = ConditionType.SIMPLE;
    }

    public WhereCondition(String leftOperand, String operator, Object rightOperand, String logicalOperator) {
        this.leftOperand = leftOperand;
        this.operator = operator;
        this.rightOperand = rightOperand;
        this.logicalOperator = logicalOperator;
        this.conditionType = ConditionType.SIMPLE;
    }

    // Getter和Setter方法
    public String getLeftOperand() {
        return leftOperand;
    }

    public void setLeftOperand(String leftOperand) {
        this.leftOperand = leftOperand;
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

    @Override
    public String toString() {
        return "WhereCondition{" +
                "leftOperand='" + leftOperand + '\'' +
                ", operator='" + operator + '\'' +
                ", rightOperand=" + rightOperand +
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