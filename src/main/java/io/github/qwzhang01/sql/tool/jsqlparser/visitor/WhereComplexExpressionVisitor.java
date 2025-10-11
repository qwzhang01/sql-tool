package io.github.qwzhang01.sql.tool.jsqlparser.visitor;

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
 * Expression visitor for completing table aliases in WHERE clause expressions.
 * This class analyzes WHERE clause expressions and ensures that table references
 * use the correct aliases when available, providing consistent table naming
 * throughout complex WHERE conditions.
 *
 * <p>Key features:</p>
 * <ul>
 *   <li>Completes table aliases in column references</li>
 *   <li>Handles EXISTS expressions with subqueries</li>
 *   <li>Maintains table alias mapping for consistent reference resolution</li>
 *   <li>Processes nested WHERE conditions recursively</li>
 * </ul>
 *
 * @author avinzhang
 */
public class WhereComplexExpressionVisitor extends ExpressionVisitorAdapter<Void> {
    private final Map<String, SqlTable> table;

    /**
     * Constructs a new WhereComplexExpressionVisitor with the given table list.
     * Creates a mapping from table names (with optional aliases) to SqlTable objects
     * for efficient alias resolution during expression processing.
     *
     * @param table the list of tables with their aliases to use for reference completion
     */
    public WhereComplexExpressionVisitor(List<SqlTable> table) {
        // Create mapping for main table name replacement
        this.table = table.stream().collect(Collectors.toMap(k -> k.getName() + (k.getAlias() != null && !k.getAlias().isEmpty() ? ":" + k.getAlias() : ""), v -> v, (v1, v2) -> v1));
    }

    /**
     * Visits column expressions to complete table alias references.
     * This method checks if a column's table reference has an alias defined
     * and updates the column to use the alias instead of the full table name.
     *
     * @param column  the column expression to visit
     * @param context the visitor context
     * @return null (required by visitor pattern)
     */
    @Override
    public <S> Void visit(Column column, S context) {
        Table cTable = column.getTable();
        if (cTable != null) {
            // Find and apply table alias if available
            table.keySet().stream().filter(k -> k.startsWith(cTable.getName() + ":")).findAny().ifPresent(k -> {
                String[] split = k.split(":");
                String alias = split[1];
                cTable.setName(alias);
            });
        }
        return null;
    }

    /**
     * Visits EXISTS expressions to process nested subqueries.
     * This method handles EXISTS and NOT EXISTS expressions by recursively
     * processing the WHERE clauses of their subqueries to complete table aliases.
     *
     * @param exits   the EXISTS expression to visit
     * @param context the visitor context
     * @return null (required by visitor pattern)
     */
    @Override
    public <S> Void visit(ExistsExpression exits, S context) {
        Expression expression = exits.getRightExpression();
        if (expression instanceof ParenthesedSelect parenthesedSelect) {
            PlainSelect plainSelect = parenthesedSelect.getPlainSelect();
            Expression where = plainSelect.getWhere();
            if (where != null) {
                // Recursively process WHERE clause in EXISTS subquery
                where.accept(this);
            }
        }
        return null;
    }
}
