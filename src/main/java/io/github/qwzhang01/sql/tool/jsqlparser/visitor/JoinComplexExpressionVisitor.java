package io.github.qwzhang01.sql.tool.jsqlparser.visitor;

import io.github.qwzhang01.sql.tool.model.SqlTable;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Expression visitor for completing table aliases in JOIN clause expressions.
 * This class analyzes JOIN condition expressions and ensures that table references
 * use the correct aliases when available, excluding the current JOIN table being processed.
 *
 * <p>Key features:</p>
 * <ul>
 *   <li>Completes table aliases in JOIN condition column references</li>
 *   <li>Excludes the current JOIN table from alias completion</li>
 *   <li>Maintains table alias mapping for consistent reference resolution</li>
 *   <li>Handles complex JOIN conditions with multiple table references</li>
 * </ul>
 *
 * @author avinzhang
 */
public class JoinComplexExpressionVisitor extends ExpressionVisitorAdapter<Void> {
    private final String joinTableName;
    private final Map<String, SqlTable> table;

    /**
     * Constructs a new JoinComplexExpressionVisitor for the specified JOIN table.
     * Creates a mapping from table names (with optional aliases) to SqlTable objects
     * for efficient alias resolution during JOIN condition processing.
     *
     * @param joinTableName the name of the table being joined (excluded from alias completion)
     * @param table         the list of tables with their aliases to use for reference completion
     */
    public JoinComplexExpressionVisitor(String joinTableName, List<SqlTable> table) {
        this.joinTableName = joinTableName;
        this.table = table.stream().collect(Collectors.toMap(k -> k.getName() + (k.getAlias() != null && !k.getAlias().isEmpty() ? ":" + k.getAlias() : ""),
                v -> v, (v1, v2) -> v1));
    }

    /**
     * Visits column expressions in JOIN conditions to complete table alias references.
     * This method checks if a column's table reference has an alias defined and updates
     * the column to use the alias instead of the full table name. The current JOIN table
     * is excluded from alias completion to avoid conflicts.
     *
     * @param column  the column expression to visit
     * @param content the visitor context
     * @return null (required by visitor pattern)
     */
    @Override
    public <S> Void visit(Column column, S content) {
        Table cTable = column.getTable();
        if (cTable != null) {
            String name = cTable.getName();
            // Skip alias completion for the current JOIN table
            if (!name.equals(joinTableName)) {
                Optional<String> any = table.keySet().stream().filter(s -> s.startsWith(name + ":")).findAny();
                if (any.isPresent()) {
                    String key = any.get();
                    SqlTable sqlTable = table.get(key);
                    // Apply alias if available
                    if (sqlTable.getAlias() != null && !sqlTable.getAlias().isEmpty()) {
                        cTable.setName(sqlTable.getAlias());
                    }
                }
            }
        }
        return null;
    }
}
