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

    public static List<SqlParam> getSpecParam(String sql) {
        sql = ParamExtractor.preProcessSql(sql);
        return new ArrayList<>(ParamFinder.find(sql));
    }

    public static List<SqlParam> getSpecParam(String sql, Pattern pattern) {
        sql = ParamExtractor.preProcessSql(sql, pattern);
        return new ArrayList<>(ParamFinder.find(sql));
    }

    public static String addJoin(String sql, String joinClause) {
        return addJoinAndWhere(sql, joinClause, null);
    }

    public static String addWhere(String sql, String whereClause) {
        return addJoinAndWhere(sql, null, whereClause);
    }

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