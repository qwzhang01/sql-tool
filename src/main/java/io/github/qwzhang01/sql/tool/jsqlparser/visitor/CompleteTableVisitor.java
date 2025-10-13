package io.github.qwzhang01.sql.tool.jsqlparser.visitor;

import io.github.qwzhang01.sql.tool.model.SqlTable;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.operators.relational.ExistsExpression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;

import java.util.List;

public class CompleteTableVisitor extends ExpressionVisitorAdapter<Void> {

    private final List<SqlTable> table;

    public CompleteTableVisitor(List<SqlTable> table) {
        this.table = table;
    }

    private String getAlias(String table) {
        for (SqlTable sqlTable : this.table) {
            String alias = sqlTable.getAlias(table);
            if (!alias.equals(table)) {
                return alias;
            }
        }
        return table;
    }

    @Override
    public <S> Void visit(Column column, S context) {
        Table cTable = column.getTable();
        if (cTable != null) {
            String cAlias = getAlias(cTable.getName());
            if (!cAlias.equals(cTable.getName())) {
                cTable.setName(cAlias);
            }
        }
        return null;
    }

    @Override
    public <S> Void visit(ExistsExpression expression, S context) {
        expression.getRightExpression().accept(this, context);
        return null;
    }
}
