package io.github.qwzhang01.sql.tool.jsqlparser.visitor;

import io.github.qwzhang01.sql.tool.jsqlparser.SelectParser;
import io.github.qwzhang01.sql.tool.model.SqlTable;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.StatementVisitorAdapter;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.update.Update;

import java.util.Collection;
import java.util.List;

/**
 * Statement visitor adaptor for merging JOIN and WHERE clauses into existing SQL statements.
 * This class takes additional JOIN and WHERE clauses and merges them with the original
 * SQL statement to create a modified version.
 *
 * <p>Key capabilities:</p>
 * <ul>
 *   <li>Merges additional JOIN clauses with existing SELECT statements</li>
 *   <li>Combines WHERE conditions using AND logic</li>
 *   <li>Handles table alias resolution and replacement</li>
 *   <li>Produces a complete modified SQL statement</li>
 * </ul>
 *
 * <p>Supported operations:</p>
 * <ul>
 *   <li>SELECT statements - full merge support</li>
 *   <li>INSERT, UPDATE, DELETE statements - throws UnsupportedOperationException</li>
 * </ul>
 *
 * @author avinzhang
 */
public class MergeStatementVisitor extends StatementVisitorAdapter<Void> {
    /**
     * The final merged SQL statement as a string
     */
    private String sql;

    /**
     * Additional JOIN clauses to be merged into the statement
     */
    private List<Join> joins;

    /**
     * Additional WHERE expression to be merged into the statement
     */
    private Expression where;

    /**
     * Sets the JOIN clauses to be merged with the visited statement.
     *
     * @param joins the list of JOIN clauses to merge
     */
    public void setJoins(List<Join> joins) {
        this.joins = joins;
    }

    /**
     * Sets the WHERE expression to be merged with the visited statement.
     *
     * @param where the WHERE expression to merge
     */
    public void setWhere(Expression where) {
        this.where = where;
    }

    /**
     * Gets the final merged SQL statement as a string.
     *
     * @return the complete merged SQL statement
     */
    public String getSql() {
        return sql;
    }

    /**
     * Visits a DELETE statement.
     * DELETE statements are not supported for merging operations.
     *
     * @param delete the DELETE statement
     * @throws UnsupportedOperationException always, as DELETE merging is not supported
     */
    @Override
    public void visit(Delete delete) {
        throw new UnsupportedOperationException("DELETE statements cannot be merged");
    }

    /**
     * Visits an UPDATE statement.
     * UPDATE statements are not supported for merging operations.
     *
     * @param update the UPDATE statement
     * @throws UnsupportedOperationException always, as UPDATE merging is not supported
     */
    @Override
    public void visit(Update update) {
        throw new UnsupportedOperationException("UPDATE statements cannot be merged");
    }

    /**
     * Visits an INSERT statement.
     * INSERT statements are not supported for merging operations.
     *
     * @param insert the INSERT statement
     * @throws UnsupportedOperationException always, as INSERT merging is not supported
     */
    @Override
    public void visit(Insert insert) {
        throw new UnsupportedOperationException("INSERT statements cannot be merged");
    }

    /**
     * Visits a SELECT statement and performs the merge operation.
     * This method merges additional JOIN and WHERE clauses with the existing SELECT statement.
     *
     * <p>The merge process includes:</p>
     * <ol>
     *   <li>Extract table information for alias resolution</li>
     *   <li>Process and merge JOIN clauses with table alias replacement</li>
     *   <li>Process and merge WHERE conditions using AND logic</li>
     *   <li>Generate the final merged SQL statement</li>
     * </ol>
     *
     * @param select the SELECT statement to merge with
     */
    @Override
    public void visit(Select select) {
        PlainSelect plainSelect = select.getPlainSelect();
        if (plainSelect == null) {
            return;
        }

        // Extract table information for alias resolution during merge
        List<SqlTable> table = new SelectParser(select).table(true);

        // Process and merge JOIN clauses
        if (joins != null && !joins.isEmpty()) {
            for (Join join : joins) {
                // Extract join table name and alias for reference replacement
                String joinTableName = "";
                FromItem item = join.getFromItem();
                if (item instanceof Table joinTable) {
                    joinTableName = joinTable.getName();
                    Alias alias = joinTable.getAlias();
                    if (alias != null) {
                        joinTableName = alias.getName();
                    }
                }

                // Replace table aliases in JOIN conditions
                Collection<Expression> ons = join.getOnExpressions();
                for (Expression on : ons) {
                    on.accept(new JoinComplexExpressionVisitor(joinTableName, table));
                }
            }

            // Add all processed JOIN clauses to the SELECT statement
            plainSelect.addJoins(joins);
        }

        // Process and merge WHERE conditions
        if (where != null) {
            // Replace table aliases in the WHERE clause
            where.accept(new WhereComplexExpressionVisitor(table));

            Expression mainWhere = plainSelect.getWhere();
            if (mainWhere != null) {
                // Combine existing WHERE with additional WHERE using AND
                plainSelect.setWhere(new AndExpression(mainWhere, where));
            } else {
                // No existing WHERE clause, set the new WHERE directly
                plainSelect.setWhere(where);
            }
        }

        // Generate the final merged SQL statement
        sql = select.toString();
    }
}