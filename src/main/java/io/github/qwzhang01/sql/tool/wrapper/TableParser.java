package io.github.qwzhang01.sql.tool.wrapper;

import io.github.qwzhang01.sql.tool.model.SqlTable;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.schema.Table;

/**
 * Table parser utility
 *
 * @author avinzhang
 */
public class TableParser {

    private TableParser() {
    }

    public static TableParser getInstance() {
        return SqlParserHolder.INSTANCE;
    }

    public SqlTable parse(Table table) {
        return new SqlTable(table.getName(), table.getAlias() == null ? "" : table.getAlias().getName(), false);
    }

    public SqlTable parse(Alias table) {
        return new SqlTable(table.getName(), "", true);
    }


    private static class SqlParserHolder {
        private static final TableParser INSTANCE = new TableParser();
    }
}
