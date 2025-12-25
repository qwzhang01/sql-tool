package io.github.qwzhang01.sql.tool.helper;

import io.github.qwzhang01.sql.tool.jsqlparser.param.ParamExtractor;
import io.github.qwzhang01.sql.tool.jsqlparser.visitor.MergeStatementVisitor;
import io.github.qwzhang01.sql.tool.jsqlparser.visitor.ParamFinder;
import io.github.qwzhang01.sql.tool.jsqlparser.visitor.SplitStatementVisitor;
import io.github.qwzhang01.sql.tool.jsqlparser.visitor.TableFinder;
import io.github.qwzhang01.sql.tool.model.SqlParam;
import io.github.qwzhang01.sql.tool.model.SqlTable;
import io.github.qwzhang01.sql.tool.wrapper.SqlParser;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Join;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Helper class providing utility methods for JSQLParser operations.
 * This class offers convenient methods for extracting tables, parameters,
 * and dynamically modifying SQL statements by adding JOIN and WHERE clauses.
 *
 * @author Avin Zhang
 * @since 1.0.0
 */
public class ParserHelper {
    
    /**
     * Extracts all table names from the given SQL statement
     *
     * @param sql the SQL statement to parse
     * @return list of SqlTable objects representing all tables found in the SQL
     */
    public static List<SqlTable> getTables(String sql) {
        return new ArrayList<>(TableFinder.findTablesOrOtherSources(sql));
    }

    /**
     * Extracts all parameters (placeholders) from the given SQL statement
     *
     * @param sql the SQL statement to parse
     * @return list of SqlParam objects representing all parameters found
     */
    public static List<SqlParam> getParam(String sql) {
        return new ArrayList<>(ParamFinder.find(sql));
    }

    /**
     * Extracts parameters after pre-processing the SQL to convert special placeholders.
     * This method converts placeholders like #{param} to standard JDBC ? placeholders.
     *
     * @param sql the SQL statement to parse
     * @return list of SqlParam objects representing all parameters found after preprocessing
     */
    public static List<SqlParam> getSpecParam(String sql) {
        sql = ParamExtractor.preProcessSql(sql);
        return new ArrayList<>(ParamFinder.find(sql));
    }

    /**
     * Extracts parameters after pre-processing with a custom pattern
     *
     * @param sql     the SQL statement to parse
     * @param pattern the regex pattern to match custom parameter placeholders
     * @return list of SqlParam objects representing all parameters found after preprocessing
     */
    public static List<SqlParam> getSpecParam(String sql, Pattern pattern) {
        sql = ParamExtractor.preProcessSql(sql, pattern);
        return new ArrayList<>(ParamFinder.find(sql));
    }

    /**
     * Adds a JOIN clause to the given SQL statement
     *
     * @param sql        the original SQL statement
     * @param joinClause the JOIN clause to add (e.g., "INNER JOIN table2 t2 ON t1.id = t2.id")
     * @return the modified SQL with the JOIN clause added
     */
    public static String addJoin(String sql, String joinClause) {
        return addJoinAndWhere(sql, joinClause, null);
    }

    /**
     * Adds a WHERE clause to the given SQL statement
     *
     * @param sql         the original SQL statement
     * @param whereClause the WHERE condition to add (can omit "WHERE" keyword)
     * @return the modified SQL with the WHERE clause added
     */
    public static String addWhere(String sql, String whereClause) {
        return addJoinAndWhere(sql, null, whereClause);
    }

    /**
     * Adds both JOIN and WHERE clauses to the given SQL statement.
     * This method intelligently merges the new clauses with any existing ones.
     *
     * @param sql         the original SQL statement
     * @param joinClause  the JOIN clause to add (can be null)
     * @param whereClause the WHERE condition to add (can be null)
     * @return the modified SQL with both clauses added
     */
    public static String addJoinAndWhere(String sql, String joinClause, String whereClause) {

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
        mVisitor.setTables(getTables(sql));


        if (joinClause != null && !joinClause.isEmpty()) {

            Statement parse = SqlParser.getInstance().parse("select * from dump_table d " + joinClause);
            SplitStatementVisitor visitor = new SplitStatementVisitor();
            parse.accept(visitor);
            List<Join> joins = visitor.getJoins();
            if (joins != null && !joins.isEmpty()) {
                mVisitor.setJoins(joins);
            }
        }

        if (whereClause != null && !whereClause.isEmpty()) {

            if (!whereClause.toUpperCase().startsWith("WHERE")) {
                whereClause = "WHERE " + whereClause;
            }

            Statement parse = SqlParser.getInstance().parse("select * from dump_table d " + whereClause);
            SplitStatementVisitor visitor = new SplitStatementVisitor();
            parse.accept(visitor);
            Expression where = visitor.getWhere();
            if (where != null) {
                mVisitor.setWhere(where);
            }
        }


        Statement parse = SqlParser.getInstance().parse(sql);
        parse.accept(mVisitor);
        return mVisitor.getSql();
    }
}