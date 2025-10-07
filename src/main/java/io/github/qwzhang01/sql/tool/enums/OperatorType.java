package io.github.qwzhang01.sql.tool.enums;

/**
 * Enumeration defining different types of SQL operators based on their parameter requirements.
 * This classification helps in parameter binding and SQL generation by determining
 * how many parameters each operator type expects.
 *
 * @author avinzhang
 */
public enum OperatorType {
    /**
     * Single parameter operators that require exactly one parameter value.
     * Examples: "=, !=, "&lt;", "&gt;", "&lt;=", "&gt;=", LIKE, NOT LIKE"
     * Usage: field = ? or field LIKE ?
     */
    SINGLE_PARAM(1),

    /**
     * IN operator that accepts a variable number of parameters.
     * The parameter count is determined dynamically based on the values list.
     * Examples: IN, NOT IN
     * Usage: field IN (?, ?, ?) or field NOT IN (?, ?)
     */
    IN_OPERATOR(0),

    /**
     * BETWEEN operator that requires exactly two parameters (start and end values).
     * Examples: BETWEEN, NOT BETWEEN
     * Usage: field BETWEEN ? AND ? or field NOT BETWEEN ? AND ?
     */
    BETWEEN_OPERATOR(2),

    /**
     * No parameter operators that don't require any parameter values.
     * Examples: IS NULL, IS NOT NULL
     * Usage: field IS NULL or field IS NOT NULL
     */
    NO_PARAM(0);

    /**
     * The expected number of parameters for this operator type.
     * For variable parameter operators (like IN), this represents the base count.
     */
    private final int paramCount;

    /**
     * Constructor for OperatorType with parameter count specification.
     *
     * @param paramCount the expected number of parameters for this operator type
     */
    OperatorType(int paramCount) {
        this.paramCount = paramCount;
    }

    /**
     * Converts a string operator to its corresponding OperatorType.
     * This method analyzes the operator string and determines the appropriate type
     * based on parameter requirements.
     *
     * @param operator the SQL operator string (e.g., "=", "IN", "BETWEEN")
     * @return the corresponding OperatorType, defaults to SINGLE_PARAM if not recognized
     */
    public static OperatorType convertOperatorType(String operator) {
        if (operator == null) {
            return OperatorType.SINGLE_PARAM;
        }

        String op = operator.toUpperCase().trim();
        return switch (op) {
            case "IN", "NOT IN" -> OperatorType.IN_OPERATOR;
            case "BETWEEN", "NOT BETWEEN" -> OperatorType.BETWEEN_OPERATOR;
            case "IS NULL", "IS NOT NULL" -> OperatorType.NO_PARAM;
            default -> OperatorType.SINGLE_PARAM;
        };
    }

    /**
     * Gets the expected parameter count for this operator type.
     *
     * @return the number of parameters this operator type expects
     */
    public int getParamCount() {
        return paramCount;
    }
}