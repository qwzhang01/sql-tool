package io.github.qwzhang01.sql.tool.jsqlparser;

import io.github.qwzhang01.sql.tool.model.SqlParam;
import io.github.qwzhang01.sql.tool.model.SqlTable;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

/**
 * Parser for analyzing SELECT statements and extracting table and parameter information.
 * This class provides methods to parse SELECT statements and extract various components
 * such as tables, parameters, JOIN clauses, and WHERE conditions.
 * 
 * <p>Key features:</p>
 * <ul>
 *   <li>Extracts table information with optional deep parsing of subqueries</li>
 *   <li>Identifies JDBC parameters in various SELECT statement components</li>
 *   <li>Handles complex SELECT structures including JOINs, subqueries, and nested expressions</li>
 *   <li>Provides detailed logging for debugging and analysis</li>
 * </ul>
 *
 * @author avinzhang
 */
public class SelectParser {
    /**
     * Logger instance for this parser
     */
    private static final Logger logger = Logger.getLogger(SelectParser.class.getName());

    /**
     * The SELECT statement to be parsed
     */
    private final Select select;

    /**
     * Constructs a new SelectParser for the given SELECT statement.
     *
     * @param select the SELECT statement to parse
     */
    public SelectParser(Select select) {
        this.select = select;
    }

    /**
     * Extracts all table information from the SELECT statement.
     * 
     * @param deeply if true, performs deep parsing including tables from subqueries;
     *               if false, extracts only direct table references
     * @return a list of SqlTable objects containing table names and aliases
     */
    public List<SqlTable> table(boolean deeply) {
        PlainSelect plainSelect = select.getPlainSelect();
        if (plainSelect != null) {
            return ExpressionParse.getInstance().getTable(plainSelect, deeply);
        }
        return new ArrayList<>();
    }

    /**
     * Extracts all JDBC parameters from the SELECT statement.
     * This method analyzes various components of the SELECT statement including
     * JOIN conditions, WHERE clauses, GROUP BY, HAVING, ORDER BY, and LIMIT clauses.
     *
     * @return a list of SqlParam objects containing parameter metadata
     */
    public List<SqlParam> param() {
        List<SqlParam> result = new ArrayList<>();
        PlainSelect plainSelect = select.getPlainSelect();
        if (plainSelect == null) {
            return result;
        }

        // Extract parameters from JOIN clauses
        List<Join> joins = plainSelect.getJoins();
        if (joins != null && !joins.isEmpty()) {
            for (Join join : joins) {
                List<SqlParam> params = extractJoinParameters(join);
                if (params != null && !params.isEmpty()) {
                    result.addAll(params);
                }
            }
        }
        
        // Extract parameters from WHERE clause
        Expression where = plainSelect.getWhere();
        if (where != null) {
            List<SqlParam> sqlParams = ExpressionParse.getInstance().parseExpression(where);
            if (sqlParams != null && !sqlParams.isEmpty()) {
                result.addAll(sqlParams);
            }
        }
        
        // Log other clauses for potential future parameter extraction
        GroupByElement groupBy = plainSelect.getGroupBy();
        if (groupBy != null) {
            logger.info("GROUP BY clause found: " + groupBy);
        }
        
        Expression having = plainSelect.getHaving();
        if (having != null) {
            logger.info("HAVING clause found: " + having);
        }
        
        List<OrderByElement> orderByElements = plainSelect.getOrderByElements();
        if (orderByElements != null && !orderByElements.isEmpty()) {
            for (OrderByElement orderByElement : orderByElements) {
                logger.info("ORDER BY element found: " + orderByElement);
            }
        }
        
        Limit limit = plainSelect.getLimit();
        if (limit != null) {
            logger.info("LIMIT clause found: " + limit);
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