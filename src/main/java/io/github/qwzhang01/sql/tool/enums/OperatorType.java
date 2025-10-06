package io.github.qwzhang01.sql.tool.enums;

/**
 * Operator type enumeration.
 */
public enum OperatorType {
    SINGLE_PARAM(1),    // Single parameter operators: =, !=, <, >, <=, >=, LIKE
    IN_OPERATOR(0),     // IN operator: parameter count dynamically determined
    BETWEEN_OPERATOR(2), // BETWEEN operator: fixed 2 parameters
    NO_PARAM(0);        // No parameter operators: IS NULL, IS NOT NULL

    private final int paramCount;

    OperatorType(int paramCount) {
        this.paramCount = paramCount;
    }

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

    public int getParamCount() {
        return paramCount;
    }
}