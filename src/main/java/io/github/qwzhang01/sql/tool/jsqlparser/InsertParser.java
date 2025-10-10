package io.github.qwzhang01.sql.tool.jsqlparser;

import io.github.qwzhang01.sql.tool.model.SqlParam;
import io.github.qwzhang01.sql.tool.model.SqlTable;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.Values;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Parser for analyzing INSERT statements and extracting table and parameter information.
 * This class provides methods to parse INSERT statements and extract the target table
 * information as well as JDBC parameters from the VALUES clause.
 * 
 * <p>Key features:</p>
 * <ul>
 *   <li>Extracts target table name and alias from INSERT statements</li>
 *   <li>Identifies JDBC parameters in VALUES clauses</li>
 *   <li>Associates parameters with their corresponding column names</li>
 *   <li>Handles standard INSERT INTO ... VALUES (...) syntax</li>
 * </ul>
 *
 * @author avinzhang
 */
public class InsertParser {
    /**
     * The INSERT statement to be parsed
     */
    private final Insert insert;

    /**
     * Constructs a new InsertParser for the given INSERT statement.
     *
     * @param insert the INSERT statement to parse
     */
    public InsertParser(Insert insert) {
        this.insert = insert;
    }

    /**
     * Extracts the target table information from the INSERT statement.
     * 
     * @return a list containing a single SqlTable object representing the target table
     */
    public List<SqlTable> table() {
        Table table = insert.getTable();
        return Collections.singletonList(new SqlTable(table.getName(), table.getAlias() != null ? table.getAlias().getName() : ""));
    }

    /**
     * Extracts all JDBC parameters from the INSERT statement's VALUES clause.
     * This method analyzes the VALUES clause and associates each parameter placeholder
     * with its corresponding column name.
     *
     * @return a list of SqlParam objects containing parameter metadata
     */
    public List<SqlParam> param() {
        List<SqlParam> list = new ArrayList<>();
        ExpressionList<Column> columns = insert.getColumns();
        Values values = insert.getValues();
        ExpressionList<?> expressions = values.getExpressions();
        
        // Iterate through each value expression and check for JDBC parameters
        for (int i = 0; i < expressions.size(); i++) {
            Expression value = expressions.get(i);
            if (value instanceof JdbcParameter) {
                // Found a JDBC parameter, associate it with the corresponding column
                Column column = columns.get(i);
                SqlParam param = new SqlParam();
                if (column.getTable() != null) {
                    param.setTableAlias(column.getTable().getName());
                }
                param.setFieldName(column.getColumnName());
                list.add(param);
            }
        }
        return list;
    }
}