package io.github.qwzhang01.sql.tool.jsqlparser;


import io.github.qwzhang01.sql.tool.exception.JsqlParserException;
import io.github.qwzhang01.sql.tool.model.SqlParam;
import io.github.qwzhang01.sql.tool.model.SqlTable;
import io.github.qwzhang01.sql.tool.parser.MySqlSqlCleaner;
import io.github.qwzhang01.sql.tool.parser.SqlCleaner;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Join;

import java.util.List;

/**
 * JSqlParser utility class for parsing and manipulating SQL statements.
 * This class provides methods to extract table information, parameters, and merge SQL clauses.
 * 
 * <p>Features include:</p>
 * <ul>
 *   <li>Adding JOIN and WHERE clauses to existing SQL statements</li>
 *   <li>Extracting SQL parameters (placeholders) from statements</li>
 *   <li>Extracting table information from SQL statements</li>
 *   <li>Support for deep parsing of nested queries</li>
 * </ul>
 *
 * @author avinzhang
 */
public class JsqlParser {
    /**
     * SQL cleaner instance for preprocessing SQL statements
     */
    private final SqlCleaner sqlCleaner;

    /**
     * Private constructor for singleton pattern.
     * Initializes the SQL cleaner with MySQL-specific implementation.
     */
    private JsqlParser() {
        sqlCleaner = new MySqlSqlCleaner();
    }

    /**
     * Gets the singleton instance of JsqlParser.
     *
     * @return the singleton JsqlParser instance
     */
    public static JsqlParser getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * Adds JOIN and WHERE clauses to an existing SQL statement.
     * This method merges additional JOIN and WHERE conditions with the original SQL.
     *
     * @param sql the original SQL statement to modify
     * @param joinClause the JOIN clause to add (can be null or empty)
     * @param whereClause the WHERE clause to add (can be null or empty)
     * @return the modified SQL statement with merged JOIN and WHERE clauses
     * @throws JsqlParserException if SQL parsing fails
     */
    public String addJoinAndWhere(String sql, String joinClause, String whereClause) {
        // Clean and prepare SQL statements
        if (sql != null && !sql.isEmpty()) {
            sql = sql.trim();
            sql = sqlCleaner.cleanSql(sql);
        }
        if (joinClause != null && !joinClause.isEmpty()) {
            joinClause = joinClause.trim();
            joinClause = sqlCleaner.cleanSql(joinClause);
        }
        if (whereClause != null && !whereClause.isEmpty()) {
            whereClause = whereClause.trim();
            whereClause = sqlCleaner.cleanSql(whereClause);
        }
        
        try {
            MergeStatementVisitorAdaptor mVisitor = new MergeStatementVisitorAdaptor();

            // Process JOIN clause if provided
            if (joinClause != null && !joinClause.isEmpty()) {
                // Parse JOIN clause using a dummy SELECT statement
                Statement parse = CCJSqlParserUtil.parse("select * from a a " + joinClause);
                GetStatementVisitorAdaptor visitor = new GetStatementVisitorAdaptor();
                parse.accept(visitor);
                List<Join> joins = visitor.getJoins();
                if (joins != null && !joins.isEmpty()) {
                    mVisitor.setJoins(joins);
                }
            }

            // Process WHERE clause if provided
            if (whereClause != null && !whereClause.isEmpty()) {
                // Ensure WHERE keyword is present
                if (!whereClause.toUpperCase().startsWith("WHERE")) {
                    whereClause = "WHERE " + whereClause;
                }
                // Parse WHERE clause using a dummy SELECT statement
                Statement parse = CCJSqlParserUtil.parse("select * from  a a " + whereClause);
                GetStatementVisitorAdaptor visitor = new GetStatementVisitorAdaptor();
                parse.accept(visitor);
                Expression where = visitor.getWhere();
                if (where != null) {
                    mVisitor.setWhere(where);
                }
            }

            // Parse the original SQL and apply the merge visitor
            Statement parse = CCJSqlParserUtil.parse(sql);
            parse.accept(mVisitor);
            return mVisitor.getSql();
        } catch (JSQLParserException e) {
            throw new JsqlParserException(e);
        }
    }

    /**
     * Extracts all SQL parameters (placeholders) from the given SQL statement.
     * This method identifies JDBC parameters (?) and their associated column information.
     *
     * @param sql the SQL statement to analyze
     * @return a list of SqlParam objects containing parameter information
     * @throws JsqlParserException if SQL parsing fails
     */
    public List<SqlParam> getParam(String sql) {
        // Clean input SQL
        if (sql != null && !sql.isEmpty()) {
            sql = sql.trim();
        }
        try {
            // Parse SQL and extract parameters using visitor pattern
            Statement parse = CCJSqlParserUtil.parse(sql);
            ParamStatementVisitorAdaptor visitor = new ParamStatementVisitorAdaptor();
            parse.accept(visitor);
            return visitor.getParams();
        } catch (JSQLParserException e) {
            throw new JsqlParserException(e);
        }
    }

    /**
     * Extracts all table information from the given SQL statement.
     * This method performs shallow parsing, extracting only direct table references.
     *
     * @param sql the SQL statement to analyze
     * @return a list of SqlTable objects containing table names and aliases
     * @throws JsqlParserException if SQL parsing fails
     */
    public List<SqlTable> getTables(String sql) {
        // Clean input SQL
        if (sql != null && !sql.isEmpty()) {
            sql = sql.trim();
        }
        try {
            // Parse SQL and extract tables using visitor pattern (shallow parsing)
            Statement parse = CCJSqlParserUtil.parse(sql);
            TablesStatementVisitorAdaptor visitor = new TablesStatementVisitorAdaptor(false);
            parse.accept(visitor);
            return visitor.getTables();
        } catch (JSQLParserException e) {
            throw new JsqlParserException(e);
        }
    }

    /**
     * Extracts all table information from the given SQL statement with deep parsing.
     * This method performs deep parsing, including tables from subqueries and nested statements.
     *
     * @param sql the SQL statement to analyze
     * @return a list of SqlTable objects containing all table names and aliases (including from subqueries)
     * @throws JsqlParserException if SQL parsing fails
     */
    public List<SqlTable> getTableDeep(String sql) {
        // Clean input SQL
        if (sql != null && !sql.isEmpty()) {
            sql = sql.trim();
        }
        try {
            // Parse SQL and extract tables using visitor pattern (deep parsing)
            Statement parse = CCJSqlParserUtil.parse(sql);
            TablesStatementVisitorAdaptor visitor = new TablesStatementVisitorAdaptor(true);
            parse.accept(visitor);
            return visitor.getTables();
        } catch (JSQLParserException e) {
            throw new JsqlParserException(e);
        }
    }

    /**
     * Singleton holder class for lazy initialization.
     * This ensures thread-safe singleton creation without synchronization overhead.
     */
    private static class SingletonHolder {
        /**
         * The singleton instance of JsqlParser
         */
        private static final JsqlParser INSTANCE = new JsqlParser();
    }
}
