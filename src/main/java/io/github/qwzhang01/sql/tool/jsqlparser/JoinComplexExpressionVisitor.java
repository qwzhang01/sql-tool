package io.github.qwzhang01.sql.tool.jsqlparser;

import io.github.qwzhang01.sql.tool.model.SqlTable;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 补全 join 中表的别名
 *
 * @author avinzhang
 */
public class JoinComplexExpressionVisitor extends ExpressionVisitorAdapter {
    private final String joinTableName;
    private final Map<String, SqlTable> table;

    public JoinComplexExpressionVisitor(String joinTableName, List<SqlTable> table) {
        this.joinTableName = joinTableName;
        this.table = table.stream().collect(Collectors.toMap(k -> k.getTableName() + (k.getAlias() != null && !k.getAlias().isEmpty() ? ":" + k.getAlias() : ""),
                v -> v, (v1, v2) -> v1));
    }


    @Override
    public void visit(Column column) {
        Table cTable = column.getTable();
        if (cTable != null) {
            String name = cTable.getName();
            if (!name.equals(joinTableName)) {
                Optional<String> any = table.keySet().stream().filter(s -> s.startsWith(name + ":")).findAny();
                if (any.isPresent()) {
                    String key = any.get();
                    SqlTable sqlTable = table.get(key);
                    if (sqlTable.getAlias() != null && !sqlTable.getAlias().isEmpty()) {
                        cTable.setName(sqlTable.getAlias());
                    }
                }
            }
        }
    }
}
