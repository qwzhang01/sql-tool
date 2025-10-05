package io.github.qwzhang01.sql.tool.util;

import io.github.qwzhang01.sql.tool.model.*;
import io.github.qwzhang01.sql.tool.parser.MySqlPureSqlParser;
import io.github.qwzhang01.sql.tool.parser.SqlParser;

import java.util.List;
import java.util.Map;

/**
 * SQL解析工具类
 *
 * @author avinzhang
 */
public class SqlParserUtils {

    private static final SqlParser DEFAULT_PARSER = new MySqlPureSqlParser();

    /**
     * 解析SQL语句（自动清理注释）
     */
    public static SqlInfo parseSQL(String sql) {
        return DEFAULT_PARSER.parse(sql);
    }

    /**
     * 解析SQL语句（带参数，自动清理注释）
     */
    public static SqlInfo parseSQL(String sql, Map<String, Object> parameters) {
        return DEFAULT_PARSER.parse(sql, parameters);
    }

    public static List<WhereCondition> parseWhere(String sql) {
        return DEFAULT_PARSER.parseWhere(sql);
    }

    public static List<JoinInfo> parseJoin(String sql) {
        return DEFAULT_PARSER.parseJoin(sql);
    }

    /**
     * 清理SQL中的注释和多余空白字符
     */
    public static String cleanSQL(String sql) {
        return DEFAULT_PARSER.getCleaner().cleanSql(sql);
    }

    /**
     * 清理并格式化SQL
     */
    public static String cleanAndFormatSQL(String sql) {
        return DEFAULT_PARSER.getCleaner().cleanAndFormatSql(sql);
    }

    /**
     * 检查SQL是否包含注释
     */
    public static boolean containsComments(String sql) {
        return DEFAULT_PARSER.getCleaner().containsComments(sql);
    }

    /**
     * 仅移除注释，保留原始格式
     */
    public static String removeCommentsOnly(String sql) {
        return DEFAULT_PARSER.getCleaner().removeCommentsOnly(sql);
    }

    /**
     * 将SqlInfo转换为SQL语句
     */
    public static String toSQL(SqlInfo sqlInfo) {
        return DEFAULT_PARSER.toSql(sqlInfo);
    }

    /**
     * 检查SQL是否为查询语句
     */
    public static boolean isSelectSQL(SqlInfo sqlInfo) {
        return sqlInfo.getSqlType() == SqlInfo.SqlType.SELECT;
    }

    /**
     * 检查SQL是否为更新语句（INSERT/UPDATE/DELETE）
     */
    public static boolean isUpdateSQL(SqlInfo sqlInfo) {
        SqlInfo.SqlType type = sqlInfo.getSqlType();
        return type == SqlInfo.SqlType.INSERT ||
                type == SqlInfo.SqlType.UPDATE ||
                type == SqlInfo.SqlType.DELETE;
    }

    /**
     * 获取SQL涉及的所有表名
     */
    public static java.util.Set<String> getAllTableNames(SqlInfo sqlInfo) {
        java.util.Set<String> tableNames = new java.util.HashSet<>();

        // 主表
        if (sqlInfo.getMainTable() != null) {
            tableNames.add(sqlInfo.getMainTable().getTableName());
        }

        // JOIN表
        if (sqlInfo.getJoinTables() != null) {
            for (JoinInfo joinInfo : sqlInfo.getJoinTables()) {
                if (joinInfo.getTableName() != null) {
                    tableNames.add(joinInfo.getTableName());
                }
            }
        }

        // 子查询中的表
        if (sqlInfo.getSubQueries() != null) {
            for (SqlInfo subQuery : sqlInfo.getSubQueries()) {
                tableNames.addAll(getAllTableNames(subQuery));
            }
        }

        return tableNames;
    }

    /**
     * 获取SQL涉及的所有字段名
     */
    public static java.util.Set<String> getAllColumnNames(SqlInfo sqlInfo) {
        java.util.Set<String> columnNames = new java.util.HashSet<>();

        // SELECT字段
        if (sqlInfo.getSelectColumns() != null) {
            for (ColumnInfo columnInfo : sqlInfo.getSelectColumns()) {
                if (!"*".equals(columnInfo.getColumnName())) {
                    columnNames.add(columnInfo.getColumnName());
                }
            }
        }

        // WHERE条件中的字段
        if (sqlInfo.getWhereConditions() != null) {
            for (WhereCondition condition : sqlInfo.getWhereConditions()) {
                collectColumnsFromCondition(condition, columnNames);
            }
        }

        // HAVING条件中的字段 - HAVING是字符串，暂时跳过解析
        // collectColumnsFromCondition(sqlInfo.getHavingCondition(), columnNames);

        // GROUP BY字段
        if (sqlInfo.getGroupByColumns() != null) {
            columnNames.addAll(sqlInfo.getGroupByColumns());
        }

        // ORDER BY字段
        if (sqlInfo.getOrderByColumns() != null) {
            for (OrderByInfo orderByInfo : sqlInfo.getOrderByColumns()) {
                columnNames.add(orderByInfo.getColumnName());
            }
        }

        // INSERT/UPDATE字段
        if (sqlInfo.getColumnValues() != null) {
            columnNames.addAll(sqlInfo.getColumnValues().keySet());
        }

        return columnNames;
    }

    /**
     * 从条件中收集字段名
     */
    private static void collectColumnsFromCondition(WhereCondition condition, java.util.Set<String> columnNames) {
        if (condition == null) {
            return;
        }

        if (condition.getLeftOperand() != null) {
            columnNames.add(condition.getLeftOperand());
        }

        if (condition.getSubConditions() != null) {
            for (WhereCondition subCondition : condition.getSubConditions()) {
                collectColumnsFromCondition(subCondition, columnNames);
            }
        }
    }

    /**
     * 检查SQL是否包含JOIN
     */
    public static boolean hasJoin(SqlInfo sqlInfo) {
        return sqlInfo.getJoinTables() != null && !sqlInfo.getJoinTables().isEmpty();
    }

    /**
     * 检查SQL是否包含子查询
     */
    public static boolean hasSubQuery(SqlInfo sqlInfo) {
        return sqlInfo.getSubQueries() != null && !sqlInfo.getSubQueries().isEmpty();
    }

    /**
     * 检查SQL是否包含聚合函数
     */
    public static boolean hasAggregateFunction(SqlInfo sqlInfo) {
        if (sqlInfo.getSelectColumns() != null) {
            for (ColumnInfo columnInfo : sqlInfo.getSelectColumns()) {
                // 简单检查字段名是否包含聚合函数关键字
                String columnName = columnInfo.getColumnName();
                if (columnName != null && (
                        columnName.toUpperCase().contains("COUNT(") ||
                                columnName.toUpperCase().contains("SUM(") ||
                                columnName.toUpperCase().contains("AVG(") ||
                                columnName.toUpperCase().contains("MAX(") ||
                                columnName.toUpperCase().contains("MIN(")
                )) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 获取SQL的复杂度评分
     */
    public static int getComplexityScore(SqlInfo sqlInfo) {
        int score = 0;

        // 基础分数
        score += 1;

        // JOIN增加复杂度
        if (sqlInfo.getJoinTables() != null) {
            score += sqlInfo.getJoinTables().size() * 2;
        }

        // 子查询增加复杂度
        if (sqlInfo.getSubQueries() != null) {
            score += sqlInfo.getSubQueries().size() * 3;
            for (SqlInfo subQuery : sqlInfo.getSubQueries()) {
                score += getComplexityScore(subQuery);
            }
        }

        // WHERE条件增加复杂度
        if (sqlInfo.getWhereConditions() != null) {
            for (WhereCondition condition : sqlInfo.getWhereConditions()) {
                score += getConditionComplexity(condition);
            }
        }

        // HAVING条件增加复杂度 - HAVING是字符串，简单计算
        if (sqlInfo.getHavingCondition() != null && !sqlInfo.getHavingCondition().trim().isEmpty()) {
            score += 1;
        }

        // GROUP BY增加复杂度
        if (sqlInfo.getGroupByColumns() != null && !sqlInfo.getGroupByColumns().isEmpty()) {
            score += 1;
        }

        // ORDER BY增加复杂度
        if (sqlInfo.getOrderByColumns() != null && !sqlInfo.getOrderByColumns().isEmpty()) {
            score += 1;
        }

        // 聚合函数增加复杂度
        if (hasAggregateFunction(sqlInfo)) {
            score += 1;
        }

        return score;
    }

    /**
     * 计算条件的复杂度
     */
    private static int getConditionComplexity(WhereCondition condition) {
        if (condition == null) {
            return 0;
        }

        int complexity = 1;

        if (condition.getSubConditions() != null) {
            for (WhereCondition subCondition : condition.getSubConditions()) {
                complexity += getConditionComplexity(subCondition);
            }
        }

        return complexity;
    }

    /**
     * 格式化SQL信息为可读字符串
     */
    public static String formatSqlInfo(SqlInfo sqlInfo) {
        StringBuilder sb = new StringBuilder();

        sb.append("SQL信息:\n");
        sb.append("  类型: ").append(sqlInfo.getSqlType()).append("\n");
        sb.append("  原始SQL: ").append(sqlInfo.getOriginalSql()).append("\n");

        if (sqlInfo.getMainTable() != null) {
            sb.append("  主表: ").append(sqlInfo.getMainTable().getTableName());
            if (sqlInfo.getMainTable().getAlias() != null) {
                sb.append(" AS ").append(sqlInfo.getMainTable().getAlias());
            }
            sb.append("\n");
        }

        if (sqlInfo.getJoinTables() != null && !sqlInfo.getJoinTables().isEmpty()) {
            sb.append("  JOIN表:\n");
            for (JoinInfo joinInfo : sqlInfo.getJoinTables()) {
                sb.append("    ").append(joinInfo.getJoinType())
                        .append(" ").append(joinInfo.getTableName());
                if (joinInfo.getAlias() != null) {
                    sb.append(" AS ").append(joinInfo.getAlias());
                }
                sb.append("\n");
            }
        }

        if (sqlInfo.getSelectColumns() != null && !sqlInfo.getSelectColumns().isEmpty()) {
            sb.append("  查询字段: ");
            for (int i = 0; i < sqlInfo.getSelectColumns().size(); i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                ColumnInfo column = sqlInfo.getSelectColumns().get(i);
                sb.append(column.getColumnName());
                if (column.getAlias() != null) {
                    sb.append(" AS ").append(column.getAlias());
                }
            }
            sb.append("\n");
        }

        if (sqlInfo.getParameterMap() != null && !sqlInfo.getParameterMap().isEmpty()) {
            sb.append("  参数: ").append(sqlInfo.getParameterMap()).append("\n");
        }

        sb.append("  复杂度评分: ").append(getComplexityScore(sqlInfo));

        return sb.toString();
    }

    public static boolean equal(String mark1, String mark2) {
        return DEFAULT_PARSER.getCompare().equal(mark1, mark2);
    }
}