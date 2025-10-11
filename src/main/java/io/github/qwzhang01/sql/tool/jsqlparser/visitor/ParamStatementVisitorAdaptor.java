package io.github.qwzhang01.sql.tool.jsqlparser.visitor;

import io.github.qwzhang01.sql.tool.jsqlparser.DeleteParser;
import io.github.qwzhang01.sql.tool.jsqlparser.InsertParser;
import io.github.qwzhang01.sql.tool.jsqlparser.SelectParser;
import io.github.qwzhang01.sql.tool.jsqlparser.UpdateParser;
import io.github.qwzhang01.sql.tool.model.SqlParam;
import net.sf.jsqlparser.statement.StatementVisitorAdapter;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.update.Update;

import java.util.List;
import java.util.logging.Logger;

/**
 * Statement visitor adaptor for extracting SQL parameters (placeholders) from SQL statements.
 * This class analyzes SQL statements and identifies all JDBC parameter placeholders (?),
 * along with their associated column and table information.
 *
 * <p>Key capabilities:</p>
 * <ul>
 *   <li>Extracts JDBC parameter placeholders from all SQL statement types</li>
 *   <li>Associates parameters with their corresponding column names and table aliases</li>
 *   <li>Handles parameters in WHERE clauses, JOIN conditions, and value assignments</li>
 *   <li>Supports SELECT, INSERT, UPDATE, and DELETE statements</li>
 * </ul>
 *
 * @author avinzhang
 */
public class ParamStatementVisitorAdaptor extends StatementVisitorAdapter<Void> {
    private static final Logger log = Logger.getLogger(ParamStatementVisitorAdaptor.class.getName());
    /**
     * List of extracted SQL parameters with their metadata
     */
    private List<SqlParam> params;

    /**
     * Gets the list of SQL parameters extracted from the visited statement.
     *
     * @return the list of SqlParam objects containing parameter metadata
     */
    public List<SqlParam> getParams() {
        return params;
    }

    /**
     * Visits a DELETE statement and extracts all parameter placeholders.
     * Uses DeleteParser to analyze the DELETE statement and identify JDBC parameters
     * in WHERE clauses and JOIN conditions.
     *
     * @param delete the DELETE statement to analyze
     */
    @Override
    public <S> Void visit(Delete delete, S content) {
        log.fine("Extracting parameters from DELETE statement: " + delete.toString());
        this.params = new DeleteParser(delete).param();
        return null;
    }

    /**
     * Visits an UPDATE statement and extracts all parameter placeholders.
     * Uses UpdateParser to analyze the UPDATE statement and identify JDBC parameters
     * in SET clauses, WHERE conditions, and JOIN clauses.
     *
     * @param update the UPDATE statement to analyze
     */
    @Override
    public <S> Void visit(Update update, S content) {
        log.fine("Extracting parameters from UPDATE statement: " + update.toString());
        this.params = new UpdateParser(update).param();
        return null;
    }

    /**
     * Visits an INSERT statement and extracts all parameter placeholders.
     * Uses InsertParser to analyze the INSERT statement and identify JDBC parameters
     * in the VALUES clause, associating them with their corresponding columns.
     *
     * @param insert the INSERT statement to analyze
     */
    @Override
    public <S> Void visit(Insert insert, S content) {
        log.fine("Extracting parameters from INSERT statement: " + insert.toString());
        this.params = new InsertParser(insert).param();
        return null;
    }

    /**
     * Visits a SELECT statement and extracts all parameter placeholders.
     * Uses SelectParser to analyze the SELECT statement and identify JDBC parameters
     * in WHERE clauses, JOIN conditions, HAVING clauses, and other expressions.
     *
     * @param select the SELECT statement to analyze
     */
    @Override
    public <S> Void visit(Select select, S content) {
        log.fine("Extracting parameters from SELECT statement: " + select.toString());
        this.params = new SelectParser(select).param();
        return null;
    }
}
