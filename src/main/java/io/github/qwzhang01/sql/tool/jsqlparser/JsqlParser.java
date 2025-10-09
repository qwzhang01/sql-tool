package io.github.qwzhang01.sql.tool.jsqlparser;


import io.github.qwzhang01.sql.tool.exception.JsqlParserException;
import io.github.qwzhang01.sql.tool.model.SqlParam;
import io.github.qwzhang01.sql.tool.model.SqlTable;
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
    private JsqlParser() {
    }

    public static JsqlParser getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public String addJoinAndWhere(String sql, String joinClause, String whereClause) {
        try {
            MergeStatementVisitor mVisitor = new MergeStatementVisitor();

            if (joinClause != null && !joinClause.isEmpty()) {
                Statement parse = CCJSqlParserUtil.parse("select * from a a " + joinClause);
                GetStatementVisitor visitor = new GetStatementVisitor();
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
                GetStatementVisitor visitor = new GetStatementVisitor();
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
        try {
            Statement parse = CCJSqlParserUtil.parse(sql);
            ParamStatementVisitor visitor = new ParamStatementVisitor();
            parse.accept(visitor);
            return visitor.getParams();
        } catch (JSQLParserException e) {
            throw new JsqlParserException(e);
        }
    }

    public List<SqlTable> getTables(String sql) {
        try {
            Statement parse = CCJSqlParserUtil.parse(sql);
            TablesStatementVisitor visitor = new TablesStatementVisitor(false);
            parse.accept(visitor);
            return visitor.getTables();
        } catch (JSQLParserException e) {
            throw new JsqlParserException(e);
        }
    }

    public List<SqlTable> getTableDeep(String sql) {
        try {
            Statement parse = CCJSqlParserUtil.parse(sql);
            TablesStatementVisitor visitor = new TablesStatementVisitor(true);
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
