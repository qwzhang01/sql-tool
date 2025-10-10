package io.github.qwzhang01.sql.tool.jsqlparser;

import io.github.qwzhang01.sql.tool.model.SqlTable;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Parser for extracting table references from WHERE clause expressions.
 * This class analyzes complex WHERE clause expressions to identify all table references
 * including those in subqueries, column references, and nested expressions.
 * 
 * <p>Key features:</p>
 * <ul>
 *   <li>Extracts table references from column expressions</li>
 *   <li>Handles subqueries in WHERE clauses</li>
 *   <li>Processes IN expressions with subqueries</li>  
 *   <li>Supports JOIN clauses within subqueries</li>
 *   <li>Recursively analyzes nested WHERE conditions</li>
 * </ul>
 *
 * @author avinzhang
 */
public class WhereTableParser {
    private List<SqlTable> tables = null;

    /**
     * Extracts all table references from the given WHERE clause expression.
     * This method initializes the table collection and uses a visitor pattern
     * to traverse the expression tree and collect all table references.
     *
     * @param where the WHERE clause expression to analyze
     * @return a list of SqlTable objects representing all tables found in the expression
     */
    public List<SqlTable> extractTable(Expression where) {
        tables = new ArrayList<>();
        where.accept(new WhereExpressionVisitor());
        return tables;
    }

    /**
     * Custom ExpressionVisitor implementation for extracting table names from WHERE expressions.
     * This visitor handles various types of expressions including column references,
     * subqueries, and IN expressions to collect all table references.
     */
    private class WhereExpressionVisitor extends ExpressionVisitorAdapter<Void> {
        /**
         * Visits column expressions to extract table references.
         * When a column is referenced with a table qualifier, this method
         * extracts the table name and alias information.
         *
         * @param column the column expression to visit
         * @param context the visitor context
         * @return null (required by visitor pattern)
         */
        @Override
        public <S> Void visit(Column column, S context) {
            // Extract table name from column reference
            Table table = column.getTable();
            if (table != null && table.getName() != null) {
                tables.add(new SqlTable(table.getName(), table.getAlias() != null ? table.getAlias().getName() : null));
            }
            return null;
        }

        /**
         * Visits SELECT subqueries to extract table references.
         * This method processes subquery expressions by analyzing their FROM clauses,
         * JOIN clauses, and recursively processing nested WHERE conditions.
         *
         * @param subSelect the SELECT subquery to visit
         * @param context the visitor context
         * @return null (required by visitor pattern)
         */
        @Override
        public <S> Void visit(Select subSelect, S context) {
            PlainSelect plainSelect = subSelect.getPlainSelect();
            // Process table names in subquery
            // Parse FROM clause
            FromItem fromItem = plainSelect.getFromItem();
            if (fromItem != null) {
                fromItem.accept(new TableNameFromItemVisitor());
            }
            // Parse JOIN clauses
            if (plainSelect.getJoins() != null) {
                plainSelect.getJoins().forEach(join -> {
                    FromItem rightItem = join.getRightItem();
                    if (rightItem != null) {
                        rightItem.accept(new TableNameFromItemVisitor());
                    }
                });
            }
            // Recursively parse WHERE clause of subquery
            Expression where = plainSelect.getWhere();
            if (where != null) {
                where.accept(this);
            }
            return null;
        }

        /**
         * Visits IN expressions to extract table references from subqueries.
         * This method handles IN clauses that contain SELECT subqueries
         * and processes both the left and right expressions.
         *
         * @param inExpression the IN expression to visit
         * @param context the visitor context
         * @return null (required by visitor pattern)
         */
        @Override
        public <S> Void visit(InExpression inExpression, S context) {
            // Handle IN subqueries
            if (inExpression.getRightExpression() instanceof Select) {
                inExpression.getRightExpression().accept(this);
            }
            // Continue processing left expression
            if (inExpression.getLeftExpression() != null) {
                inExpression.getLeftExpression().accept(this);
            }
            return null;
        }
    }

    /**
     * Custom FromItemVisitor implementation for extracting table names from FROM and JOIN clauses.
     * This visitor handles different types of FROM items including direct table references
     * and parenthesized subqueries, collecting all table information recursively.
     */
    private class TableNameFromItemVisitor extends FromItemVisitorAdapter<Void> {
        
        /**
         * Visits table references in FROM and JOIN clauses.
         * This method extracts the table name and alias information
         * from direct table references.
         *
         * @param table the table reference to visit
         * @param context the visitor context
         * @return null (required by visitor pattern)
         */
        @Override
        public <S> Void visit(Table table, S context) {
            // Directly extract table name
            if (table.getName() != null) {
                tables.add(new SqlTable(table.getName(), table.getAlias() != null ? table.getAlias().getName() : null));
            }
            return null;
        }

        /**
         * Visits parenthesized SELECT subqueries in FROM and JOIN clauses.
         * This method recursively processes subqueries by analyzing their FROM clauses,
         * JOIN clauses, and WHERE conditions to extract all table references.
         *
         * @param subSelect the parenthesized SELECT subquery to visit
         * @param context the visitor context
         * @return null (required by visitor pattern)
         */
        @Override
        public <S> Void visit(ParenthesedSelect subSelect, S context) {
            PlainSelect plainSelect = subSelect.getPlainSelect();
            FromItem fromItem = plainSelect.getFromItem();
            if (fromItem != null) {
                fromItem.accept(this);
            }
            if (plainSelect.getJoins() != null) {
                plainSelect.getJoins().forEach(join -> {
                    FromItem rightItem = join.getRightItem();
                    if (rightItem != null) {
                        rightItem.accept(this);
                    }
                });
            }
            // Recursively process subquery WHERE clause
            Expression where = plainSelect.getWhere();
            if (where != null) {
                where.accept(new WhereExpressionVisitor());
            }
            return null;
        }
    }
}