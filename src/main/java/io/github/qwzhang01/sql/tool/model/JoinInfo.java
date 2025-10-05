package io.github.qwzhang01.sql.tool.model;

import java.util.List;

/**
 * JOIN operation information with detailed condition analysis.
 * This class represents a JOIN operation in SQL, including the join type,
 * target table information, and detailed analysis of join conditions.
 *
 * @author Avin Zhang
 * @since 1.0.0
 */
public class JoinInfo {

    /**
     * Type of JOIN operation (INNER, LEFT, RIGHT, FULL, CROSS)
     */
    private JoinType joinType;

    /**
     * Name of the table being joined
     */
    private String tableName;

    /**
     * Alias assigned to the joined table
     */
    private String alias;

    /**
     * JOIN condition as original string (maintained for backward compatibility)
     */
    private String condition;

    /**
     * Parsed JOIN conditions with detailed field analysis
     */
    private List<WhereCondition> joinConditions;

    /**
     * Whether the join target is a subquery
     */
    private boolean isSubQuery;

    /**
     * Subquery SQL statement (if joining with a subquery)
     */
    private String subQuerySql;

    // Constructors

    /**
     * Default constructor
     */
    public JoinInfo() {
    }

    /**
     * Constructor with join type, table name, and condition
     *
     * @param joinType  the type of JOIN operation
     * @param tableName the name of the table to join
     * @param condition the join condition
     */
    public JoinInfo(JoinType joinType, String tableName, String condition) {
        this.joinType = joinType;
        this.tableName = tableName;
        this.condition = condition;
    }

    /**
     * Constructor with join type, table name, alias, and condition
     *
     * @param joinType  the type of JOIN operation
     * @param tableName the name of the table to join
     * @param alias     the alias for the joined table
     * @param condition the join condition
     */
    public JoinInfo(JoinType joinType, String tableName, String alias, String condition) {
        this.joinType = joinType;
        this.tableName = tableName;
        this.alias = alias;
        this.condition = condition;
    }

    // Getter and Setter methods
    public JoinType getJoinType() {
        return joinType;
    }

    public void setJoinType(JoinType joinType) {
        this.joinType = joinType;
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

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public boolean isSubQuery() {
        return isSubQuery;
    }

    public void setSubQuery(boolean subQuery) {
        isSubQuery = subQuery;
    }

    public String getSubQuerySql() {
        return subQuerySql;
    }

    public void setSubQuerySql(String subQuerySql) {
        this.subQuerySql = subQuerySql;
    }

    public List<WhereCondition> getJoinConditions() {
        return joinConditions;
    }

    public void setJoinConditions(List<WhereCondition> joinConditions) {
        this.joinConditions = joinConditions;
    }

    @Override
    public String toString() {
        return "JoinInfo{" +
                "joinType=" + joinType +
                ", tableName='" + tableName + '\'' +
                ", alias='" + alias + '\'' +
                ", condition='" + condition + '\'' +
                ", joinConditions=" + joinConditions +
                ", isSubQuery=" + isSubQuery +
                ", subQuerySql='" + subQuerySql + '\'' +
                '}';
    }

    /**
     * JOIN type enumeration defining all supported JOIN operations.
     * Each type corresponds to a specific SQL JOIN syntax and behavior.
     */
    public enum JoinType {
        /**
         * INNER JOIN - returns only matching rows from both tables
         */
        INNER_JOIN("INNER JOIN"),
        /**
         * LEFT JOIN - returns all rows from left table and matching rows from right table
         */
        LEFT_JOIN("LEFT JOIN"),
        /**
         * RIGHT JOIN - returns all rows from right table and matching rows from left table
         */
        RIGHT_JOIN("RIGHT JOIN"),
        /**
         * FULL JOIN - returns all rows from both tables
         */
        FULL_JOIN("FULL JOIN"),
        /**
         * CROSS JOIN - returns Cartesian product of both tables
         */
        CROSS_JOIN("CROSS JOIN"),
        /**
         * LEFT OUTER JOIN - alias for LEFT JOIN
         */
        LEFT_OUTER_JOIN("LEFT OUTER JOIN"),
        /**
         * RIGHT OUTER JOIN - alias for RIGHT JOIN
         */
        RIGHT_OUTER_JOIN("RIGHT OUTER JOIN"),
        /**
         * FULL OUTER JOIN - alias for FULL JOIN
         */
        FULL_OUTER_JOIN("FULL OUTER JOIN");

        private final String sqlKeyword;

        /**
         * Constructor for JOIN type with SQL keyword
         *
         * @param sqlKeyword the SQL keyword representation
         */
        JoinType(String sqlKeyword) {
            this.sqlKeyword = sqlKeyword;
        }

        /**
         * Creates a JoinType from string representation
         *
         * @param joinTypeStr the string representation of join type
         * @return the corresponding JoinType or null if not found
         */
        public static JoinType fromString(String joinTypeStr) {
            if (joinTypeStr == null) {
                return null;
            }

            String normalized = joinTypeStr.toUpperCase().trim();
            for (JoinType type : values()) {
                if (type.getSqlKeyword().equals(normalized) ||
                        type.name().equals(normalized.replace(" ", "_"))) {
                    return type;
                }
            }
            return null;
        }

        /**
         * Gets the SQL keyword for this join type
         *
         * @return the SQL keyword string
         */
        public String getSqlKeyword() {
            return sqlKeyword;
        }
    }
}