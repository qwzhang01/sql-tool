package io.github.qwzhang01.sql.tool.helper;

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

/**
 * Helper class for JSQLParser operations
 *
 * @author avinzhang
 */
public class ParserHelper {
    public static List<SqlTable> getTables(String sql) {
        return new ArrayList<>(TableFinder.findTablesOrOtherSources(sql));
    }

    public static List<SqlParam> getParam(String sql) {
        return new ArrayList<>(ParamFinder.find(sql));
    }

    public static String addJoin(String sql, String joinClause) {
        return addJoinAndWhere(sql, joinClause, null);
    }

    public static String addWhere(String sql, String whereClause) {
        return addJoinAndWhere(sql, null, whereClause);
    }

    public static String addJoinAndWhere(String sql, String joinClause, String whereClause) {
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
        mVisitor.setTables(getTables(sql));

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
}