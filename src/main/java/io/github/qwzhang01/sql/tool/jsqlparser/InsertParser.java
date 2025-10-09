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
 * @author avinzhang
 */
public class InsertParser {
    private final Insert insert;

    public InsertParser(Insert insert) {
        this.insert = insert;
    }

    public List<SqlTable> table() {
        Table table = insert.getTable();
        return Collections.singletonList(new SqlTable(table.getName(), table.getAlias() != null ? table.getAlias().getName() : ""));
    }

    public List<SqlParam> param() {
        List<SqlParam> list = new ArrayList<>();
        ExpressionList<Column> columns = insert.getColumns();
        Values values = insert.getValues();
        ExpressionList<?> expressions = values.getExpressions();
        for (int i = 0; i < expressions.size(); i++) {
            Expression value = expressions.get(i);
            if (value instanceof JdbcParameter) {
                Column column = columns.get(i);
                SqlParam param = new SqlParam();
                param.setTableAlias(column.getTable().getName());
                param.setFieldName(column.getColumnName());
                list.add(param);
            }
        }
        return list;
    }
}