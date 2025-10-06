package io.github.qwzhang01.sql.tool.parser;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.NotExpression;
import net.sf.jsqlparser.expression.operators.relational.ParenthesedExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import org.junit.jupiter.api.Test;

import java.util.List;

public class JsqlparserTest {

    @Test
    public void test() throws JSQLParserException {
        String sql = "SELECT users.* FROM users\n" +
                "LEFT JOIN dept d ON users.dept_id = d.id\n" +
                "WHERE users.name = ?\n" +
                "AND users.age > ?\n" +
                "AND users.city IN (?, ?, ?)\n" +
                "AND users.salary BETWEEN ? AND ?\n" +
                "AND users.status LIKE ?";

        Statement statement = CCJSqlParserUtil.parse(sql);
        Select select = (Select) statement;
        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        List<Join> joins = plainSelect.getJoins();

        for (Join join : joins) {
            System.out.println("");
        }

        Expression where = plainSelect.getWhere();
        if (where instanceof BinaryExpression binaryExpression) {
            // 处理二元表达式
            Expression leftExpression = binaryExpression.getLeftExpression();
            Expression rightExpression = binaryExpression.getRightExpression();
        } else if (where instanceof ParenthesedExpressionList) {
            // 处理括号表达式
            ParenthesedExpressionList parenthesis = (ParenthesedExpressionList) where;
            //Expression expression = parenthesis.getExpression();
        } else if (where instanceof NotExpression) {
            // 处理NOT表达式
            NotExpression notExpression = (NotExpression) where;
            Expression expression = notExpression.getExpression();
        }
    }
}
