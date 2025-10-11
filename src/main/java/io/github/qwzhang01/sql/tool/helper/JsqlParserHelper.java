package io.github.qwzhang01.sql.tool.helper;

import io.github.qwzhang01.sql.tool.jsqlparser.JsqlParser;
import io.github.qwzhang01.sql.tool.jsqlparser.visitor.TableFinder;
import io.github.qwzhang01.sql.tool.model.SqlParam;
import io.github.qwzhang01.sql.tool.model.SqlTable;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for JSQLParser operations
 *
 * @author avinzhang
 */
public class JsqlParserHelper {

    public static String addJoin(String sql, String joinClause) {
        return JsqlParser.getInstance().addJoinAndWhere(sql, joinClause, null);
    }

    public static String addWhere(String sql, String whereClause) {
        return JsqlParser.getInstance().addJoinAndWhere(sql, null, whereClause);
    }

    public static String addJoinAndWhere(String sql, String joinClause, String whereClause) {
        return JsqlParser.getInstance().addJoinAndWhere(sql, joinClause, whereClause);
    }

    public static List<SqlParam> getParam(String sql) {
        return JsqlParser.getInstance().getParam(sql);
    }

    public static List<SqlTable> getTables(String sql) {
        return new ArrayList<>(TableFinder.findTablesOrOtherSources(sql));
    }
}