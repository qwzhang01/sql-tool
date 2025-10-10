package io.github.qwzhang01.sql.tool.jsqlparser;

import io.github.qwzhang01.sql.tool.model.SqlParam;
import io.github.qwzhang01.sql.tool.model.SqlTable;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.update.UpdateSet;

import java.util.*;
import java.util.logging.Logger;

/**
 * Parser for analyzing UPDATE statements and extracting table and parameter information.
 * This class provides comprehensive analysis of UPDATE statements including target tables,
 * JOIN clauses, WHERE conditions, and JDBC parameters.
 *
 * <p>Key features:</p>
 * <ul>
 *   <li>Extracts target table and joined tables from UPDATE statements</li>
 *   <li>Handles complex UPDATE statements with JOINs</li>
 *   <li>Identifies JDBC parameters in SET clauses, WHERE conditions, and JOIN clauses</li>
 *   <li>Supports deep parsing of subqueries and nested table references</li>
 *   <li>Provides detailed logging for debugging and analysis</li>
 * </ul>
 *
 * @author avinzhang
 */
public class UpdateParser {
    /**
     * Logger instance for this parser
     */
    private static final Logger logger = Logger.getLogger(UpdateParser.class.getName());

    /**
     * The UPDATE statement to be parsed
     */
    private final Update update;

    /**
     * Constructs a new UpdateParser for the given UPDATE statement.
     *
     * @param update the UPDATE statement to parse
     */
    public UpdateParser(Update update) {
        this.update = update;
    }

    /**
     * Extracts all table information from the UPDATE statement.
     * This method performs deep parsing by default, including tables from subqueries.
     *
     * @return a list of SqlTable objects representing all tables in the UPDATE statement
     */
    public List<SqlTable> table() {
        return getTable(update, true);
    }

    /**
     * Extracts table information from the UPDATE statement with configurable parsing depth.
     * This method analyzes the target table, JOIN clauses, and optionally WHERE clause subqueries.
     *
     * @param update the UPDATE statement to analyze (parameter name should be 'update' not 'delete')
     * @param deeply if true, recursively analyzes subqueries; if false, only direct references
     * @return a list of SqlTable objects with duplicates removed
     */
    private List<SqlTable> getTable(Update update, boolean deeply) {
        List<SqlTable> result = new ArrayList<>();

        // Extract the main target table
        Table table = update.getTable();
        result.add(new SqlTable(table.getName(), table.getAlias() != null ? table.getAlias().getName() : ""));

        // Extract tables from JOIN clauses
        List<Join> joins = update.getJoins();
        if (joins != null && !joins.isEmpty()) {
            for (Join join : joins) {
                // Process left side of JOIN (if applicable)
                FromItem leftItem = join.getFromItem();
                List<SqlTable> joinTables = fromItem(leftItem, deeply);
                if (!joinTables.isEmpty()) {
                    result.addAll(joinTables);
                }

                // Log JOIN information for debugging
                logger.fine("JOIN type: " + join.getJoinHint());
                Collection<Expression> expressions = join.getOnExpressions();
                for (Expression expression : expressions) {
                    logger.fine("ON condition: " + expression);
                }

                // Process right side of JOIN
                FromItem rightItem = join.getRightItem();
                joinTables = fromItem(rightItem, deeply);
                if (!joinTables.isEmpty()) {
                    result.addAll(joinTables);
                }
            }
        }

        // Extract tables from WHERE clause (if deep parsing is enabled)
        Expression where = update.getWhere();
        if (where != null && deeply) {
            List<SqlTable> tables = new WhereTableParser().extractTable(where);
            if (tables != null && !tables.isEmpty()) {
                result.addAll(tables);
            }
        }

        // Remove duplicates based on table name and alias combination
        Set<String> mut = new HashSet<>();
        return result.stream().filter(s -> {
            String key = s.getTableName() + s.getAlias();
            if (mut.contains(key)) {
                return false;
            }
            mut.add(key);
            return true;
        }).toList();
    }

    /**
     * Extracts table information from FROM item expressions.
     * This method handles various types of FROM items including direct table references,
     * parenthesized subqueries, and lateral subqueries.
     *
     * @param fromItem the FROM item to analyze
     * @param deeply   if true, recursively analyzes subqueries
     * @return a list of SqlTable objects found in the FROM item
     */
    private List<SqlTable> fromItem(FromItem fromItem, boolean deeply) {
        List<SqlTable> result = new ArrayList<>();
        if (fromItem == null) {
            return result;
        }

        // Handle direct table references
        if (fromItem instanceof Table table) {
            logger.fine("Table found: " + table.getName());
            result.add(new SqlTable(table.getName(), table.getAlias() != null ? table.getAlias().getName() : ""));
        }

        // Handle parenthesized subqueries
        if (fromItem instanceof ParenthesedSelect subSelect) {
            Alias alias = subSelect.getAlias();
            if (alias != null) {
                String name = alias.getName().trim();
                // Remove "AS" keyword if present
                if (name.toUpperCase().startsWith("AS ")) {
                    name = name.substring(2).trim();
                }
                result.add(new SqlTable("", name));
            }

            // Recursively parse subquery if deep parsing is enabled
            if (deeply) {
                PlainSelect plainSelect = subSelect.getPlainSelect();
                if (plainSelect != null) {
                    result.addAll(ExpressionParse.getInstance().getTable(plainSelect, true));
                }
            }
        }

        // Handle lateral subqueries
        if (fromItem instanceof LateralSubSelect subSelect) {
            if (deeply) {
                PlainSelect plainSelect = subSelect.getPlainSelect();
                if (plainSelect != null) {
                    result.addAll(ExpressionParse.getInstance().getTable(plainSelect, true));
                }
            }
        }

        return result;
    }

    /**
     * Extracts all JDBC parameters from the UPDATE statement.
     * This method analyzes SET clauses, JOIN conditions, and WHERE clauses
     * to identify parameter placeholders and their associated metadata.
     *
     * @return a list of SqlParam objects containing parameter information
     */
    public List<SqlParam> param() {
        List<SqlParam> result = new ArrayList<>();

        // Extract parameters from SET clauses
        List<UpdateSet> updateSets = update.getUpdateSets();
        for (UpdateSet updateSet : updateSets) {
            ExpressionList<Column> columns = updateSet.getColumns();
            ExpressionList<?> values = updateSet.getValues();

            // Check each value in the SET clause for JDBC parameters
            for (int i = 0; i < values.size(); i++) {
                Object value = values.get(i);
                if (value instanceof JdbcParameter) {
                    Column column = columns.get(i);
                    SqlParam param = new SqlParam();
                    if (column.getTable() != null) {
                        param.setTableAlias(column.getTable().getName());
                    }
                    param.setFieldName(column.getColumnName());
                    result.add(param);
                }
            }
        }

        // Extract parameters from JOIN clauses
        List<Join> joins = update.getJoins();
        if (joins != null && !joins.isEmpty()) {
            for (Join join : joins) {
                List<SqlParam> params = extractJoinParameters(join);
                if (params != null && !params.isEmpty()) {
                    result.addAll(params);
                }
            }
        }

        // Extract parameters from WHERE clause
        Expression where = update.getWhere();
        if (where != null) {
            List<SqlParam> sqlParams = ExpressionParse.getInstance().parseExpression(where);
            if (sqlParams != null && !sqlParams.isEmpty()) {
                result.addAll(sqlParams);
            }
        }

        return result;
    }

    /**
     * Extracts JDBC parameters from a JOIN clause.
     * This method analyzes the ON expressions of a JOIN to identify parameter placeholders.
     *
     * @param join the JOIN clause to analyze
     * @return a list of SqlParam objects found in the JOIN conditions
     */
    private List<SqlParam> extractJoinParameters(Join join) {
        List<SqlParam> sqlParams = new ArrayList<>();
        Collection<Expression> expressions = join.getOnExpressions();
        for (Expression expression : expressions) {
            List<SqlParam> params = ExpressionParse.getInstance().parseExpression(expression);
            if (params != null && !params.isEmpty()) {
                sqlParams.addAll(params);
            }
        }
        return sqlParams;
    }
}