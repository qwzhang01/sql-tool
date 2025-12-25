package io.github.qwzhang01.sql.tool.wrapper;

import io.github.qwzhang01.sql.tool.model.SqlTable;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.schema.Table;

/**
 * Table parser utility for converting JSQLParser Table objects to SqlTable objects.
 * This singleton class handles the conversion of table information from JSQLParser's
 * representation to the tool's internal SqlTable model.
 *
 * @author Avin Zhang
 * @since 1.0.0
 */
public class TableParser {

    /**
     * Private constructor to prevent instantiation
     */
    private TableParser() {
    }

    /**
     * Gets the singleton instance of TableParser
     *
     * @return the singleton TableParser instance
     */
    public static TableParser getInstance() {
        return SqlParserHolder.INSTANCE;
    }

    /**
     * Parses a JSQLParser Table object into a SqlTable object
     *
     * @param table the JSQLParser Table object
     * @return SqlTable object with name, alias, and virtual flag set appropriately
     */
    public SqlTable parse(Table table) {
        return new SqlTable(table.getName(), table.getAlias() == null ? "" : table.getAlias().getName(), false);
    }

    /**
     * Parses a JSQLParser Alias object into a SqlTable object.
     * Used for virtual tables like subqueries.
     *
     * @param table the JSQLParser Alias object
     * @return SqlTable object marked as virtual
     */
    public SqlTable parse(Alias table) {
        return new SqlTable(table.getName(), "", true);
    }


    private static class SqlParserHolder {
        private static final TableParser INSTANCE = new TableParser();
    }
}
