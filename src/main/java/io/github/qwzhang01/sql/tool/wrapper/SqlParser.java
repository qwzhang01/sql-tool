package io.github.qwzhang01.sql.tool.wrapper;

import io.github.qwzhang01.sql.tool.exception.SqlIllegalException;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;

/**
 * SQL parser wrapper providing exception handling and singleton access.
 * This class wraps JSQLParser's CCJSqlParserUtil to provide consistent
 * exception handling and converts JSQLParserException to SqlIllegalException.
 *
 * @author Avin Zhang
 * @since 1.0.0
 */
public class SqlParser {

    /**
     * Private constructor to prevent instantiation
     */
    private SqlParser() {
    }

    /**
     * Gets the singleton instance of SqlParser
     *
     * @return the singleton SqlParser instance
     */
    public static SqlParser getInstance() {
        return SqlParserHolder.INSTANCE;
    }

    /**
     * Parses a SQL statement string into a Statement object
     *
     * @param sql the SQL statement to parse
     * @return the parsed Statement object
     * @throws SqlIllegalException if the SQL cannot be parsed
     */
    public Statement parse(String sql) {
        try {
            return CCJSqlParserUtil.parse(sql);
        } catch (JSQLParserException e) {
            throw new SqlIllegalException("Invalid SQL, cannot parse", e, sql);
        }
    }

    /**
     * Parses an SQL expression string into an Expression object
     *
     * @param expression the SQL expression to parse
     * @return the parsed Expression object
     * @throws SqlIllegalException if the expression cannot be parsed
     */
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
