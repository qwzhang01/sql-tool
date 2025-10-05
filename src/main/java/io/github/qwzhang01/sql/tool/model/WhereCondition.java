package io.github.qwzhang01.sql.tool.model;

import java.util.List;

/**
 * WHERE condition information with detailed field analysis.
 * This class represents a single condition in a WHERE clause, providing
 * comprehensive information about the field, operator, and values involved.
 *
 * @author Avin Zhang
 * @since 1.0.0
 */
public class WhereCondition {

    /**
     * Left operand (field name) - maintained for backward compatibility
     */
    private String leftOperand;

    /**
     * Detailed field information object containing table name, alias, and field name
     */
    private FieldInfo fieldInfo;

    /**
     * Comparison operator (=, >, <, >=, <=, !=, LIKE, IN, BETWEEN, etc.)
     */
    private String operator;

    /**
     * Right operand (value or parameter) that the field is compared against
     */
    private Object rightOperand;

    /**
     * Number of values (for IN, BETWEEN and other multi-value operators)
     */
    private Integer valueCount;

    /**
     * Logical connector (AND, OR) linking this condition to others
     */
    private String logicalOperator;

    /**
     * Type of condition for categorization and processing
     */
    private ConditionType conditionType;

    /**
     * Sub-conditions for complex nested conditions with parentheses
     */
    private List<WhereCondition> subConditions;

    // Constructors

    /**
     * Default constructor
     */
    public WhereCondition() {
    }

    /**
     * Constructor with basic condition components
     *
     * @param leftOperand  the field name or expression on the left side
     * @param operator     the comparison operator
     * @param rightOperand the value or expression on the right side
     */
    public WhereCondition(String leftOperand, String operator, Object rightOperand) {
        this.leftOperand = leftOperand;
        this.fieldInfo = new FieldInfo(leftOperand);
        this.operator = operator;
        this.rightOperand = rightOperand;
        this.conditionType = ConditionType.SIMPLE;
        this.valueCount = calculateValueCount(operator, rightOperand);
    }

    /**
     * Constructor with logical operator
     *
     * @param leftOperand     the field name or expression on the left side
     * @param operator        the comparison operator
     * @param rightOperand    the value or expression on the right side
     * @param logicalOperator the logical connector (AND/OR)
     */
    public WhereCondition(String leftOperand, String operator, Object rightOperand, String logicalOperator) {
        this.leftOperand = leftOperand;
        this.fieldInfo = new FieldInfo(leftOperand);
        this.operator = operator;
        this.rightOperand = rightOperand;
        this.logicalOperator = logicalOperator;
        this.conditionType = ConditionType.SIMPLE;
        this.valueCount = calculateValueCount(operator, rightOperand);
    }

    /**
     * Constructor with detailed field information
     *
     * @param fieldInfo    detailed field information including table and alias
     * @param operator     the comparison operator
     * @param rightOperand the value or expression on the right side
     */
    public WhereCondition(FieldInfo fieldInfo, String operator, Object rightOperand) {
        this.fieldInfo = fieldInfo;
        this.leftOperand = fieldInfo != null ? fieldInfo.getFullExpression() : null;
        this.operator = operator;
        this.rightOperand = rightOperand;
        this.conditionType = ConditionType.SIMPLE;
        this.valueCount = calculateValueCount(operator, rightOperand);
    }

    // Getter and Setter methods
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
     * Checks if the condition is empty or invalid
     *
     * @return true if the left operand is null or empty
     */
    public boolean isEmpty() {
        return leftOperand == null || leftOperand.trim().isEmpty();
    }

    /**
     * Checks if the condition contains the specified text
     *
     * @param text the text to search for
     * @return true if any part of the condition contains the text
     */
    public boolean contains(String text) {
        if (text == null) return false;

        return (leftOperand != null && leftOperand.contains(text)) ||
                (operator != null && operator.contains(text)) ||
                (rightOperand != null && rightOperand.toString().contains(text));
    }

    /**
     * Calculates the number of values based on the operator type
     *
     * @param operator     the comparison operator
     * @param rightOperand the right side value(s)
     * @return the count of values involved in the condition
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
                return 2; // BETWEEN always has two values

            case "IS NULL":
            case "IS NOT NULL":
                return 0; // NULL checks require no values

            default:
                return 1; // Other operators typically have one value
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
     * Condition type enumeration for categorizing different types of WHERE conditions.
     * This helps in processing and optimizing different condition patterns.
     */
    public enum ConditionType {
        /**
         * Simple condition: field = value
         */
        SIMPLE,
        /**
         * Complex condition: contains sub-conditions with parentheses
         */
        COMPLEX,
        /**
         * IN condition: field IN (value1, value2, ...)
         */
        IN,
        /**
         * BETWEEN condition: field BETWEEN value1 AND value2
         */
        BETWEEN,
        /**
         * LIKE condition: field LIKE pattern
         */
        LIKE,
        /**
         * EXISTS condition: EXISTS (subquery)
         */
        EXISTS,
        /**
         * IS NULL condition: field IS NULL
         */
        IS_NULL,
        /**
         * IS NOT NULL condition: field IS NOT NULL
         */
        IS_NOT_NULL
    }
}