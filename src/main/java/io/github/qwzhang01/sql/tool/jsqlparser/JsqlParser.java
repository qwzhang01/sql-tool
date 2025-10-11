package io.github.qwzhang01.sql.tool.jsqlparser;


import io.github.qwzhang01.sql.tool.exception.JsqlParserException;
import io.github.qwzhang01.sql.tool.jsqlparser.visitor.MergeStatementVisitor;
import io.github.qwzhang01.sql.tool.jsqlparser.visitor.ParamStatementVisitorAdaptor;
import io.github.qwzhang01.sql.tool.jsqlparser.visitor.SplitStatementVisitor;
import io.github.qwzhang01.sql.tool.model.SqlParam;
import io.github.qwzhang01.sql.tool.wrapper.SqlParser;
import net.sf.jsqlparser.expression.Expression;
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
     * @param sql         the original SQL statement to modify
     * @param joinClause  the JOIN clause to add (can be null or empty)
     * @param whereClause the WHERE clause to add (can be null or empty)
     * @return the modified SQL statement with merged JOIN and WHERE clauses
     * @throws JsqlParserException if SQL parsing fails
     */
    public String addJoinAndWhere(String sql, String joinClause, String whereClause) {
        // Clean and prepare SQL statements
        if (sql != null && !sql.isEmpty()) {
            sql = sql.trim();
        }
        if (joinClause != null && !joinClause.isEmpty()) {
            joinClause = joinClause.trim();
        }
        if (whereClause != null && !whereClause.isEmpty()) {
            whereClause = whereClause.trim();
        }

        MergeStatementVisitor mVisitor = new MergeStatementVisitor();

        // Process JOIN clause if provided
        if (joinClause != null && !joinClause.isEmpty()) {
            // Parse JOIN clause using a dummy SELECT statement
            Statement parse = SqlParser.getInstance().parse("select * from a a " + joinClause);
            SplitStatementVisitor visitor = new SplitStatementVisitor();
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
            Statement parse = SqlParser.getInstance().parse("select * from  a a " + whereClause);
            SplitStatementVisitor visitor = new SplitStatementVisitor();
            parse.accept(visitor);
            Expression where = visitor.getWhere();
            if (where != null) {
                mVisitor.setWhere(where);
            }
        }

        // Parse the original SQL and apply the merge visitor
        Statement parse = SqlParser.getInstance().parse(sql);
        parse.accept(mVisitor);
        return mVisitor.getSql();
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
        Statement parse = SqlParser.getInstance().parse(sql);
        ParamStatementVisitorAdaptor visitor = new ParamStatementVisitorAdaptor();
        parse.accept(visitor);
        return visitor.getParams();
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
