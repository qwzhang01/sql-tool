package io.github.qwzhang01.sql.tool.wrapper;

import io.github.qwzhang01.sql.tool.exception.SqlIllegalException;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;

/**
 * CCJSqlParserUtil 工具
 * 异常捕捉
 *
 * @author avinzhang
 */
public class SqlParser {

    // 私有构造函数，防止外部实例化
    private SqlParser() {
        // 初始化代码可以放在这里
    }

    // 获取单例实例的公共方法
    public static SqlParser getInstance() {
        return SqlParserHolder.INSTANCE;
    }

    // 可以在这里添加其他业务方法
    public Statement parse(String sql) {
        try {
            return CCJSqlParserUtil.parse(sql);
        } catch (JSQLParserException e) {
            throw new SqlIllegalException("SQL 非法，无法转换", e, sql);
        }
    }

    public Expression parseExpression(String expression) {
        try {
            return CCJSqlParserUtil.parseExpression(expression);
        } catch (JSQLParserException e) {
            throw new SqlIllegalException("SQL 非法，无法转换", e, expression);
        }
    }

    // 静态内部类，用于持有单例实例
    private static class SqlParserHolder {
        private static final SqlParser INSTANCE = new SqlParser();
    }
}
