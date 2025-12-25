package io.github.qwzhang01.sql.tool.jsqlparser.visitor;

import io.github.qwzhang01.sql.tool.model.SqlTable;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.operators.relational.ExistsExpression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.ParenthesedSelect;
import net.sf.jsqlparser.statement.select.PlainSelect;

import java.util.Collection;
import java.util.List;

/**
 * Expression visitor that completes table references by resolving and applying aliases.
 * This visitor ensures that all column references use the correct table aliases
 * based on the provided table list.
 *
 * @author Avin Zhang
 * @since 1.0.0
 */
public class CompleteTableVisitor extends ExpressionVisitorAdapter<Void> {

    /**
     * List of tables with their aliases for resolution
     */
    private final List<SqlTable> table;

    /**
     * Constructs a CompleteTableVisitor with a list of tables for alias resolution
     *
     * @param table list of SqlTable objects containing table names and aliases
     */
    public CompleteTableVisitor(List<SqlTable> table) {
        this.table = table;
    }

    /**
     * Resolves the alias for a given table name by searching through the table list
     *
     * @param table the table name to resolve
     * @return the resolved alias or the original table name if no alias found
     */
    private String getAlias(String table) {
        for (SqlTable sqlTable : this.table) {
            String alias = sqlTable.getAlias(table);
            if (alias == null) {
                alias = "";
            }
            if (!alias.isEmpty()) {
                alias = alias.trim().replace("`", "");
            }
            table = table.trim().replace("`", "");

            if (!alias.equalsIgnoreCase(table)) {
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
            if (!cAlias.equalsIgnoreCase(cTable.getName())) {
                cTable.setName(cAlias);
            }
        }
        return null;
    }

    @Override
    public <S> Void visit(ExistsExpression expression, S context) {
        Expression rightExpression = expression.getRightExpression();
        if (rightExpression instanceof ParenthesedSelect select) {
            PlainSelect plainSelect = select.getPlainSelect();
            List<Join> joins = plainSelect.getJoins();
            if (joins != null && !joins.isEmpty()) {
                for (Join join : joins) {
                    Collection<Expression> ons = join.getOnExpressions();
                    for (Expression on : ons) {
                        on.accept(this, context);
                    }
                }
            }
            if (plainSelect.getWhere() != null) {
                plainSelect.getWhere().accept(this, context);
            }
        }
        return null;
    }
}
