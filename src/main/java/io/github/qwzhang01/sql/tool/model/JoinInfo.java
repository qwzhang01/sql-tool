package io.github.qwzhang01.sql.tool.model;

import java.util.List;

/**
 * JOIN信息
 */
public class JoinInfo {

    /**
     * JOIN类型
     */
    private JoinType joinType;

    /**
     * 表名
     */
    private String tableName;

    /**
     * 表别名
     */
    private String alias;

    /**
     * JOIN条件（原始字符串，保持向后兼容）
     */
    private String condition;

    /**
     * 解析后的JOIN条件列表
     */
    private List<WhereCondition> joinConditions;

    /**
     * 是否为子查询
     */
    private boolean isSubQuery;

    /**
     * 子查询SQL（如果是子查询）
     */
    private String subQuerySql;

    // 构造函数
    public JoinInfo() {
    }

    public JoinInfo(JoinType joinType, String tableName, String condition) {
        this.joinType = joinType;
        this.tableName = tableName;
        this.condition = condition;
    }

    public JoinInfo(JoinType joinType, String tableName, String alias, String condition) {
        this.joinType = joinType;
        this.tableName = tableName;
        this.alias = alias;
        this.condition = condition;
    }

    // Getter和Setter方法
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
     * JOIN类型枚举
     */
    public enum JoinType {
        INNER_JOIN("INNER JOIN"),
        LEFT_JOIN("LEFT JOIN"),
        RIGHT_JOIN("RIGHT JOIN"),
        FULL_JOIN("FULL JOIN"),
        CROSS_JOIN("CROSS JOIN"),
        LEFT_OUTER_JOIN("LEFT OUTER JOIN"),
        RIGHT_OUTER_JOIN("RIGHT OUTER JOIN"),
        FULL_OUTER_JOIN("FULL OUTER JOIN");

        private final String sqlKeyword;

        JoinType(String sqlKeyword) {
            this.sqlKeyword = sqlKeyword;
        }

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

        public String getSqlKeyword() {
            return sqlKeyword;
        }
    }
}