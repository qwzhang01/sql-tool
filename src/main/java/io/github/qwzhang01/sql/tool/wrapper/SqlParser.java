package io.github.qwzhang01.sql.tool.wrapper;

import io.github.qwzhang01.sql.tool.exception.SqlIllegalException;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;

/**
 * SQL parser wrapper with exception handling
 *
 * @author avinzhang
 */
public class SqlParser {

    private SqlParser() {
    }

    public static SqlParser getInstance() {
        return SqlParserHolder.INSTANCE;
    }

    public Statement parse(String sql) {
        try {
            return CCJSqlParserUtil.parse(sql);
        } catch (JSQLParserException e) {
            throw new SqlIllegalException("Invalid SQL, cannot parse", e, sql);
        }
    }

    public Expression parseExpression(String expression) {
        try {
            return CCJSqlParserUtil.parseExpression(expression);
        } catch (JSQLParserException e) {
            throw new SqlIllegalException("Invalid SQL, cannot parse", e, expression);
        }
    }


    private static class SqlParserHolder {
        private static final SqlParser INSTANCE = new SqlParser();
    }
}
