package io.github.qwzhang01.sql.tool.model;

import io.github.qwzhang01.sql.tool.enums.JoinType;

import java.util.List;

/**
 * JOIN operation information with detailed condition analysis.
 * This class represents a JOIN operation in SQL, including the join type,
 * target table information, and detailed analysis of join conditions.
 *
 * @author Avin Zhang
 * @since 1.0.0
 */
public class SqlJoin {

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
    private List<SqlCondition> joinConditions;

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
    public SqlJoin() {
    }

    /**
     * Constructor with join type, table name, and condition
     *
     * @param joinType  the type of JOIN operation
     * @param tableName the name of the table to join
     * @param condition the join condition
     */
    public SqlJoin(JoinType joinType, String tableName, String condition) {
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
    public SqlJoin(JoinType joinType, String tableName, String alias, String condition) {
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

    public List<SqlCondition> getJoinConditions() {
        return joinConditions;
    }

    public void setJoinConditions(List<SqlCondition> joinConditions) {
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
}