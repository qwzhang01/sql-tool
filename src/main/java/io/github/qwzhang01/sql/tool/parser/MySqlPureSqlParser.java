package io.github.qwzhang01.sql.tool.parser;

import io.github.qwzhang01.sql.tool.enums.Direction;
import io.github.qwzhang01.sql.tool.enums.JoinType;
import io.github.qwzhang01.sql.tool.enums.SqlType;
import io.github.qwzhang01.sql.tool.exception.ParseException;
import io.github.qwzhang01.sql.tool.exception.UnSupportedException;
import io.github.qwzhang01.sql.tool.model.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * MySQL-specific SQL parser implementation that provides comprehensive parsing
 * capabilities for MySQL SQL statements. This parser can extract detailed information
 * about tables, columns, WHERE conditions, JOIN clauses, ORDER BY, LIMIT, and more.
 *
 * <p>Key features:</p>
 * <ul>
 *   <li>Supports MySQL-specific syntax including backtick identifiers</li>
 *   <li>Parses complex WHERE conditions with detailed field information</li>
 *   <li>Handles various JOIN types (INNER, LEFT, RIGHT, FULL, CROSS)</li>
 *   <li>Extracts ORDER BY and LIMIT clauses</li>
 *   <li>Provides detailed field analysis with table names and aliases</li>
 *   <li>Supports subqueries and complex expressions</li>
 * </ul>
 *
 * @author Avin Zhang
 * @since 1.0.0
 */
public class MySqlPureSqlParser implements SqlParser {
    // SQL keyword regular expressions
    private static final Pattern SELECT_PATTERN = Pattern.compile("^\\s*SELECT\\s+(.+?)\\s+FROM\\s+(.+?)(?:\\s+WHERE\\s+(.+?))?(?:\\s+GROUP\\s+BY\\s+(.+?))?(?:\\s+HAVING\\s+(.+?))?(?:\\s+ORDER\\s+BY\\s+(.+?))?(?:\\s+LIMIT\\s+(.+?))?\\s*$", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern INSERT_PATTERN = Pattern.compile("^\\s*INSERT\\s+INTO\\s+(\\w+)\\s*\\(([^)]+)\\)\\s*VALUES\\s*\\(([^)]+)\\)\\s*$", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern UPDATE_PATTERN = Pattern.compile("^\\s*UPDATE\\s+(\\w+(?:\\s+\\w+)?)\\s+SET\\s+(.+?)(?:\\s+WHERE\\s+(.+?))?(?:\\s+LIMIT\\s+(.+?))?\\s*$", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern DELETE_PATTERN = Pattern.compile("^\\s*DELETE\\s+FROM\\s+(\\w+(?:\\s+\\w+)?)(?:\\s+WHERE\\s+(.+?))?(?:\\s+LIMIT\\s+(.+?))?\\s*$", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern JOIN_PATTERN = Pattern.compile("(INNER\\s+JOIN|LEFT\\s+(?:OUTER\\s+)?JOIN|RIGHT\\s+(?:OUTER\\s+)?JOIN|FULL\\s+(?:OUTER\\s+)?JOIN|CROSS\\s+JOIN|JOIN)\\s+" + "(`?[\\w.]+`?)(?:\\s+(`?\\w+`?))?(?:\\s+ON\\s+(.+?))?(?=\\s+(?:INNER\\s+JOIN|LEFT\\s+(?:OUTER\\s+)?JOIN|RIGHT\\s+(?:OUTER\\s+)?JOIN|FULL\\s+(?:OUTER\\s+)?JOIN|CROSS\\s+JOIN|JOIN|WHERE|GROUP\\s+BY|ORDER\\s+BY|LIMIT|$)|$)", Pattern.CASE_INSENSITIVE);

    private static final Set<String> JOIN = Set.of("INNER JOIN", "JOIN", "LEFT JOIN", "LEFT OUTER JOIN", "RIGHT JOIN", "RIGHT OUTER JOIN", "FULL JOIN", "FULL OUTER JOIN", "CROSS JOIN");
    private SqlCleaner sqlCleaner = null;
    private SqlCompare compare = null;

    @Override
    public SqlObj parse(String sql) {
        if (sql == null || sql.isEmpty() || sql.trim().isEmpty()) {
            throw new UnSupportedException("SQL cannot be null or empty");
        }

        SqlObj sqlObj = new SqlObj();
        sqlObj.setOriginalSql(sql);

        // Clean SQL comments and extra whitespace characters
        sqlObj.setOriginalSql(getCleaner().cleanSql(sqlObj.getOriginalSql()).trim());

        try {
            if (sqlObj.getOriginalSql().toUpperCase().startsWith("SELECT")) {
                parseSelect(sqlObj);
            } else if (sqlObj.getOriginalSql().toUpperCase().startsWith("INSERT")) {
                parseInsert(sqlObj);
            } else if (sqlObj.getOriginalSql().toUpperCase().startsWith("UPDATE")) {
                parseUpdate(sqlObj);
            } else if (sqlObj.getOriginalSql().toUpperCase().startsWith("DELETE")) {
                parseDelete(sqlObj);
            } else {
                throw new IllegalArgumentException("不支持的SQL类型: " + sql);
            }

            return sqlObj;
        } catch (IllegalArgumentException e) {
            throw new UnSupportedException(e.getMessage(), e);
        } catch (Exception e) {
            throw new ParseException("解析SQL失败: " + sql, e);
        }
    }

    @Override
    public List<SqlJoin> parseJoin(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            throw new IllegalArgumentException("SQL不能为空");
        }

        sql = getCleaner().cleanSql(sql).trim();

        // 如果直接是JOIN语句
        if (containsJoin(sql)) {
            return parseJoinTables(sql);
        }

        // 没有JOIN，返回空列表
        return new ArrayList<>();
    }

    private boolean containsJoin(String sql) {
        String upperSql = sql.toUpperCase();
        for (String join : JOIN) {
            if (upperSql.startsWith(join)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<SqlCondition> parseWhere(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            throw new IllegalArgumentException("SQL不能为空");
        }

        SqlObj sqlObj = new SqlObj();
        sqlObj.setOriginalSql(sql);
        // 清理SQL中的注释和多余空白字符
        sqlObj.setOriginalSql(getCleaner().cleanSql(sqlObj.getOriginalSql()).trim());
        if (!sqlObj.getOriginalSql().toUpperCase().startsWith("WHERE")) {
            throw new IllegalArgumentException("不支持的JOIN类型: " + sql);
        }
        parseWhereConditions(sqlObj.getOriginalSql().substring(5).trim(), sqlObj);
        return sqlObj.getWhereConditions();
    }

    /**
     * 解析SELECT语句
     */
    private void parseSelect(SqlObj sqlObj) {
        String sql = sqlObj.getOriginalSql();
        sqlObj.setSqlType(SqlType.SELECT);

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
            sqlObj.setSelectColumns(parseSelectColumns(selectClause));

            // 解析FROM子句（包含JOIN）
            parseFromClause(fromClause, sqlObj);

            // 解析WHERE条件
            if (whereClause != null) {
                parseWhereConditions(whereClause, sqlObj);
            }

            // 解析GROUP BY
            if (groupByClause != null) {
                parseGroupBy(groupByClause, sqlObj);
            }

            // 解析HAVING
            if (havingClause != null) {
                sqlObj.setHavingCondition(havingClause.trim());
            }

            // 解析ORDER BY
            if (orderByClause != null) {
                parseOrderBy(orderByClause, sqlObj);
            }

            // 解析LIMIT
            if (limitClause != null) {
                parseLimit(limitClause, sqlObj);
            }
        }
    }

    /**
     * 解析SELECT字段
     */
    private List<SqlField> parseSelectColumns(String selectClause) {
        List<SqlField> columns = new ArrayList<>();
        String[] parts = selectClause.split(",");

        for (String part : parts) {
            part = part.trim();
            SqlField column = new SqlField();

            // 检查是否有别名
            if (part.toUpperCase().contains(" AS ")) {
                String[] aliasParts = part.split("\\s+(?i)AS\\s+");
                column.setFieldName(aliasParts[0].trim());
                column.setAlias(aliasParts[1].trim());
            } else {
                // 检查简单别名（空格分隔）
                String[] spaceParts = part.split("\\s+");
                if (spaceParts.length == 2 && !spaceParts[1].toUpperCase().matches("FROM|WHERE|GROUP|ORDER|LIMIT")) {
                    column.setFieldName(spaceParts[0].trim());
                    column.setAlias(spaceParts[1].trim());
                } else {
                    column.setFieldName(part);
                }
            }

            // 解析表别名
            if (column.getFieldName().contains(".")) {
                String[] tableParts = column.getFieldName().split("\\.");
                column.setTableAlias(tableParts[0]);
                column.setFieldName(tableParts[1]);
            }

            columns.add(column);
        }

        return columns;
    }

    /**
     * 解析FROM子句（包含JOIN）
     */
    private void parseFromClause(String fromClause, SqlObj sqlObj) {
        // 首先提取主表
        String[] parts = fromClause.split("\\s+(?i)(?:INNER\\s+JOIN|LEFT\\s+(?:OUTER\\s+)?JOIN|RIGHT\\s+(?:OUTER\\s+)?JOIN|FULL\\s+(?:OUTER\\s+)?JOIN|CROSS\\s+JOIN|JOIN)");
        String mainTablePart = parts[0].trim();

        // 解析主表
        SqlTable mainTable = parseTableInfo(mainTablePart);
        sqlObj.setMainTable(mainTable);

        // 解析JOIN表
        List<SqlJoin> joinTables = parseJoinTables(fromClause);
        sqlObj.setJoinTables(joinTables);
    }

    /**
     * 解析表信息
     */
    private SqlTable parseTableInfo(String tablePart) {
        tablePart = tablePart.trim();
        String[] parts = tablePart.split("\\s+");

        SqlTable table = new SqlTable();
        table.setTableName(parts[0]);

        if (parts.length > 1) {
            table.setAlias(parts[1]);
        }

        return table;
    }

    /**
     * 解析JOIN表
     */
    private List<SqlJoin> parseJoinTables(String fromClause) {
        Matcher matcher = JOIN_PATTERN.matcher(fromClause);
        List<SqlJoin> joinTables = new ArrayList<>();
        while (matcher.find()) {
            String joinTypeStr = matcher.group(1).toUpperCase().replaceAll("\\s+", "_");
            String tableName = matcher.group(2);
            String tableAlias = matcher.group(3);
            String joinConditionStr = matcher.group(4);

            // 解析JOIN类型
            JoinType joinType = switch (joinTypeStr) {
                case "INNER_JOIN", "JOIN" -> JoinType.INNER_JOIN;
                case "LEFT_JOIN", "LEFT_OUTER_JOIN" -> JoinType.LEFT_JOIN;
                case "RIGHT_JOIN", "RIGHT_OUTER_JOIN" -> JoinType.RIGHT_JOIN;
                case "FULL_JOIN", "FULL_OUTER_JOIN" -> JoinType.FULL_JOIN;
                case "CROSS_JOIN" -> JoinType.CROSS_JOIN;
                default -> JoinType.INNER_JOIN;
            };

            // 创建JOIN信息
            SqlJoin sqlJoin = new SqlJoin();
            sqlJoin.setJoinType(joinType);
            // 去掉反引号
            sqlJoin.setTableName(tableName != null ? tableName.replaceAll("`", "") : null);
            sqlJoin.setAlias(tableAlias != null ? tableAlias.replaceAll("`", "") : null);

            // 解析JOIN条件
            if (joinConditionStr != null) {
                sqlJoin.setCondition(joinConditionStr.trim());

                // 解析详细的JOIN条件
                List<SqlCondition> joinConditions = parseJoinConditions(joinConditionStr.trim());
                sqlJoin.setJoinConditions(joinConditions);
            }

            joinTables.add(sqlJoin);
        }

        return joinTables;
    }

    /**
     * 解析JOIN条件
     */
    private List<SqlCondition> parseJoinConditions(String joinConditionStr) {
        List<SqlCondition> conditions = new ArrayList<>();

        // 智能分割条件，避免破坏 BETWEEN...AND 结构
        List<String> andParts = smartSplitByAnd(joinConditionStr);

        for (String part : andParts) {
            part = part.trim();

            // 进一步按OR分割
            String[] orParts = part.split("\\s+(?i)OR\\s+");

            for (String orPart : orParts) {
                SqlCondition condition = parseWhereCondition(orPart.trim());
                if (condition != null) {
                    conditions.add(condition);
                }
            }
        }

        return conditions;
    }

    /**
     * 解析WHERE条件
     */
    private void parseWhereConditions(String whereClause, SqlObj sqlObj) {
        List<SqlCondition> conditions = new ArrayList<>();

        // 智能分割条件，避免破坏 BETWEEN...AND 结构
        List<String> andParts = smartSplitByAnd(whereClause);

        for (String part : andParts) {
            part = part.trim();

            // 进一步按OR分割
            String[] orParts = part.split("\\s+(?i)OR\\s+");

            for (String orPart : orParts) {
                SqlCondition condition = parseWhereCondition(orPart.trim());
                if (condition != null) {
                    conditions.add(condition);
                }
            }
        }

        sqlObj.setWhereConditions(conditions);
    }

    /**
     * 智能分割 WHERE 子句，避免破坏 BETWEEN...AND 结构
     */
    private List<String> smartSplitByAnd(String whereClause) {
        List<String> parts = new ArrayList<>();
        StringBuilder currentPart = new StringBuilder();

        String upperClause = whereClause.toUpperCase();
        int i = 0;

        while (i < whereClause.length()) {
            // 检查是否遇到 AND
            if (i <= whereClause.length() - 4 && upperClause.substring(i, i + 4).equals(" AND")) {
                // 检查这个 AND 是否在 BETWEEN...AND 结构中
                String beforeAnd = currentPart.toString().toUpperCase();

                // 如果当前部分包含 BETWEEN 或 NOT BETWEEN，且没有对应的 AND，则这个 AND 属于 BETWEEN
                boolean inBetween = false;
                if (beforeAnd.contains(" BETWEEN ") || beforeAnd.contains(" NOT BETWEEN ")) {
                    // 简单检查：如果已经有了一个 AND，则当前 AND 是逻辑连接符
                    // 如果没有 AND，则当前 AND 是 BETWEEN 的一部分
                    int betweenCount = countOccurrences(beforeAnd, " BETWEEN ");
                    int notBetweenCount = countOccurrences(beforeAnd, " NOT BETWEEN ");
                    int andCount = countOccurrences(beforeAnd, " AND ");

                    // 如果 BETWEEN 的数量大于 AND 的数量，说明当前 AND 属于 BETWEEN
                    inBetween = (betweenCount + notBetweenCount) > andCount;
                }

                if (inBetween) {
                    // 这个 AND 是 BETWEEN 的一部分，添加到当前部分
                    currentPart.append(whereClause.substring(i, i + 4));
                    i += 4;
                } else {
                    // 这个 AND 是逻辑连接符，分割条件
                    parts.add(currentPart.toString().trim());
                    currentPart = new StringBuilder();
                    i += 4; // 跳过 " AND"
                    // 跳过后续空格
                    while (i < whereClause.length() && Character.isWhitespace(whereClause.charAt(i))) {
                        i++;
                    }
                }
            } else {
                currentPart.append(whereClause.charAt(i));
                i++;
            }
        }

        // 添加最后一部分
        if (currentPart.length() > 0) {
            parts.add(currentPart.toString().trim());
        }

        return parts;
    }

    /**
     * 计算字符串中子串的出现次数
     */
    private int countOccurrences(String text, String pattern) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(pattern, index)) != -1) {
            count++;
            index += pattern.length();
        }
        return count;
    }

    /**
     * 解析单个WHERE条件
     */
    private SqlCondition parseWhereCondition(String conditionStr) {
        SqlCondition condition = new SqlCondition();
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
        // NOT BETWEEN (必须在 BETWEEN 之前检查)
        else if (conditionStr.toUpperCase().contains(" NOT BETWEEN ")) {
            String[] parts = conditionStr.split("\\s+(?i)NOT\\s+BETWEEN\\s+", 2);
            condition.setLeftOperand(parts[0].trim());
            condition.setOperator("NOT BETWEEN");
            condition.setRightOperand(parts[1].trim());
        }
        // BETWEEN
        else if (conditionStr.toUpperCase().contains(" BETWEEN ")) {
            String[] parts = conditionStr.split("\\s+(?i)BETWEEN\\s+", 2);
            condition.setLeftOperand(parts[0].trim());
            condition.setOperator("BETWEEN");
            condition.setRightOperand(parts[1].trim());
        }
        // NOT IN (必须在 IN 之前检查)
        else if (conditionStr.toUpperCase().contains(" NOT IN ")) {
            String[] parts = conditionStr.split("\\s+(?i)NOT\\s+IN\\s+");
            condition.setLeftOperand(parts[0].trim());
            condition.setOperator("NOT IN");
            condition.setRightOperand(parts[1].trim());
        }
        // IN
        else if (conditionStr.toUpperCase().contains(" IN ")) {
            String[] parts = conditionStr.split("\\s+(?i)IN\\s+");
            condition.setLeftOperand(parts[0].trim());
            condition.setOperator("IN");
            condition.setRightOperand(parts[1].trim());
        }
        // NOT LIKE (必须在 LIKE 之前检查)
        else if (conditionStr.toUpperCase().contains(" NOT LIKE ")) {
            String[] parts = conditionStr.split("\\s+(?i)NOT\\s+LIKE\\s+");
            condition.setLeftOperand(parts[0].trim());
            condition.setOperator("NOT LIKE");
            condition.setRightOperand(parts[1].trim());
        }
        // LIKE
        else if (conditionStr.toUpperCase().contains(" LIKE ")) {
            String[] parts = conditionStr.split("\\s+(?i)LIKE\\s+");
            condition.setLeftOperand(parts[0].trim());
            condition.setOperator("LIKE");
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
            // 无法解析的条件
            return null;
        }

        return condition;
    }

    /**
     * 解析GROUP BY
     */
    private void parseGroupBy(String groupByClause, SqlObj sqlObj) {
        List<String> groupByColumns = new ArrayList<>();
        String[] parts = groupByClause.split(",");

        for (String part : parts) {
            groupByColumns.add(part.trim());
        }

        sqlObj.setGroupByColumns(groupByColumns);
    }

    /**
     * 解析ORDER BY
     */
    private void parseOrderBy(String orderByClause, SqlObj sqlObj) {
        List<SqlOrderBy> orderByColumns = new ArrayList<>();
        String[] parts = orderByClause.split(",");

        for (String part : parts) {
            part = part.trim();
            SqlOrderBy orderBy = new SqlOrderBy();

            if (part.toUpperCase().endsWith(" DESC")) {
                orderBy.setColumnName(part.substring(0, part.length() - 5).trim());
                orderBy.setDirection(Direction.DESC);
            } else if (part.toUpperCase().endsWith(" ASC")) {
                orderBy.setColumnName(part.substring(0, part.length() - 4).trim());
                orderBy.setDirection(Direction.ASC);
            } else {
                orderBy.setColumnName(part);
                orderBy.setDirection(Direction.ASC); // 默认升序
            }

            orderByColumns.add(orderBy);
        }

        sqlObj.setOrderByColumns(orderByColumns);
    }

    /**
     * 解析LIMIT
     */
    private void parseLimit(String limitClause, SqlObj sqlObj) {
        SqlLimit sqlLimit = new SqlLimit();
        limitClause = limitClause.trim();

        if (limitClause.contains("OFFSET")) {
            // LIMIT n OFFSET m 格式
            String[] parts = limitClause.split("\\s+(?i)OFFSET\\s+");
            sqlLimit.setLimit(Integer.parseInt(parts[0].trim()));
            sqlLimit.setOffset(Integer.parseInt(parts[1].trim()));
        } else if (limitClause.contains(",")) {
            // LIMIT m, n 格式 (MySQL特有)
            String[] parts = limitClause.split(",");
            sqlLimit.setOffset(Integer.parseInt(parts[0].trim()));
            sqlLimit.setLimit(Integer.parseInt(parts[1].trim()));
        } else {
            // LIMIT n 格式
            sqlLimit.setLimit(Integer.parseInt(limitClause));
            sqlLimit.setOffset(0);
        }

        sqlObj.setLimitInfo(sqlLimit);
    }

    /**
     * 解析INSERT语句
     */
    private void parseInsert(SqlObj sqlObj) {
        String sql = sqlObj.getOriginalSql();

        sqlObj.setSqlType(SqlType.INSERT);

        Matcher matcher = INSERT_PATTERN.matcher(sql);
        if (matcher.find()) {
            String tableName = matcher.group(1);
            String columnsStr = matcher.group(2);
            String valuesStr = matcher.group(3);

            // 设置主表
            SqlTable mainTable = new SqlTable();
            mainTable.setTableName(tableName);
            sqlObj.setMainTable(mainTable);

            // 解析字段
            List<String> columnNames = new ArrayList<>();
            String[] columnArray = columnsStr.split(",");
            for (String columnName : columnArray) {
                columnNames.add(columnName.trim());
            }
            sqlObj.setInsertColumns(columnNames);

            // 解析值
            List<Object> values = new ArrayList<>();
            String[] valueArray = valuesStr.split(",");
            for (String value : valueArray) {
                values.add(value.trim());
            }
            sqlObj.setInsertValues(values);
        }
    }

    /**
     * 解析UPDATE语句
     */
    private void parseUpdate(SqlObj sqlObj) {
        String sql = sqlObj.getOriginalSql();

        sqlObj.setSqlType(SqlType.UPDATE);

        Matcher matcher = UPDATE_PATTERN.matcher(sql);
        if (matcher.find()) {
            String tableInfo = matcher.group(1);
            String setClause = matcher.group(2);
            String whereClause = matcher.group(3);
            String limitClause = matcher.group(4);

            // 解析表信息
            SqlTable mainTable = parseTableInfo(tableInfo);
            sqlObj.setMainTable(mainTable);

            // 解析SET子句
            Map<String, Object> updateValues = new HashMap<>();
            String[] setParts = setClause.split(",");
            for (String part : setParts) {
                String[] keyValue = part.split("=");
                if (keyValue.length == 2) {
                    updateValues.put(keyValue[0].trim(), keyValue[1].trim());
                }
            }
            sqlObj.setUpdateValues(updateValues);

            // 解析WHERE条件
            if (whereClause != null) {
                parseWhereConditions(whereClause, sqlObj);
            }

            // 解析LIMIT
            if (limitClause != null) {
                parseLimit(limitClause, sqlObj);
            }
        }
    }

    /**
     * 解析DELETE语句
     */
    private void parseDelete(SqlObj sqlObj) {
        String sql = sqlObj.getOriginalSql();
        sqlObj.setSqlType(SqlType.DELETE);

        Matcher matcher = DELETE_PATTERN.matcher(sql);
        if (matcher.find()) {
            String tableInfo = matcher.group(1);
            String whereClause = matcher.group(2);
            String limitClause = matcher.group(3);

            // 解析表信息
            SqlTable mainTable = parseTableInfo(tableInfo);
            sqlObj.setMainTable(mainTable);

            // 解析WHERE条件
            if (whereClause != null) {
                parseWhereConditions(whereClause, sqlObj);
            }

            // 解析LIMIT
            if (limitClause != null) {
                parseLimit(limitClause, sqlObj);
            }
        }
    }

    @Override
    public String toSql(SqlObj sqlObj) {
        StringBuilder sql = new StringBuilder();

        switch (sqlObj.getSqlType()) {
            case SELECT:
                buildSelectSql(sqlObj, sql);
                break;
            case INSERT:
                buildInsertSql(sqlObj, sql);
                break;
            case UPDATE:
                buildUpdateSql(sqlObj, sql);
                break;
            case DELETE:
                buildDeleteSql(sqlObj, sql);
                break;
        }

        return sql.toString();
    }

    /**
     * 构建SELECT SQL
     */
    private void buildSelectSql(SqlObj sqlObj, StringBuilder sql) {
        sql.append("SELECT ");

        // SELECT字段
        if (sqlObj.getSelectColumns() != null && !sqlObj.getSelectColumns().isEmpty()) {
            for (int i = 0; i < sqlObj.getSelectColumns().size(); i++) {
                if (i > 0) {
                    sql.append(", ");
                }
                SqlField column = sqlObj.getSelectColumns().get(i);

                if (column.getTableAlias() != null) {
                    sql.append(column.getTableAlias()).append(".");
                }
                sql.append(column.getFieldName());

                if (column.getAlias() != null) {
                    sql.append(" AS ").append(column.getAlias());
                }
            }
        } else {
            sql.append("*");
        }

        // FROM子句
        sql.append(" FROM ").append(sqlObj.getMainTable().getTableName());
        if (sqlObj.getMainTable().getAlias() != null) {
            sql.append(" ").append(sqlObj.getMainTable().getAlias());
        }

        // JOIN子句
        if (sqlObj.getJoinTables() != null) {
            for (SqlJoin join : sqlObj.getJoinTables()) {
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
        if (sqlObj.getWhereConditions() != null && !sqlObj.getWhereConditions().isEmpty()) {
            sql.append(" WHERE ");
            for (int i = 0; i < sqlObj.getWhereConditions().size(); i++) {
                if (i > 0) {
                    sql.append(" AND ");
                }
                sql.append(buildConditionString(sqlObj.getWhereConditions().get(i)));
            }
        }

        // GROUP BY子句
        if (sqlObj.getGroupByColumns() != null && !sqlObj.getGroupByColumns().isEmpty()) {
            sql.append(" GROUP BY ");
            for (int i = 0; i < sqlObj.getGroupByColumns().size(); i++) {
                if (i > 0) {
                    sql.append(", ");
                }
                sql.append(sqlObj.getGroupByColumns().get(i));
            }
        }

        // HAVING子句
        if (sqlObj.getHavingCondition() != null) {
            sql.append(" HAVING ").append(sqlObj.getHavingCondition());
        }

        // ORDER BY子句
        if (sqlObj.getOrderByColumns() != null && !sqlObj.getOrderByColumns().isEmpty()) {
            sql.append(" ORDER BY ");
            for (int i = 0; i < sqlObj.getOrderByColumns().size(); i++) {
                if (i > 0) {
                    sql.append(", ");
                }
                SqlOrderBy orderBy = sqlObj.getOrderByColumns().get(i);
                sql.append(orderBy.getColumnName()).append(" ").append(orderBy.getDirection());
            }
        }

        // LIMIT子句
        if (sqlObj.getLimitInfo() != null) {
            sql.append(" LIMIT ").append(sqlObj.getLimitInfo().getLimit());
            if (sqlObj.getLimitInfo().getOffset() > 0) {
                sql.append(" OFFSET ").append(sqlObj.getLimitInfo().getOffset());
            }
        }
    }

    /**
     * 构建INSERT SQL
     */
    private void buildInsertSql(SqlObj sqlObj, StringBuilder sql) {
        sql.append("INSERT INTO ").append(sqlObj.getMainTable().getTableName());

        if (sqlObj.getInsertColumns() != null && !sqlObj.getInsertColumns().isEmpty()) {
            sql.append(" (");
            for (int i = 0; i < sqlObj.getInsertColumns().size(); i++) {
                if (i > 0) {
                    sql.append(", ");
                }
                sql.append(sqlObj.getInsertColumns().get(i));
            }
            sql.append(") VALUES (");

            for (int i = 0; i < sqlObj.getInsertValues().size(); i++) {
                if (i > 0) {
                    sql.append(", ");
                }
                Object value = sqlObj.getInsertValues().get(i);
                sql.append(value != null ? value.toString() : "NULL");
            }
            sql.append(")");
        }
    }

    /**
     * 构建UPDATE SQL
     */
    private void buildUpdateSql(SqlObj sqlObj, StringBuilder sql) {
        sql.append("UPDATE ").append(sqlObj.getMainTable().getTableName());
        if (sqlObj.getMainTable().getAlias() != null) {
            sql.append(" ").append(sqlObj.getMainTable().getAlias());
        }

        if (sqlObj.getUpdateValues() != null && !sqlObj.getUpdateValues().isEmpty()) {
            sql.append(" SET ");
            int i = 0;
            for (Map.Entry<String, Object> entry : sqlObj.getUpdateValues().entrySet()) {
                if (i > 0) {
                    sql.append(", ");
                }
                sql.append(entry.getKey()).append(" = ").append(entry.getValue());
                i++;
            }
        }

        // WHERE子句
        if (sqlObj.getWhereConditions() != null && !sqlObj.getWhereConditions().isEmpty()) {
            sql.append(" WHERE ");
            for (int j = 0; j < sqlObj.getWhereConditions().size(); j++) {
                if (j > 0) {
                    sql.append(" AND ");
                }
                sql.append(buildConditionString(sqlObj.getWhereConditions().get(j)));
            }
        }
    }

    /**
     * 构建DELETE SQL
     */
    private void buildDeleteSql(SqlObj sqlObj, StringBuilder sql) {
        sql.append("DELETE FROM ").append(sqlObj.getMainTable().getTableName());

        // WHERE子句
        if (sqlObj.getWhereConditions() != null && !sqlObj.getWhereConditions().isEmpty()) {
            sql.append(" WHERE ");
            for (int i = 0; i < sqlObj.getWhereConditions().size(); i++) {
                if (i > 0) {
                    sql.append(" AND ");
                }
                sql.append(buildConditionString(sqlObj.getWhereConditions().get(i)));
            }
        }
    }

    /**
     * 构建条件字符串
     */
    private String buildConditionString(SqlCondition condition) {
        StringBuilder sb = new StringBuilder();
        sb.append(condition.getLeftOperand());
        sb.append(" ").append(condition.getOperator());

        if (condition.getRightOperand() != null) {
            sb.append(" ").append(condition.getRightOperand());
        }

        return sb.toString();
    }

    @Override
    public SqlCleaner getCleaner() {
        if (this.sqlCleaner != null) {
            return this.sqlCleaner;
        }
        synchronized (this) {
            if (this.sqlCleaner != null) {
                return this.sqlCleaner;
            }
            this.sqlCleaner = new MySqlSqlCleaner();
            return this.sqlCleaner;
        }
    }

    @Override
    public SqlCompare getCompare() {
        if (this.compare != null) {
            return this.compare;
        }
        synchronized (this) {
            if (this.compare != null) {
                return this.compare;
            }
            this.compare = new MySqlSqlCompare();
            return this.compare;
        }
    }
}