package io.github.qwzhang01.sql.tool.jsqlparser;

import io.github.qwzhang01.sql.tool.model.SqlTable;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.operators.relational.ExistsExpression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.ParenthesedSelect;
import net.sf.jsqlparser.statement.select.PlainSelect;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 补全where中表的别名
 *
 * @author avinzhang
 */
public class WhereComplexExpressionVisitor extends ExpressionVisitorAdapter {
    private final Map<String, SqlTable> table;

    public WhereComplexExpressionVisitor(List<SqlTable> table) {
        // 主表名称替换
        this.table = table.stream().collect(Collectors.toMap(k -> k.getTableName() + (k.getAlias() != null && !k.getAlias().isEmpty() ?
                        ":" + k.getAlias() : ""),
                v -> v, (v1, v2) -> v1));
    }

    @Override
    public void visit(Column column) {
        Table cTable = column.getTable();
        if (cTable != null) {
            table.keySet().stream().filter(k -> k.startsWith(cTable.getName() + ":"))
                    .findAny()
                    .ifPresent(k -> {
                        String[] split = k.split(":");
                        String alias = split[1];
                        cTable.setName(alias);
                    });
        }
    }

    @Override
    public void visit(ExistsExpression exits) {
        Expression expression = exits.getRightExpression();
        if (expression instanceof ParenthesedSelect parenthesedSelect) {
            PlainSelect plainSelect = parenthesedSelect.getPlainSelect();
            Expression where = plainSelect.getWhere();
            if (where != null) {
                where.accept(this);
            }
        }
    }
}
