package io.github.qwzhang01.sql.tool.jsqlparser;

import io.github.qwzhang01.sql.tool.model.SqlParam;
import io.github.qwzhang01.sql.tool.model.SqlTable;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author avinzhang
 */
public class SelectParser {
    private static final Logger logger = Logger.getLogger(SelectParser.class.getName());

    private final Select select;

    public SelectParser(Select select) {
        this.select = select;
    }

    public List<SqlTable> table(boolean deeply) {
        PlainSelect plainSelect = select.getPlainSelect();
        return ExpressionParse.getInstance().getTable(plainSelect, deeply);
    }


    public List<SqlParam> param() {
        List<SqlParam> result = new ArrayList<>();
        PlainSelect plainSelect = select.getPlainSelect();

        List<Join> joins = plainSelect.getJoins();
        if (joins != null && !joins.isEmpty()) {
            for (Join join : joins) {
                List<SqlParam> params = join(join);
                if (params != null && !params.isEmpty()) {
                    result.addAll(params);
                }
            }
        }
        Expression where = plainSelect.getWhere();
        if (where != null) {
            List<SqlParam> sqlParams = ExpressionParse.getInstance().parseExpression(where);
            if (sqlParams != null && !sqlParams.isEmpty()) {
                result.addAll(sqlParams);
            }
        }
        // group by
        GroupByElement groupBy = plainSelect.getGroupBy();
        if (groupBy != null) {
            logger.info("GROUP BY: " + groupBy);
        }
        Expression having = plainSelect.getHaving();
        if (having != null) {
            logger.info("having: " + having);
        }
        // order by
        List<OrderByElement> orderByElements = plainSelect.getOrderByElements();
        if (orderByElements != null && !orderByElements.isEmpty()) {
            for (OrderByElement orderByElement : orderByElements) {
                logger.info("ORDER BY: " + orderByElement);
            }
        }
        // limit
        Limit limit = plainSelect.getLimit();
        if (limit != null) {
            logger.info("LIMIT: " + limit);
        }
        return result;
    }

    private List<SqlParam> join(Join join) {
        List<SqlParam> sqlParams = new ArrayList<>();
        Collection<Expression> expressions = join.getOnExpressions();
        for (Expression expression : expressions) {
            List<SqlParam> params = ExpressionParse.getInstance().parseExpression(expression);
            if (params != null && !params.isEmpty()) {
                sqlParams.addAll(params);
            }
        }
        return sqlParams;
    }
}