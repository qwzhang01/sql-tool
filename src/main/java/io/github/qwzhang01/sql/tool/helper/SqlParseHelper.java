package io.github.qwzhang01.sql.tool.helper;

import io.github.qwzhang01.sql.tool.enums.SqlType;
import io.github.qwzhang01.sql.tool.exception.ParseException;
import io.github.qwzhang01.sql.tool.model.*;
import io.github.qwzhang01.sql.tool.parser.MySqlPureSqlParser;
import io.github.qwzhang01.sql.tool.parser.SqlParser;

import java.util.List;

/**
 * Utility class providing convenient static methods for SQL parsing operations.
 * This class serves as a facade for the underlying SQL parser implementation,
 * offering simplified access to common SQL parsing, cleaning, and analysis tasks.
 *
 * <p>Key features:</p>
 * <ul>
 *   <li>Parse SQL statements into structured objects</li>
 *   <li>Clean and format SQL with comment removal</li>
 *   <li>Extract specific SQL components (WHERE, JOIN, etc.)</li>
 *   <li>Analyze SQL complexity and characteristics</li>
 *   <li>Convert between SQL strings and structured objects</li>
 * </ul>
 *
 * <p>Usage example:</p>
 * <pre>{@code
 * // Parse a SQL statement
 * SqlInfo sqlInfo = SqlParserUtils.parseSQL("SELECT * FROM users WHERE id = 1");
 *
 * // Clean SQL comments
 * String cleanSql = SqlParserUtils.cleanSQL("SELECT * FROM users -- comment");
 *
 * // Check SQL characteristics
 * boolean hasJoins = SqlParserUtils.hasJoin(sqlInfo);
 * int complexity = SqlParserUtils.getComplexityScore(sqlInfo);
 * }</pre>
 *
 * @author Avin Zhang
 * @since 1.0.0
 */
public class SqlParseHelper {

    private static final SqlParser DEFAULT_PARSER = new MySqlPureSqlParser();

    /**
     * Parses a SQL statement into a structured SqlInfo object with automatic comment cleaning.
     * This method handles all types of SQL statements (SELECT, INSERT, UPDATE, DELETE).
     *
     * @param sql the SQL statement to parse
     * @return SqlInfo object containing parsed SQL components, or null if SQL is null/empty
     * @throws IllegalArgumentException if the SQL syntax is invalid
     * @throws ParseException           if parsing fails
     */
    public static SqlObj parseSQL(String sql) {
        return DEFAULT_PARSER.parse(sql);
    }

    /**
     * Parses WHERE conditions from a SQL statement or WHERE clause.
     *
     * @param sql the SQL statement or WHERE clause to parse
     * @return list of parsed WHERE conditions with detailed field information
     * @throws IllegalArgumentException if the input is not a valid WHERE clause
     */
    public static List<SqlCondition> parseWhere(String sql) {
        return DEFAULT_PARSER.parseWhere(sql);
    }

    /**
     * Parses JOIN clauses from a SQL statement.
     * Supports all JOIN types: INNER, LEFT, RIGHT, FULL, CROSS.
     *
     * @param sql the SQL statement containing JOIN clauses
     * @return list of parsed JOIN information with detailed conditions
     */
    public static List<SqlJoin> parseJoin(String sql) {
        return DEFAULT_PARSER.parseJoin(sql);
    }

    /**
     * Cleans SQL by removing comments and extra whitespace characters.
     * Supports both single-line (--) and multi-line (/* *\/) comments.
     *
     * @param sql the SQL statement to clean
     * @return cleaned SQL with comments and extra whitespace removed
     */
    public static String cleanSQL(String sql) {
        return DEFAULT_PARSER.getCleaner().cleanSql(sql);
    }

    /**
     * Cleans and formats SQL for better readability.
     * Removes comments, normalizes whitespace, and applies basic formatting.
     *
     * @param sql the SQL statement to clean and format
     * @return formatted SQL string
     */
    public static String cleanAndFormatSQL(String sql) {
        return DEFAULT_PARSER.getCleaner().cleanAndFormatSql(sql);
    }

    /**
     * Checks whether the SQL statement contains any comments.
     * Detects both single-line (--) and multi-line ("/* *\/") comment styles.
     *
     * @param sql the SQL statement to check
     * @return true if the SQL contains comments, false otherwise
     */
    public static boolean containsComments(String sql) {
        return DEFAULT_PARSER.getCleaner().containsComments(sql);
    }

    /**
     * Removes only comments from SQL while preserving the original formatting.
     * Unlike cleanSQL(), this method maintains original whitespace and line breaks.
     *
     * @param sql the SQL statement to process
     * @return SQL with comments removed but original formatting preserved
     */
    public static String removeCommentsOnly(String sql) {
        return DEFAULT_PARSER.getCleaner().removeCommentsOnly(sql);
    }

    /**
     * Converts a SqlInfo object back to a SQL statement string.
     * This is useful for reconstructing SQL after modifications to the SqlInfo object.
     *
     * @param sqlObj the SqlInfo object to convert
     * @return SQL statement string representation
     * @throws IllegalArgumentException if sqlInfo is null or invalid
     */
    public static String toSQL(SqlObj sqlObj) {
        return DEFAULT_PARSER.toSql(sqlObj);
    }

    public static String toSQL(List<SqlJoin> joins) {
        return DEFAULT_PARSER.toSql(joins);
    }

    /**
     * Checks if the SQL statement is a SELECT query.
     *
     * @param sqlObj the parsed SQL information
     * @return true if the SQL is a SELECT statement, false otherwise
     */
    public static boolean isSelectSQL(SqlObj sqlObj) {
        return sqlObj.getSqlType() == SqlType.SELECT;
    }

    /**
     * Checks if the SQL statement is a data modification statement (INSERT/UPDATE/DELETE).
     *
     * @param sqlObj the parsed SQL information
     * @return true if the SQL is an INSERT, UPDATE, or DELETE statement, false otherwise
     */
    public static boolean isUpdateSQL(SqlObj sqlObj) {
        SqlType type = sqlObj.getSqlType();
        return type == SqlType.INSERT ||
                type == SqlType.UPDATE ||
                type == SqlType.DELETE;
    }

    /**
     * Extracts all table names referenced in the SQL statement.
     * This includes main tables, JOIN tables, and tables in subqueries.
     *
     * @param sqlObj the parsed SQL information
     * @return set of unique table names found in the SQL
     */
    public static java.util.Set<String> getAllTableNames(SqlObj sqlObj) {
        java.util.Set<String> tableNames = new java.util.HashSet<>();

        // Main table
        if (sqlObj.getMainTable() != null) {
            tableNames.add(sqlObj.getMainTable().getName());
        }

        // JOIN tables
        if (sqlObj.getJoinTables() != null) {
            for (SqlJoin sqlJoin : sqlObj.getJoinTables()) {
                if (sqlJoin.getTableName() != null) {
                    tableNames.add(sqlJoin.getTableName());
                }
            }
        }

        // Tables in subqueries
        if (sqlObj.getSubQueries() != null) {
            for (SqlObj subQuery : sqlObj.getSubQueries()) {
                tableNames.addAll(getAllTableNames(subQuery));
            }
        }

        return tableNames;
    }

    /**
     * Extracts all column names referenced in the SQL statement.
     * This includes SELECT columns, WHERE condition columns, GROUP BY columns,
     * ORDER BY columns, and INSERT/UPDATE columns.
     *
     * @param sqlObj the parsed SQL information
     * @return set of unique column names found in the SQL (excludes wildcard "*")
     */
    public static java.util.Set<String> getAllColumnNames(SqlObj sqlObj) {
        java.util.Set<String> columnNames = new java.util.HashSet<>();

        // SELECT columns
        if (sqlObj.getSelectColumns() != null) {
            for (SqlField columnInfo : sqlObj.getSelectColumns()) {
                if (!"*".equals(columnInfo.getFieldName())) {
                    columnNames.add(columnInfo.getFieldName());
                }
            }
        }

        // Columns in WHERE conditions
        if (sqlObj.getWhereConditions() != null) {
            for (SqlCondition condition : sqlObj.getWhereConditions()) {
                collectColumnsFromCondition(condition, columnNames);
            }
        }

        // Columns in HAVING conditions - HAVING is a string, skip parsing for now
        // collectColumnsFromCondition(sqlInfo.getHavingCondition(), columnNames);

        // GROUP BY columns
        if (sqlObj.getGroupByColumns() != null) {
            columnNames.addAll(sqlObj.getGroupByColumns());
        }

        // ORDER BY columns
        if (sqlObj.getOrderByColumns() != null) {
            for (SqlOrderBy sqlOrderBy : sqlObj.getOrderByColumns()) {
                columnNames.add(sqlOrderBy.getColumnName());
            }
        }

        // INSERT/UPDATE columns
        if (sqlObj.getUpdateValues() != null) {
            columnNames.addAll(sqlObj.getUpdateValues().stream().map(SqlUpdateColumn::columnName).toList());
        }

        return columnNames;
    }

    /**
     * Recursively collects column names from WHERE conditions.
     * This helper method extracts column names from both simple and nested conditions.
     *
     * @param condition   the WHERE condition to analyze
     * @param columnNames the set to collect column names into
     */
    private static void collectColumnsFromCondition(SqlCondition condition, java.util.Set<String> columnNames) {
        if (condition == null) {
            return;
        }

        if (condition.getLeftOperand() != null) {
            columnNames.add(condition.getLeftOperand());
        }

        if (condition.getSubConditions() != null) {
            for (SqlCondition subCondition : condition.getSubConditions()) {
                collectColumnsFromCondition(subCondition, columnNames);
            }
        }
    }

    /**
     * Checks if the SQL statement contains any JOIN clauses.
     *
     * @param sqlObj the parsed SQL information
     * @return true if the SQL contains JOIN clauses, false otherwise
     */
    public static boolean hasJoin(SqlObj sqlObj) {
        return sqlObj.getJoinTables() != null && !sqlObj.getJoinTables().isEmpty();
    }

    /**
     * Checks if the SQL statement contains any subqueries.
     *
     * @param sqlObj the parsed SQL information
     * @return true if the SQL contains subqueries, false otherwise
     */
    public static boolean hasSubQuery(SqlObj sqlObj) {
        return sqlObj.getSubQueries() != null && !sqlObj.getSubQueries().isEmpty();
    }

    /**
     * Checks if the SQL statement contains aggregate functions.
     * Detects common aggregate functions: COUNT, SUM, AVG, MAX, MIN.
     *
     * @param sqlObj the parsed SQL information
     * @return true if the SQL contains aggregate functions, false otherwise
     */
    public static boolean hasAggregateFunction(SqlObj sqlObj) {
        if (sqlObj.getSelectColumns() != null) {
            for (SqlField columnInfo : sqlObj.getSelectColumns()) {
                // Simple check if column name contains aggregate function keywords
                String columnName = columnInfo.getFieldName();
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
     * Calculates a complexity score for the SQL statement based on various factors.
     * The score increases with JOINs, subqueries, WHERE conditions, and other SQL features.
     *
     * <p>Scoring factors:</p>
     * <ul>
     *   <li>Base score: 1</li>
     *   <li>Each JOIN: +2 points</li>
     *   <li>Each subquery: +3 points (plus recursive scoring)</li>
     *   <li>Each WHERE condition: +1 point (recursive for nested conditions)</li>
     *   <li>HAVING clause: +1 point</li>
     *   <li>GROUP BY clause: +1 point</li>
     *   <li>ORDER BY clause: +1 point</li>
     *   <li>Aggregate functions: +1 point</li>
     * </ul>
     *
     * @param sqlObj the parsed SQL information
     * @return complexity score (higher values indicate more complex SQL)
     */
    public static int getComplexityScore(SqlObj sqlObj) {
        int score = 0;

        // Base score
        score += 1;

        // JOINs increase complexity
        if (sqlObj.getJoinTables() != null) {
            score += sqlObj.getJoinTables().size() * 2;
        }

        // Subqueries increase complexity
        if (sqlObj.getSubQueries() != null) {
            score += sqlObj.getSubQueries().size() * 3;
            for (SqlObj subQuery : sqlObj.getSubQueries()) {
                score += getComplexityScore(subQuery);
            }
        }

        // WHERE conditions increase complexity
        if (sqlObj.getWhereConditions() != null) {
            for (SqlCondition condition : sqlObj.getWhereConditions()) {
                score += getConditionComplexity(condition);
            }
        }

        // HAVING conditions increase complexity - HAVING is a string, simple calculation
        if (sqlObj.getHavingCondition() != null && !sqlObj.getHavingCondition().trim().isEmpty()) {
            score += 1;
        }

        // GROUP BY increases complexity
        if (sqlObj.getGroupByColumns() != null && !sqlObj.getGroupByColumns().isEmpty()) {
            score += 1;
        }

        // ORDER BY increases complexity
        if (sqlObj.getOrderByColumns() != null && !sqlObj.getOrderByColumns().isEmpty()) {
            score += 1;
        }

        // Aggregate functions increase complexity
        if (hasAggregateFunction(sqlObj)) {
            score += 1;
        }

        return score;
    }

    /**
     * Calculates the complexity score for a WHERE condition.
     * Recursively processes nested conditions to provide accurate complexity measurement.
     *
     * @param condition the WHERE condition to analyze
     * @return complexity score for the condition
     */
    private static int getConditionComplexity(SqlCondition condition) {
        if (condition == null) {
            return 0;
        }

        int complexity = 1;

        if (condition.getSubConditions() != null) {
            for (SqlCondition subCondition : condition.getSubConditions()) {
                complexity += getConditionComplexity(subCondition);
            }
        }

        return complexity;
    }

    /**
     * Formats SQL information into a human-readable string representation.
     * This method provides a comprehensive overview of the parsed SQL structure
     * including tables, columns, joins, and complexity metrics.
     *
     * @param sqlObj the parsed SQL information to format
     * @return formatted string representation of the SQL information
     */
    public static String formatSqlInfo(SqlObj sqlObj) {
        StringBuilder sb = new StringBuilder();

        sb.append("SQL Information:\n");
        sb.append("  Type: ").append(sqlObj.getSqlType()).append("\n");
        sb.append("  Original SQL: ").append(sqlObj.getOriginalSql()).append("\n");

        if (sqlObj.getMainTable() != null) {
            sb.append("  Main Table: ").append(sqlObj.getMainTable().getName());
            if (sqlObj.getMainTable().getAlias() != null) {
                sb.append(" AS ").append(sqlObj.getMainTable().getAlias());
            }
            sb.append("\n");
        }

        if (sqlObj.getJoinTables() != null && !sqlObj.getJoinTables().isEmpty()) {
            sb.append("  JOIN Tables:\n");
            for (SqlJoin sqlJoin : sqlObj.getJoinTables()) {
                sb.append("    ").append(sqlJoin.getJoinType())
                        .append(" ").append(sqlJoin.getTableName());
                if (sqlJoin.getAlias() != null) {
                    sb.append(" AS ").append(sqlJoin.getAlias());
                }
                sb.append("\n");
            }
        }

        if (sqlObj.getSelectColumns() != null && !sqlObj.getSelectColumns().isEmpty()) {
            sb.append("  Select Columns: ");
            for (int i = 0; i < sqlObj.getSelectColumns().size(); i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                SqlField column = sqlObj.getSelectColumns().get(i);
                sb.append(column.getFieldName());
                if (column.getAlias() != null) {
                    sb.append(" AS ").append(column.getAlias());
                }
            }
            sb.append("\n");
        }

        sb.append("  Complexity Score: ").append(getComplexityScore(sqlObj));

        return sb.toString();
    }

    /**
     * Compares two SQL statements for equality using the underlying SQL comparison engine.
     * This method performs semantic comparison rather than simple string comparison.
     *
     * @param sql1 the first SQL statement to compare
     * @param sql2 the second SQL statement to compare
     * @return true if the SQL statements are semantically equivalent, false otherwise
     */
    public static boolean equal(String sql1, String sql2) {
        return DEFAULT_PARSER.getCompare().equal(sql1, sql2);
    }
}