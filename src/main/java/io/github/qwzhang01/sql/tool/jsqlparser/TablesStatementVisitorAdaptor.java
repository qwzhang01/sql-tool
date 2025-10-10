package io.github.qwzhang01.sql.tool.jsqlparser;

import io.github.qwzhang01.sql.tool.model.SqlTable;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.update.Update;

import java.util.List;

/**
 * Statement visitor adaptor for extracting table information from SQL statements.
 * This class analyzes SQL statements and extracts all table references including
 * table names, aliases, and optionally tables from nested subqueries.
 * 
 * <p>Features:</p>
 * <ul>
 *   <li>Extracts table names and aliases from all types of SQL statements</li>
 *   <li>Supports both shallow and deep parsing modes</li>
 *   <li>Deep parsing includes tables from subqueries and nested statements</li>
 *   <li>Handles SELECT, INSERT, UPDATE, and DELETE statements</li>
 * </ul>
 *
 * @author avinzhang
 */
public class TablesStatementVisitorAdaptor extends StatementVisitorAdaptor<Void> {

    /**
     * Flag indicating whether to perform deep parsing (including subqueries)
     */
    private final boolean deeply;
    
    /**
     * List of extracted table information
     */
    private List<SqlTable> tables;

    /**
     * Constructs a new TablesStatementVisitorAdaptor with the specified parsing depth.
     *
     * @param deeply if true, performs deep parsing including subqueries; 
     *               if false, performs shallow parsing of direct table references only
     */
    public TablesStatementVisitorAdaptor(boolean deeply) {
        this.deeply = deeply;
    }

    /**
     * Gets the list of tables extracted from the visited SQL statement.
     *
     * @return the list of SqlTable objects containing table names and aliases
     */
    public List<SqlTable> getTables() {
        return tables;
    }

    /**
     * Visits a DELETE statement and extracts all table references.
     * Uses DeleteParser to analyze the DELETE statement and extract table information.
     *
     * @param delete the DELETE statement to analyze
     */
    @Override
    public void visit(Delete delete) {
        log.info("Processing DELETE statement: " + delete.toString());
        this.tables = new DeleteParser(delete).table();
    }

    /**
     * Visits an UPDATE statement and extracts all table references.
     * Uses UpdateParser to analyze the UPDATE statement and extract table information.
     *
     * @param update the UPDATE statement to analyze
     */
    @Override
    public void visit(Update update) {
        log.info("Processing UPDATE statement: " + update.toString());
        this.tables = new UpdateParser(update).table();
    }

    /**
     * Visits an INSERT statement and extracts all table references.
     * Uses InsertParser to analyze the INSERT statement and extract table information.
     *
     * @param insert the INSERT statement to analyze
     */
    @Override
    public void visit(Insert insert) {
        log.info("Processing INSERT statement: " + insert.toString());
        this.tables = new InsertParser(insert).table();
    }

    /**
     * Visits a SELECT statement and extracts all table references.
     * Uses SelectParser to analyze the SELECT statement and extract table information.
     * The parsing depth is controlled by the 'deeply' flag set in the constructor.
     *
     * @param select the SELECT statement to analyze
     */
    @Override
    public void visit(Select select) {
        log.info("Processing SELECT statement: " + select.toString());
        this.tables = new SelectParser(select).table(deeply);
    }


}
