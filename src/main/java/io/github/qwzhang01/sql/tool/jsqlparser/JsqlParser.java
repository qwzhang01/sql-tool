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
 * jsql parser
 *
 * @author avinzhang
 */
public class JsqlParser {
    private final SqlCleaner sqlCleaner;

    private JsqlParser() {
        sqlCleaner = new MySqlSqlCleaner();
    }

    public static JsqlParser getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public String addJoinAndWhere(String sql, String joinClause, String whereClause) {
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

            if (joinClause != null && !joinClause.isEmpty()) {
                Statement parse = CCJSqlParserUtil.parse("select * from a a " + joinClause);
                GetStatementVisitorAdaptor visitor = new GetStatementVisitorAdaptor();
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
                Statement parse = CCJSqlParserUtil.parse("select * from  a a " + whereClause);
                GetStatementVisitorAdaptor visitor = new GetStatementVisitorAdaptor();
                parse.accept(visitor);
                Expression where = visitor.getWhere();
                if (where != null) {
                    mVisitor.setWhere(where);
                }
            }

            Statement parse = CCJSqlParserUtil.parse(sql);
            parse.accept(mVisitor);
            return mVisitor.getSql();
        } catch (JSQLParserException e) {
            throw new JsqlParserException(e);
        }
    }

    public List<SqlParam> getParam(String sql) {
        if (sql != null && !sql.isEmpty()) {
            sql = sql.trim();
        }
        try {
            Statement parse = CCJSqlParserUtil.parse(sql);
            ParamStatementVisitorAdaptor visitor = new ParamStatementVisitorAdaptor();
            parse.accept(visitor);
            return visitor.getParams();
        } catch (JSQLParserException e) {
            throw new JsqlParserException(e);
        }
    }

    public List<SqlTable> getTables(String sql) {
        if (sql != null && !sql.isEmpty()) {
            sql = sql.trim();
        }
        try {
            Statement parse = CCJSqlParserUtil.parse(sql);
            TablesStatementVisitorAdaptor visitor = new TablesStatementVisitorAdaptor(false);
            parse.accept(visitor);
            return visitor.getTables();
        } catch (JSQLParserException e) {
            throw new JsqlParserException(e);
        }
    }

    public List<SqlTable> getTableDeep(String sql) {
        if (sql != null && !sql.isEmpty()) {
            sql = sql.trim();
        }
        try {
            Statement parse = CCJSqlParserUtil.parse(sql);
            TablesStatementVisitorAdaptor visitor = new TablesStatementVisitorAdaptor(true);
            parse.accept(visitor);
            return visitor.getTables();
        } catch (JSQLParserException e) {
            throw new JsqlParserException(e);
        }
    }

    private static class SingletonHolder {
        private static final JsqlParser INSTANCE = new JsqlParser();
    }
}
