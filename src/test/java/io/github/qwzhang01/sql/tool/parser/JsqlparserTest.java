package io.github.qwzhang01.sql.tool.parser;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.NotExpression;
import net.sf.jsqlparser.expression.operators.relational.ParenthesedExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import org.junit.jupiter.api.Test;

public class JsqlparserTest {

    @Test
    public void test() throws JSQLParserException {
        String sql = "SELECT * FROM T WHERE A > 5 AND B < 10 OR C = 'test'";
        Statement statement = CCJSqlParserUtil.parse(sql);
        Select select = (Select) statement;
        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        Expression where = plainSelect.getWhere();
        if (where instanceof BinaryExpression) {
            // 处理二元表达式
            BinaryExpression binaryExpression = (BinaryExpression) where;
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
