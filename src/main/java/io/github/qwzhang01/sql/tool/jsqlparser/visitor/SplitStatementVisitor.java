package io.github.qwzhang01.sql.tool.jsqlparser.visitor;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.StatementVisitorAdapter;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.update.Update;

import java.util.List;

/**
 * Statement visitor adaptor for extracting JOIN and WHERE clauses from SQL statements.
 * This class is designed to parse SQL statements and extract their JOIN and WHERE
 * components for later merging with other SQL statements.
 *
 * <p>Primary use cases:</p>
 * <ul>
 *   <li>Extracting JOIN clauses from SELECT, UPDATE, and DELETE statements</li>
 *   <li>Extracting WHERE conditions for SQL merging operations</li>
 *   <li>Supporting dynamic SQL construction and modification</li>
 * </ul>
 *
 * @author avinzhang
 */
public class SplitStatementVisitor extends StatementVisitorAdapter<Void> {

    /**
     * List of JOIN clauses extracted from the visited statement
     */
    private List<Join> joins;

    /**
     * WHERE expression extracted from the visited statement
     */
    private Expression where;

    /**
     * Gets the WHERE expression extracted from the visited statement.
     *
     * @return the WHERE expression, or null if no WHERE clause was found
     */
    public Expression getWhere() {
        return where;
    }

    /**
     * Gets the list of JOIN clauses extracted from the visited statement.
     *
     * @return the list of JOIN clauses, or null if no JOINs were found
     */
    public List<Join> getJoins() {
        return joins;
    }

    /**
     * Visits a DELETE statement and extracts its JOIN and WHERE clauses.
     *
     * @param delete the DELETE statement to process
     */
    @Override
    public <S> Void visit(Delete delete, S content) {
        this.joins = delete.getJoins();
        this.where = delete.getWhere();
        return null;
    }

    /**
     * Visits an UPDATE statement and extracts its JOIN and WHERE clauses.
     *
     * @param update the UPDATE statement to process
     */
    @Override
    public <S> Void visit(Update update, S content) {
        this.joins = update.getJoins();
        this.where = update.getWhere();
        return null;
    }

    /**
     * Visits a SELECT statement and extracts its JOIN and WHERE clauses.
     * This method processes the PlainSelect component to extract the relevant clauses.
     *
     * @param select the SELECT statement to process
     */
    @Override
    public <S> Void visit(Select select, S content) {
        PlainSelect plainSelect = select.getPlainSelect();
        if (plainSelect != null) {
            this.joins = plainSelect.getJoins();
            this.where = plainSelect.getWhere();
        }
        return null;
    }
}
