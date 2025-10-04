package io.github.qwzhang01.sql.tool.parser;

import io.github.qwzhang01.sql.tool.exception.ParseException;
import io.github.qwzhang01.sql.tool.model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 纯Java实现的MySQL SQL解析器
 * 不依赖任何外部包，支持SQL与对象的双向转换
 */
public class MySqlPureSqlParser implements SqlParser {

    // SQL关键字正则表达式
    private static final Pattern SELECT_PATTERN = Pattern.compile(
            "^\\s*SELECT\\s+(.+?)\\s+FROM\\s+(.+?)(?:\\s+WHERE\\s+(.+?))?(?:\\s+GROUP\\s+BY\\s+(.+?))?(?:\\s+HAVING\\s+(.+?))?(?:\\s+ORDER\\s+BY\\s+(.+?))?(?:\\s+LIMIT\\s+(.+?))?\\s*$",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );

    private static final Pattern INSERT_PATTERN = Pattern.compile(
            "^\\s*INSERT\\s+INTO\\s+(\\w+)\\s*\\(([^)]+)\\)\\s*VALUES\\s*\\(([^)]+)\\)\\s*$",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );

    private static final Pattern UPDATE_PATTERN = Pattern.compile(
            "^\\s*UPDATE\\s+(\\w+(?:\\s+\\w+)?)\\s+SET\\s+(.+?)(?:\\s+WHERE\\s+(.+?))?(?:\\s+LIMIT\\s+(.+?))?\\s*$",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );

    private static final Pattern DELETE_PATTERN = Pattern.compile(
            "^\\s*DELETE\\s+FROM\\s+(\\w+(?:\\s+\\w+)?)(?:\\s+WHERE\\s+(.+?))?(?:\\s+LIMIT\\s+(.+?))?\\s*$",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );

    @Override
    public SqlInfo parse(String sql) {
        if (sql == null || sql.isEmpty() || sql.trim().isEmpty()) {
            return null;
        }
        return parse(sql, null);
    }

    @Override
    public SqlInfo parse(String sql, Map<String, Object> parameters) {
        if (sql == null || sql.trim().isEmpty()) {
            throw new IllegalArgumentException("SQL不能为空");
        }

        sql = sql.trim();
        SqlInfo sqlInfo = new SqlInfo();
        sqlInfo.setOriginalSql(sql);

        try {
            if (sql.toUpperCase().startsWith("SELECT")) {
                parseSelect(sql, sqlInfo);
            } else if (sql.toUpperCase().startsWith("INSERT")) {
                parseInsert(sql, sqlInfo);
            } else if (sql.toUpperCase().startsWith("UPDATE")) {
                parseUpdate(sql, sqlInfo);
            } else if (sql.toUpperCase().startsWith("DELETE")) {
                parseDelete(sql, sqlInfo);
            } else {
                throw new IllegalArgumentException("不支持的SQL类型: " + sql);
            }

            if (parameters != null) {
                sqlInfo.setParameterMap(parameters);
            }

            return sqlInfo;
        } catch (Exception e) {
            throw new ParseException("解析SQL失败: " + sql, e);
        }
    }

    /**
     * 解析SELECT语句
     */
    private void parseSelect(String sql, SqlInfo sqlInfo) {
        sqlInfo.setSqlType(SqlInfo.SqlType.SELECT);

        Matcher matcher = SELECT_PATTERN.matcher(sql);
        if (matcher.find()) {
            String selectClause = matcher.group(1);
            String fromClause = matcher.group(2);
            String whereClause = matcher.group(3);
            String groupByClause = matcher.group(4);
            String havingClause = matcher.group(5);
            String orderByClause = matcher.group(6);
            String limitClause = matcher.group(7);

            // 解析SELECT字段
            parseSelectColumns(selectClause, sqlInfo);

            // 解析FROM子句（包含JOIN）
            parseFromClause(fromClause, sqlInfo);

            // 解析WHERE条件
            if (whereClause != null) {
                parseWhereConditions(whereClause, sqlInfo);
            }

            // 解析GROUP BY
            if (groupByClause != null) {
                parseGroupBy(groupByClause, sqlInfo);
            }

            // 解析HAVING
            if (havingClause != null) {
                sqlInfo.setHavingCondition(havingClause.trim());
            }

            // 解析ORDER BY
            if (orderByClause != null) {
                parseOrderBy(orderByClause, sqlInfo);
            }

            // 解析LIMIT
            if (limitClause != null) {
                parseLimit(limitClause, sqlInfo);
            }
        }
    }

    /**
     * 解析SELECT字段
     */
    private void parseSelectColumns(String selectClause, SqlInfo sqlInfo) {
        List<ColumnInfo> columns = new ArrayList<>();
        String[] parts = selectClause.split(",");

        for (String part : parts) {
            part = part.trim();
            ColumnInfo column = new ColumnInfo();

            // 检查是否有别名
            if (part.toUpperCase().contains(" AS ")) {
                String[] aliasParts = part.split("\\s+(?i)AS\\s+");
                column.setColumnName(aliasParts[0].trim());
                column.setAlias(aliasParts[1].trim());
            } else {
                // 检查简单别名（空格分隔）
                String[] spaceParts = part.split("\\s+");
                if (spaceParts.length == 2 && !spaceParts[1].toUpperCase().matches("FROM|WHERE|GROUP|ORDER|LIMIT")) {
                    column.setColumnName(spaceParts[0].trim());
                    column.setAlias(spaceParts[1].trim());
                } else {
                    column.setColumnName(part);
                }
            }

            // 解析表别名
            if (column.getColumnName().contains(".")) {
                String[] tableParts = column.getColumnName().split("\\.");
                column.setTableAlias(tableParts[0]);
                column.setColumnName(tableParts[1]);
            }

            columns.add(column);
        }

        sqlInfo.setSelectColumns(columns);
    }

    /**
     * 解析FROM子句（包含JOIN）
     */
    private void parseFromClause(String fromClause, SqlInfo sqlInfo) {
        // 首先提取主表
        String[] parts = fromClause.split("\\s+(?i)(?:INNER\\s+JOIN|LEFT\\s+(?:OUTER\\s+)?JOIN|RIGHT\\s+(?:OUTER\\s+)?JOIN|FULL\\s+(?:OUTER\\s+)?JOIN|CROSS\\s+JOIN|JOIN)");
        String mainTablePart = parts[0].trim();

        // 解析主表
        TableInfo mainTable = parseTableInfo(mainTablePart);
        sqlInfo.setMainTable(mainTable);

        // 解析JOIN表
        List<JoinInfo> joinTables = parseJoinTables(fromClause);
        sqlInfo.setJoinTables(joinTables);
    }

    /**
     * 解析表信息
     */
    private TableInfo parseTableInfo(String tablePart) {
        tablePart = tablePart.trim();
        String[] parts = tablePart.split("\\s+");

        TableInfo table = new TableInfo();
        table.setTableName(parts[0]);

        if (parts.length > 1) {
            table.setAlias(parts[1]);
        }

        return table;
    }

    /**
     * 解析JOIN表
     */
    private List<JoinInfo> parseJoinTables(String fromClause) {
        List<JoinInfo> joinTables = new ArrayList<>();

        // 改进的JOIN正则表达式，支持多个JOIN
        Pattern joinPattern = Pattern.compile(
                "(INNER\\s+JOIN|LEFT\\s+(?:OUTER\\s+)?JOIN|RIGHT\\s+(?:OUTER\\s+)?JOIN|FULL\\s+(?:OUTER\\s+)?JOIN|CROSS\\s+JOIN|JOIN)\\s+" +
                        "(\\w+)(?:\\s+(\\w+))?(?:\\s+ON\\s+([^\\s]+(?:\\s*[=<>!]+\\s*[^\\s]+)?(?:\\s+AND\\s+[^\\s]+(?:\\s*[=<>!]+\\s*[^\\s]+)?)*?))?",
                Pattern.CASE_INSENSITIVE
        );

        Matcher matcher = joinPattern.matcher(fromClause);

        while (matcher.find()) {
            String joinTypeStr = matcher.group(1).toUpperCase().replaceAll("\\s+", "_");
            String tableName = matcher.group(2);
            String tableAlias = matcher.group(3);
            String joinConditionStr = matcher.group(4);

            // 解析JOIN类型
            JoinInfo.JoinType joinType;
            switch (joinTypeStr) {
                case "INNER_JOIN":
                case "JOIN":
                    joinType = JoinInfo.JoinType.INNER_JOIN;
                    break;
                case "LEFT_JOIN":
                case "LEFT_OUTER_JOIN":
                    joinType = JoinInfo.JoinType.LEFT_JOIN;
                    break;
                case "RIGHT_JOIN":
                case "RIGHT_OUTER_JOIN":
                    joinType = JoinInfo.JoinType.RIGHT_JOIN;
                    break;
                case "FULL_JOIN":
                case "FULL_OUTER_JOIN":
                    joinType = JoinInfo.JoinType.FULL_JOIN;
                    break;
                case "CROSS_JOIN":
                    joinType = JoinInfo.JoinType.CROSS_JOIN;
                    break;
                default:
                    joinType = JoinInfo.JoinType.INNER_JOIN;
            }

            // 创建JOIN信息
            JoinInfo joinInfo = new JoinInfo();
            joinInfo.setJoinType(joinType);
            joinInfo.setTableName(tableName);
            joinInfo.setAlias(tableAlias);

            // 解析JOIN条件
            if (joinConditionStr != null) {
                joinInfo.setCondition(joinConditionStr.trim());
            }

            joinTables.add(joinInfo);
        }

        return joinTables;
    }

    /**
     * 解析WHERE条件
     */
    private void parseWhereConditions(String whereClause, SqlInfo sqlInfo) {
        List<WhereCondition> conditions = new ArrayList<>();

        // 按AND分割条件
        String[] andParts = whereClause.split("\\s+(?i)AND\\s+");

        for (String part : andParts) {
            part = part.trim();

            // 进一步按OR分割
            String[] orParts = part.split("\\s+(?i)OR\\s+");

            for (String orPart : orParts) {
                WhereCondition condition = parseWhereCondition(orPart.trim());
                if (condition != null) {
                    conditions.add(condition);
                }
            }
        }

        sqlInfo.setWhereConditions(conditions);
    }

    /**
     * 解析单个WHERE条件
     */
    private WhereCondition parseWhereCondition(String conditionStr) {
        WhereCondition condition = new WhereCondition();
        conditionStr = conditionStr.trim();

        // 去掉括号
        if (conditionStr.startsWith("(") && conditionStr.endsWith(")")) {
            conditionStr = conditionStr.substring(1, conditionStr.length() - 1).trim();
        }

        // IS NULL / IS NOT NULL
        if (conditionStr.toUpperCase().matches(".*\\s+IS\\s+NOT\\s+NULL\\s*")) {
            String[] parts = conditionStr.split("\\s+(?i)IS\\s+NOT\\s+NULL");
            condition.setLeftOperand(parts[0].trim());
            condition.setOperator("IS NOT NULL");
            condition.setRightOperand(null);
        } else if (conditionStr.toUpperCase().matches(".*\\s+IS\\s+NULL\\s*")) {
            String[] parts = conditionStr.split("\\s+(?i)IS\\s+NULL");
            condition.setLeftOperand(parts[0].trim());
            condition.setOperator("IS NULL");
            condition.setRightOperand(null);
        }
        // BETWEEN
        else if (conditionStr.toUpperCase().contains(" BETWEEN ")) {
            String[] parts = conditionStr.split("\\s+(?i)BETWEEN\\s+");
            condition.setLeftOperand(parts[0].trim());
            condition.setOperator("BETWEEN");
            condition.setRightOperand(parts[1].trim());
        }
        // NOT BETWEEN
        else if (conditionStr.toUpperCase().contains(" NOT BETWEEN ")) {
            String[] parts = conditionStr.split("\\s+(?i)NOT\\s+BETWEEN\\s+");
            condition.setLeftOperand(parts[0].trim());
            condition.setOperator("NOT BETWEEN");
            condition.setRightOperand(parts[1].trim());
        }
        // IN
        else if (conditionStr.toUpperCase().contains(" IN ")) {
            String[] parts = conditionStr.split("\\s+(?i)IN\\s+");
            condition.setLeftOperand(parts[0].trim());
            condition.setOperator("IN");
            condition.setRightOperand(parts[1].trim());
        }
        // NOT IN
        else if (conditionStr.toUpperCase().contains(" NOT IN ")) {
            String[] parts = conditionStr.split("\\s+(?i)NOT\\s+IN\\s+");
            condition.setLeftOperand(parts[0].trim());
            condition.setOperator("NOT IN");
            condition.setRightOperand(parts[1].trim());
        }
        // LIKE
        else if (conditionStr.toUpperCase().contains(" LIKE ")) {
            String[] parts = conditionStr.split("\\s+(?i)LIKE\\s+");
            condition.setLeftOperand(parts[0].trim());
            condition.setOperator("LIKE");
            condition.setRightOperand(parts[1].trim());
        }
        // NOT LIKE
        else if (conditionStr.toUpperCase().contains(" NOT LIKE ")) {
            String[] parts = conditionStr.split("\\s+(?i)NOT\\s+LIKE\\s+");
            condition.setLeftOperand(parts[0].trim());
            condition.setOperator("NOT LIKE");
            condition.setRightOperand(parts[1].trim());
        }
        // 基本比较操作符
        else if (conditionStr.contains(">=")) {
            String[] parts = conditionStr.split(">=");
            condition.setLeftOperand(parts[0].trim());
            condition.setOperator(">=");
            condition.setRightOperand(parts[1].trim());
        } else if (conditionStr.contains("<=")) {
            String[] parts = conditionStr.split("<=");
            condition.setLeftOperand(parts[0].trim());
            condition.setOperator("<=");
            condition.setRightOperand(parts[1].trim());
        } else if (conditionStr.contains("<>")) {
            String[] parts = conditionStr.split("<>");
            condition.setLeftOperand(parts[0].trim());
            condition.setOperator("<>");
            condition.setRightOperand(parts[1].trim());
        } else if (conditionStr.contains("!=")) {
            String[] parts = conditionStr.split("!=");
            condition.setLeftOperand(parts[0].trim());
            condition.setOperator("!=");
            condition.setRightOperand(parts[1].trim());
        } else if (conditionStr.contains("=")) {
            String[] parts = conditionStr.split("=");
            condition.setLeftOperand(parts[0].trim());
            condition.setOperator("=");
            condition.setRightOperand(parts[1].trim());
        } else if (conditionStr.contains(">")) {
            String[] parts = conditionStr.split(">");
            condition.setLeftOperand(parts[0].trim());
            condition.setOperator(">");
            condition.setRightOperand(parts[1].trim());
        } else if (conditionStr.contains("<")) {
            String[] parts = conditionStr.split("<");
            condition.setLeftOperand(parts[0].trim());
            condition.setOperator("<");
            condition.setRightOperand(parts[1].trim());
        } else {
            return null; // 无法解析的条件
        }

        return condition;
    }

    /**
     * 解析GROUP BY
     */
    private void parseGroupBy(String groupByClause, SqlInfo sqlInfo) {
        List<String> groupByColumns = new ArrayList<>();
        String[] parts = groupByClause.split(",");

        for (String part : parts) {
            groupByColumns.add(part.trim());
        }

        sqlInfo.setGroupByColumns(groupByColumns);
    }

    /**
     * 解析ORDER BY
     */
    private void parseOrderBy(String orderByClause, SqlInfo sqlInfo) {
        List<OrderByInfo> orderByColumns = new ArrayList<>();
        String[] parts = orderByClause.split(",");

        for (String part : parts) {
            part = part.trim();
            OrderByInfo orderBy = new OrderByInfo();

            if (part.toUpperCase().endsWith(" DESC")) {
                orderBy.setColumnName(part.substring(0, part.length() - 5).trim());
                orderBy.setDirection(OrderByInfo.Direction.DESC);
            } else if (part.toUpperCase().endsWith(" ASC")) {
                orderBy.setColumnName(part.substring(0, part.length() - 4).trim());
                orderBy.setDirection(OrderByInfo.Direction.ASC);
            } else {
                orderBy.setColumnName(part);
                orderBy.setDirection(OrderByInfo.Direction.ASC); // 默认升序
            }

            orderByColumns.add(orderBy);
        }

        sqlInfo.setOrderByColumns(orderByColumns);
    }

    /**
     * 解析LIMIT
     */
    private void parseLimit(String limitClause, SqlInfo sqlInfo) {
        LimitInfo limitInfo = new LimitInfo();
        limitClause = limitClause.trim();

        if (limitClause.contains("OFFSET")) {
            // LIMIT n OFFSET m 格式
            String[] parts = limitClause.split("\\s+(?i)OFFSET\\s+");
            limitInfo.setLimit(Integer.parseInt(parts[0].trim()));
            limitInfo.setOffset(Integer.parseInt(parts[1].trim()));
        } else if (limitClause.contains(",")) {
            // LIMIT m, n 格式 (MySQL特有)
            String[] parts = limitClause.split(",");
            limitInfo.setOffset(Integer.parseInt(parts[0].trim()));
            limitInfo.setLimit(Integer.parseInt(parts[1].trim()));
        } else {
            // LIMIT n 格式
            limitInfo.setLimit(Integer.parseInt(limitClause));
            limitInfo.setOffset(0);
        }

        sqlInfo.setLimitInfo(limitInfo);
    }

    /**
     * 解析INSERT语句
     */
    private void parseInsert(String sql, SqlInfo sqlInfo) {
        sqlInfo.setSqlType(SqlInfo.SqlType.INSERT);

        Matcher matcher = INSERT_PATTERN.matcher(sql);
        if (matcher.find()) {
            String tableName = matcher.group(1);
            String columnsStr = matcher.group(2);
            String valuesStr = matcher.group(3);

            // 设置主表
            TableInfo mainTable = new TableInfo();
            mainTable.setTableName(tableName);
            sqlInfo.setMainTable(mainTable);

            // 解析字段
            List<String> columnNames = new ArrayList<>();
            String[] columnArray = columnsStr.split(",");
            for (String columnName : columnArray) {
                columnNames.add(columnName.trim());
            }
            sqlInfo.setInsertColumns(columnNames);

            // 解析值
            List<Object> values = new ArrayList<>();
            String[] valueArray = valuesStr.split(",");
            for (String value : valueArray) {
                values.add(value.trim());
            }
            sqlInfo.setInsertValues(values);
        }
    }

    /**
     * 解析UPDATE语句
     */
    private void parseUpdate(String sql, SqlInfo sqlInfo) {
        sqlInfo.setSqlType(SqlInfo.SqlType.UPDATE);

        Matcher matcher = UPDATE_PATTERN.matcher(sql);
        if (matcher.find()) {
            String tableInfo = matcher.group(1);
            String setClause = matcher.group(2);
            String whereClause = matcher.group(3);
            String limitClause = matcher.group(4);

            // 解析表信息
            TableInfo mainTable = parseTableInfo(tableInfo);
            sqlInfo.setMainTable(mainTable);

            // 解析SET子句
            Map<String, Object> updateValues = new HashMap<>();
            String[] setParts = setClause.split(",");
            for (String part : setParts) {
                String[] keyValue = part.split("=");
                if (keyValue.length == 2) {
                    updateValues.put(keyValue[0].trim(), keyValue[1].trim());
                }
            }
            sqlInfo.setUpdateValues(updateValues);

            // 解析WHERE条件
            if (whereClause != null) {
                parseWhereConditions(whereClause, sqlInfo);
            }

            // 解析LIMIT
            if (limitClause != null) {
                parseLimit(limitClause, sqlInfo);
            }
        }
    }

    /**
     * 解析DELETE语句
     */
    private void parseDelete(String sql, SqlInfo sqlInfo) {
        sqlInfo.setSqlType(SqlInfo.SqlType.DELETE);

        Matcher matcher = DELETE_PATTERN.matcher(sql);
        if (matcher.find()) {
            String tableInfo = matcher.group(1);
            String whereClause = matcher.group(2);
            String limitClause = matcher.group(3);

            // 解析表信息
            TableInfo mainTable = parseTableInfo(tableInfo);
            sqlInfo.setMainTable(mainTable);

            // 解析WHERE条件
            if (whereClause != null) {
                parseWhereConditions(whereClause, sqlInfo);
            }

            // 解析LIMIT
            if (limitClause != null) {
                parseLimit(limitClause, sqlInfo);
            }
        }
    }

    @Override
    public String toSql(SqlInfo sqlInfo) {
        StringBuilder sql = new StringBuilder();

        switch (sqlInfo.getSqlType()) {
            case SELECT:
                buildSelectSql(sqlInfo, sql);
                break;
            case INSERT:
                buildInsertSql(sqlInfo, sql);
                break;
            case UPDATE:
                buildUpdateSql(sqlInfo, sql);
                break;
            case DELETE:
                buildDeleteSql(sqlInfo, sql);
                break;
        }

        return sql.toString();
    }

    /**
     * 构建SELECT SQL
     */
    private void buildSelectSql(SqlInfo sqlInfo, StringBuilder sql) {
        sql.append("SELECT ");

        // SELECT字段
        if (sqlInfo.getSelectColumns() != null && !sqlInfo.getSelectColumns().isEmpty()) {
            for (int i = 0; i < sqlInfo.getSelectColumns().size(); i++) {
                if (i > 0) sql.append(", ");
                ColumnInfo column = sqlInfo.getSelectColumns().get(i);

                if (column.getTableAlias() != null) {
                    sql.append(column.getTableAlias()).append(".");
                }
                sql.append(column.getColumnName());

                if (column.getAlias() != null) {
                    sql.append(" AS ").append(column.getAlias());
                }
            }
        } else {
            sql.append("*");
        }

        // FROM子句
        sql.append(" FROM ").append(sqlInfo.getMainTable().getTableName());
        if (sqlInfo.getMainTable().getAlias() != null) {
            sql.append(" ").append(sqlInfo.getMainTable().getAlias());
        }

        // JOIN子句
        if (sqlInfo.getJoinTables() != null) {
            for (JoinInfo join : sqlInfo.getJoinTables()) {
                sql.append(" ").append(join.getJoinType().getSqlKeyword());
                sql.append(" ").append(join.getTableName());
                if (join.getAlias() != null) {
                    sql.append(" ").append(join.getAlias());
                }
                if (join.getCondition() != null) {
                    sql.append(" ON ").append(join.getCondition());
                }
            }
        }

        // WHERE子句
        if (sqlInfo.getWhereConditions() != null && !sqlInfo.getWhereConditions().isEmpty()) {
            sql.append(" WHERE ");
            for (int i = 0; i < sqlInfo.getWhereConditions().size(); i++) {
                if (i > 0) sql.append(" AND ");
                sql.append(buildConditionString(sqlInfo.getWhereConditions().get(i)));
            }
        }

        // GROUP BY子句
        if (sqlInfo.getGroupByColumns() != null && !sqlInfo.getGroupByColumns().isEmpty()) {
            sql.append(" GROUP BY ");
            for (int i = 0; i < sqlInfo.getGroupByColumns().size(); i++) {
                if (i > 0) sql.append(", ");
                sql.append(sqlInfo.getGroupByColumns().get(i));
            }
        }

        // HAVING子句
        if (sqlInfo.getHavingCondition() != null) {
            sql.append(" HAVING ").append(sqlInfo.getHavingCondition());
        }

        // ORDER BY子句
        if (sqlInfo.getOrderByColumns() != null && !sqlInfo.getOrderByColumns().isEmpty()) {
            sql.append(" ORDER BY ");
            for (int i = 0; i < sqlInfo.getOrderByColumns().size(); i++) {
                if (i > 0) sql.append(", ");
                OrderByInfo orderBy = sqlInfo.getOrderByColumns().get(i);
                sql.append(orderBy.getColumnName()).append(" ").append(orderBy.getDirection());
            }
        }

        // LIMIT子句
        if (sqlInfo.getLimitInfo() != null) {
            sql.append(" LIMIT ").append(sqlInfo.getLimitInfo().getLimit());
            if (sqlInfo.getLimitInfo().getOffset() > 0) {
                sql.append(" OFFSET ").append(sqlInfo.getLimitInfo().getOffset());
            }
        }
    }

    /**
     * 构建INSERT SQL
     */
    private void buildInsertSql(SqlInfo sqlInfo, StringBuilder sql) {
        sql.append("INSERT INTO ").append(sqlInfo.getMainTable().getTableName());

        if (sqlInfo.getInsertColumns() != null && !sqlInfo.getInsertColumns().isEmpty()) {
            sql.append(" (");
            for (int i = 0; i < sqlInfo.getInsertColumns().size(); i++) {
                if (i > 0) sql.append(", ");
                sql.append(sqlInfo.getInsertColumns().get(i));
            }
            sql.append(") VALUES (");

            for (int i = 0; i < sqlInfo.getInsertValues().size(); i++) {
                if (i > 0) sql.append(", ");
                Object value = sqlInfo.getInsertValues().get(i);
                sql.append(value != null ? value.toString() : "NULL");
            }
            sql.append(")");
        }
    }

    /**
     * 构建UPDATE SQL
     */
    private void buildUpdateSql(SqlInfo sqlInfo, StringBuilder sql) {
        sql.append("UPDATE ").append(sqlInfo.getMainTable().getTableName());
        if (sqlInfo.getMainTable().getAlias() != null) {
            sql.append(" ").append(sqlInfo.getMainTable().getAlias());
        }

        if (sqlInfo.getUpdateValues() != null && !sqlInfo.getUpdateValues().isEmpty()) {
            sql.append(" SET ");
            int i = 0;
            for (Map.Entry<String, Object> entry : sqlInfo.getUpdateValues().entrySet()) {
                if (i > 0) sql.append(", ");
                sql.append(entry.getKey()).append(" = ").append(entry.getValue());
                i++;
            }
        }

        // WHERE子句
        if (sqlInfo.getWhereConditions() != null && !sqlInfo.getWhereConditions().isEmpty()) {
            sql.append(" WHERE ");
            for (int j = 0; j < sqlInfo.getWhereConditions().size(); j++) {
                if (j > 0) sql.append(" AND ");
                sql.append(buildConditionString(sqlInfo.getWhereConditions().get(j)));
            }
        }
    }

    /**
     * 构建DELETE SQL
     */
    private void buildDeleteSql(SqlInfo sqlInfo, StringBuilder sql) {
        sql.append("DELETE FROM ").append(sqlInfo.getMainTable().getTableName());

        // WHERE子句
        if (sqlInfo.getWhereConditions() != null && !sqlInfo.getWhereConditions().isEmpty()) {
            sql.append(" WHERE ");
            for (int i = 0; i < sqlInfo.getWhereConditions().size(); i++) {
                if (i > 0) sql.append(" AND ");
                sql.append(buildConditionString(sqlInfo.getWhereConditions().get(i)));
            }
        }
    }

    /**
     * 构建条件字符串
     */
    private String buildConditionString(WhereCondition condition) {
        StringBuilder sb = new StringBuilder();
        sb.append(condition.getLeftOperand());
        sb.append(" ").append(condition.getOperator());

        if (condition.getRightOperand() != null) {
            sb.append(" ").append(condition.getRightOperand());
        }

        return sb.toString();
    }
}