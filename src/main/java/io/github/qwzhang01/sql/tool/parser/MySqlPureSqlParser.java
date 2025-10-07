package io.github.qwzhang01.sql.tool.parser;

import io.github.qwzhang01.sql.tool.enums.Direction;
import io.github.qwzhang01.sql.tool.enums.JoinType;
import io.github.qwzhang01.sql.tool.enums.SqlType;
import io.github.qwzhang01.sql.tool.exception.ParseException;
import io.github.qwzhang01.sql.tool.exception.UnSupportedException;
import io.github.qwzhang01.sql.tool.model.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
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
                throw new IllegalArgumentException("Unsupported SQL type: " + sql);
            }

            return sqlObj;
        } catch (IllegalArgumentException e) {
            throw new UnSupportedException(e.getMessage(), e);
        } catch (Exception e) {
            throw new ParseException("Failed to parse SQL: " + sql, e);
        }
    }

    @Override
    public List<SqlJoin> parseJoin(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            throw new IllegalArgumentException("SQL cannot be null or empty");
        }

        sql = getCleaner().cleanSql(sql).trim();

        // If it's a direct JOIN statement
        if (containsJoin(sql)) {
            return parseJoinTables(sql);
        }

        // No JOIN, return empty list
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
            throw new IllegalArgumentException("SQL cannot be null or empty");
        }

        SqlObj sqlObj = new SqlObj();
        sqlObj.setOriginalSql(sql);
        // Clean comments and extra whitespace in SQL
        sqlObj.setOriginalSql(getCleaner().cleanSql(sqlObj.getOriginalSql()).trim());
        if (!sqlObj.getOriginalSql().toUpperCase().startsWith("WHERE")) {
            throw new IllegalArgumentException("Unsupported JOIN type: " + sql);
        }
        parseWhereConditions(sqlObj.getOriginalSql().substring(5).trim(), sqlObj);
        return sqlObj.getWhereConditions();
    }

    /**
     * Parse SELECT statement
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

            // Parse SELECT fields
            sqlObj.setSelectColumns(parseSelectColumns(selectClause));

            // Parse FROM clause (including JOIN)
            parseFromClause(fromClause, sqlObj);

            // Parse WHERE conditions
            if (whereClause != null) {
                parseWhereConditions(whereClause, sqlObj);
            }

            // Parse GROUP BY
            if (groupByClause != null) {
                parseGroupBy(groupByClause, sqlObj);
            }

            // Parse HAVING
            if (havingClause != null) {
                sqlObj.setHavingCondition(havingClause.trim());
            }

            // Parse ORDER BY
            if (orderByClause != null) {
                parseOrderBy(orderByClause, sqlObj);
            }

            // Parse LIMIT
            if (limitClause != null) {
                parseLimit(limitClause, sqlObj);
            }
        }
    }

    /**
     * Parse SELECT fields
     */
    private List<SqlField> parseSelectColumns(String selectClause) {
        List<SqlField> columns = new ArrayList<>();
        String[] parts = selectClause.split(",");

        for (String part : parts) {
            part = part.trim();
            SqlField column = new SqlField();

            // Check if there's an alias
            if (part.toUpperCase().contains(" AS ")) {
                String[] aliasParts = part.split("\\s+(?i)AS\\s+");
                column.setFieldName(aliasParts[0].trim());
                column.setAlias(aliasParts[1].trim());
            } else {
                // Check simple alias (space separated)
                String[] spaceParts = part.split("\\s+");
                if (spaceParts.length == 2 && !spaceParts[1].toUpperCase().matches("FROM|WHERE|GROUP|ORDER|LIMIT")) {
                    column.setFieldName(spaceParts[0].trim());
                    column.setAlias(spaceParts[1].trim());
                } else {
                    column.setFieldName(part);
                }
            }

            // Parse table alias
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
     * Parse FROM clause (including JOIN)
     */
    private void parseFromClause(String fromClause, SqlObj sqlObj) {
        // First extract the main table
        String[] parts = fromClause.split("\\s+(?i)(?:INNER\\s+JOIN|LEFT\\s+(?:OUTER\\s+)?JOIN|RIGHT\\s+(?:OUTER\\s+)?JOIN|FULL\\s+(?:OUTER\\s+)?JOIN|CROSS\\s+JOIN|JOIN)");
        String mainTablePart = parts[0].trim();

        // Parse main table
        SqlTable mainTable = parseTableInfo(mainTablePart);
        sqlObj.setMainTable(mainTable);

        // Parse JOIN tables
        List<SqlJoin> joinTables = parseJoinTables(fromClause);
        sqlObj.setJoinTables(joinTables);
    }

    /**
     * Parse table information
     */
    private SqlTable parseTableInfo(String tablePart) {
        tablePart = tablePart.trim();
        String[] parts = tablePart.split("\\s+|(?i)AS");
        parts = Arrays.stream(parts).map(String::trim).filter(s -> !s.isEmpty()).toArray(String[]::new);

        SqlTable table = new SqlTable();
        table.setTableName(parts[0]);

        if (parts.length > 1) {
            table.setAlias(parts[1]);
        }

        return table;
    }

    /**
     * Parse JOIN tables
     */
    private List<SqlJoin> parseJoinTables(String fromClause) {
        Matcher matcher = JOIN_PATTERN.matcher(fromClause);
        List<SqlJoin> joinTables = new ArrayList<>();
        while (matcher.find()) {
            String joinTypeStr = matcher.group(1).toUpperCase().replaceAll("\\s+", "_");
            String tableName = matcher.group(2);
            String tableAlias = matcher.group(3);
            String joinConditionStr = matcher.group(4);

            // Parse JOIN type
            JoinType joinType = switch (joinTypeStr) {
                case "INNER_JOIN", "JOIN" -> JoinType.INNER_JOIN;
                case "LEFT_JOIN", "LEFT_OUTER_JOIN" -> JoinType.LEFT_JOIN;
                case "RIGHT_JOIN", "RIGHT_OUTER_JOIN" -> JoinType.RIGHT_JOIN;
                case "FULL_JOIN", "FULL_OUTER_JOIN" -> JoinType.FULL_JOIN;
                case "CROSS_JOIN" -> JoinType.CROSS_JOIN;
                default -> JoinType.INNER_JOIN;
            };

            // Create JOIN information
            SqlJoin sqlJoin = new SqlJoin();
            sqlJoin.setJoinType(joinType);
            // Remove backticks
            sqlJoin.setTableName(tableName != null ? tableName.replaceAll("`", "") : null);
            sqlJoin.setAlias(tableAlias != null ? tableAlias.replaceAll("`", "") : null);

            // Parse JOIN conditions
            if (joinConditionStr != null) {
                sqlJoin.setCondition(joinConditionStr.trim());

                // Parse detailed JOIN conditions
                List<SqlCondition> joinConditions = parseJoinConditions(joinConditionStr.trim());
                sqlJoin.setJoinConditions(joinConditions);
            }

            joinTables.add(sqlJoin);
        }

        return joinTables;
    }

    /**
     * Parse JOIN conditions
     */
    private List<SqlCondition> parseJoinConditions(String joinConditionStr) {
        List<SqlCondition> conditions = new ArrayList<>();

        // Smart split conditions to avoid breaking BETWEEN...AND structure
        List<String> andParts = smartSplitByAnd(joinConditionStr);

        for (String part : andParts) {
            part = part.trim();

            // Further split by OR
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
     * Parse WHERE conditions
     */
    private void parseWhereConditions(String whereClause, SqlObj sqlObj) {
        List<SqlCondition> conditions = new ArrayList<>();

        // Smart split conditions to avoid breaking BETWEEN...AND structure
        List<String> andParts = smartSplitByAnd(whereClause);

        for (String part : andParts) {
            part = part.trim();

            // Further split by OR
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
     * Smart split WHERE clause to avoid breaking BETWEEN...AND structure
     */
    private List<String> smartSplitByAnd(String whereClause) {
        List<String> parts = new ArrayList<>();
        StringBuilder currentPart = new StringBuilder();

        String upperClause = whereClause.toUpperCase();
        int i = 0;

        while (i < whereClause.length()) {
            // Check if we encounter AND
            if (i <= whereClause.length() - 4 && upperClause.substring(i, i + 4).equals(" AND")) {
                // Check if this AND is within BETWEEN...AND structure
                String beforeAnd = currentPart.toString().toUpperCase();

                // If current part contains BETWEEN or NOT BETWEEN without corresponding AND, then this AND belongs to BETWEEN
                boolean inBetween = false;
                if (beforeAnd.contains(" BETWEEN ") || beforeAnd.contains(" NOT BETWEEN ")) {
                    // Simple check: if there's already an AND, then current AND is logical connector
                    // If no AND, then current AND is part of BETWEEN
                    int betweenCount = countOccurrences(beforeAnd, " BETWEEN ");
                    int notBetweenCount = countOccurrences(beforeAnd, " NOT BETWEEN ");
                    int andCount = countOccurrences(beforeAnd, " AND ");

                    // If BETWEEN count is greater than AND count, current AND belongs to BETWEEN
                    inBetween = (betweenCount + notBetweenCount) > andCount;
                }

                if (inBetween) {
                    // This AND is part of BETWEEN, add to current part
                    currentPart.append(whereClause.substring(i, i + 4));
                    i += 4;
                } else {
                    // This AND is logical connector, split condition
                    parts.add(currentPart.toString().trim());
                    currentPart = new StringBuilder();
                    i += 4; // Skip " AND"
                    // Skip subsequent spaces
                    while (i < whereClause.length() && Character.isWhitespace(whereClause.charAt(i))) {
                        i++;
                    }
                }
            } else {
                currentPart.append(whereClause.charAt(i));
                i++;
            }
        }

        // Add last part
        if (currentPart.length() > 0) {
            parts.add(currentPart.toString().trim());
        }

        return parts;
    }

    /**
     * Count occurrences of substring in text
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
     * Parse single WHERE condition
     */
    private SqlCondition parseWhereCondition(String conditionStr) {
        SqlCondition condition = new SqlCondition();
        conditionStr = conditionStr.trim();

        // Remove parentheses
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
        // NOT BETWEEN (must check before BETWEEN)
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
        // NOT IN (must check before IN)
        else if (conditionStr.toUpperCase().contains(" NOT IN ")) {
            String[] parts = conditionStr.split("\\s+(?i)NOT\\s+IN\\s+");
            condition.setLeftOperand(parts[0].trim());
            condition.setOperator("NOT IN");
            condition.setRightOperand(parts[1].trim());
        }
        // NOT IN (must check before IN)
        else if (conditionStr.toUpperCase().contains(" NOT IN(")) {
            String[] parts = conditionStr.split("\\s+(?i)NOT\\s+IN");
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
        // IN
        else if (conditionStr.toUpperCase().contains(" IN(")) {
            String[] parts = conditionStr.split("\\s+(?i)IN");
            condition.setLeftOperand(parts[0].trim());
            condition.setOperator("IN");
            condition.setRightOperand(parts[1].trim());
        }
        // NOT LIKE (must check before LIKE)
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
        // Basic comparison operators
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
            // Unparseable condition
            return null;
        }

        return condition;
    }

    /**
     * Parse GROUP BY
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
     * Parse ORDER BY
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
                orderBy.setDirection(Direction.ASC); // Default ascending
            }

            orderByColumns.add(orderBy);
        }

        sqlObj.setOrderByColumns(orderByColumns);
    }

    /**
     * Parse LIMIT
     */
    private void parseLimit(String limitClause, SqlObj sqlObj) {
        SqlLimit sqlLimit = new SqlLimit();
        limitClause = limitClause.trim();

        if (limitClause.contains("OFFSET")) {
            // LIMIT n OFFSET m format
            String[] parts = limitClause.split("\\s+(?i)OFFSET\\s+");
            sqlLimit.setLimit(Integer.parseInt(parts[0].trim()));
            sqlLimit.setOffset(Integer.parseInt(parts[1].trim()));
        } else if (limitClause.contains(",")) {
            // LIMIT m, n format (MySQL specific)
            String[] parts = limitClause.split(",");
            sqlLimit.setOffset(Integer.parseInt(parts[0].trim()));
            sqlLimit.setLimit(Integer.parseInt(parts[1].trim()));
        } else {
            // LIMIT n format
            sqlLimit.setLimit(Integer.parseInt(limitClause));
            sqlLimit.setOffset(0);
        }

        sqlObj.setLimitInfo(sqlLimit);
    }

    /**
     * Parse INSERT statement
     */
    private void parseInsert(SqlObj sqlObj) {
        String sql = sqlObj.getOriginalSql();

        sqlObj.setSqlType(SqlType.INSERT);

        Matcher matcher = INSERT_PATTERN.matcher(sql);
        if (matcher.find()) {
            String tableName = matcher.group(1);
            String columnsStr = matcher.group(2);
            String valuesStr = matcher.group(3);

            // Set main table
            SqlTable mainTable = new SqlTable();
            mainTable.setTableName(tableName);
            sqlObj.setMainTable(mainTable);

            // Parse fields
            sqlObj.setInsertValues(new ArrayList<>());
            String[] columnArray = columnsStr.split(",");
            String[] valueArray = valuesStr.split(",");
            for (int i = 0; i < columnArray.length; i++) {
                sqlObj.getInsertValues()
                        .add(new SqlUpdateColumn(columnArray[i].trim(),
                                valueArray[i].trim()));
            }
        }
    }

    /**
     * Parse UPDATE statement
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

            // Parse table information
            SqlTable mainTable = parseTableInfo(tableInfo);
            sqlObj.setMainTable(mainTable);

            // Parse SET clause
            sqlObj.setUpdateValues(new ArrayList<>());
            String[] setParts = setClause.split(",");
            for (String part : setParts) {
                String[] keyValue = part.split("=");
                if (keyValue.length == 2) {
                    sqlObj.getUpdateValues()
                            .add(new SqlUpdateColumn(keyValue[0].trim(),
                                    keyValue[1].trim()));
                }
            }

            // Parse WHERE conditions
            if (whereClause != null) {
                parseWhereConditions(whereClause, sqlObj);
            }

            // Parse LIMIT
            if (limitClause != null) {
                parseLimit(limitClause, sqlObj);
            }
        }
    }

    /**
     * Parse DELETE statement
     */
    private void parseDelete(SqlObj sqlObj) {
        String sql = sqlObj.getOriginalSql();
        sqlObj.setSqlType(SqlType.DELETE);

        Matcher matcher = DELETE_PATTERN.matcher(sql);
        if (matcher.find()) {
            String tableInfo = matcher.group(1);
            String whereClause = matcher.group(2);
            String limitClause = matcher.group(3);

            // Parse table information
            SqlTable mainTable = parseTableInfo(tableInfo);
            sqlObj.setMainTable(mainTable);

            // Parse WHERE conditions
            if (whereClause != null) {
                parseWhereConditions(whereClause, sqlObj);
            }

            // Parse LIMIT
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
     * Build SELECT SQL
     */
    private void buildSelectSql(SqlObj sqlObj, StringBuilder sql) {
        sql.append("SELECT ");

        // SELECT fields
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

        // FROM clause
        sql.append(" FROM ").append(sqlObj.getMainTable().getTableName());
        if (sqlObj.getMainTable().getAlias() != null) {
            sql.append(" ").append(sqlObj.getMainTable().getAlias());
        }

        // JOIN clause
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

        // WHERE clause
        if (sqlObj.getWhereConditions() != null && !sqlObj.getWhereConditions().isEmpty()) {
            sql.append(" WHERE ");
            for (int i = 0; i < sqlObj.getWhereConditions().size(); i++) {
                if (i > 0) {
                    sql.append(" AND ");
                }
                sql.append(buildConditionString(sqlObj.getWhereConditions().get(i)));
            }
        }

        // GROUP BY clause
        if (sqlObj.getGroupByColumns() != null && !sqlObj.getGroupByColumns().isEmpty()) {
            sql.append(" GROUP BY ");
            for (int i = 0; i < sqlObj.getGroupByColumns().size(); i++) {
                if (i > 0) {
                    sql.append(", ");
                }
                sql.append(sqlObj.getGroupByColumns().get(i));
            }
        }

        // HAVING clause
        if (sqlObj.getHavingCondition() != null) {
            sql.append(" HAVING ").append(sqlObj.getHavingCondition());
        }

        // ORDER BY clause
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

        // LIMIT clause
        if (sqlObj.getLimitInfo() != null) {
            sql.append(" LIMIT ").append(sqlObj.getLimitInfo().getLimit());
            if (sqlObj.getLimitInfo().getOffset() > 0) {
                sql.append(" OFFSET ").append(sqlObj.getLimitInfo().getOffset());
            }
        }
    }

    /**
     * Build INSERT SQL
     */
    private void buildInsertSql(SqlObj sqlObj, StringBuilder sql) {
        sql.append("INSERT INTO ").append(sqlObj.getMainTable().getTableName());

        if (sqlObj.getInsertValues() != null && !sqlObj.getInsertValues().isEmpty()) {
            sql.append(" (");
            for (int i = 0; i < sqlObj.getInsertValues().size(); i++) {
                if (i > 0) {
                    sql.append(", ");
                }
                sql.append(sqlObj.getInsertValues().get(i).columnName());
            }
            sql.append(") VALUES (");

            for (int i = 0; i < sqlObj.getInsertValues().size(); i++) {
                if (i > 0) {
                    sql.append(", ");
                }
                Object value = sqlObj.getInsertValues().get(i).value();
                sql.append(value != null ? value.toString() : "NULL");
            }
            sql.append(")");
        }
    }

    /**
     * Build UPDATE SQL
     */
    private void buildUpdateSql(SqlObj sqlObj, StringBuilder sql) {
        sql.append("UPDATE ").append(sqlObj.getMainTable().getTableName());
        if (sqlObj.getMainTable().getAlias() != null) {
            sql.append(" ").append(sqlObj.getMainTable().getAlias());
        }

        if (sqlObj.getUpdateValues() != null && !sqlObj.getUpdateValues().isEmpty()) {
            sql.append(" SET ");
            int i = 0;
            for (SqlUpdateColumn entry : sqlObj.getUpdateValues()) {
                if (i > 0) {
                    sql.append(", ");
                }
                sql.append(entry.columnName()).append(" = ").append(entry.value());
                i++;
            }
        }

        // WHERE clause
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
     * Build DELETE SQL
     */
    private void buildDeleteSql(SqlObj sqlObj, StringBuilder sql) {
        sql.append("DELETE FROM ").append(sqlObj.getMainTable().getTableName());

        // WHERE clause
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
     * Build condition string
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