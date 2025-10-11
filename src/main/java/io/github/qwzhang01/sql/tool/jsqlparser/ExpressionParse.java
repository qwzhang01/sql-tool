package io.github.qwzhang01.sql.tool.jsqlparser;

import io.github.qwzhang01.sql.tool.kit.ListKit;
import io.github.qwzhang01.sql.tool.model.SqlParam;
import io.github.qwzhang01.sql.tool.model.SqlTable;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.util.TablesNamesFinder;

import java.util.*;
import java.util.logging.Logger;

/**
 * Singleton utility class for parsing SQL expressions and extracting table and parameter information.
 * This class provides comprehensive analysis of SQL expressions, including recursive parsing
 * of complex nested expressions, subqueries, and various operator types.
 *
 * <p>Key capabilities:</p>
 * <ul>
 *   <li>Recursive parsing of complex SQL expressions</li>
 *   <li>JDBC parameter extraction with column association</li>
 *   <li>Table extraction from various expression types and subqueries</li>
 *   <li>Support for all major SQL operators and constructs</li>
 *   <li>Deep analysis of nested SELECT statements</li>
 * </ul>
 *
 * @author avinzhang
 */
public class ExpressionParse {
    /**
     * Logger instance for this parser
     */
    private final Logger log = Logger.getLogger(this.getClass().getName());

    /**
     * Private constructor for singleton pattern
     */
    private ExpressionParse() {
    }

    /**
     * Gets the singleton instance of ExpressionParse.
     *
     * @return the singleton ExpressionParse instance
     */
    public static ExpressionParse getInstance() {
        return ExpressionParse.SingletonHolder.INSTANCE;
    }

    public static List<String> extractTableNames(String sql) throws Exception {
        Statement statement = CCJSqlParserUtil.parse(sql);
        TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
        return tablesNamesFinder.getTableList(statement);
    }

    /**
     * Parses an SQL expression and extracts JDBC parameters.
     * This is a convenience method that calls the recursive parser with default indentation.
     *
     * @param expression the SQL expression to parse
     * @return a list of SqlParam objects representing JDBC parameters found in the expression
     */
    public List<SqlParam> parseExpression(Expression expression) {
        return parseExpression(expression, "  ");
    }

    /**
     * Recursively parses SQL expressions to identify JDBC parameters and their associated metadata.
     * This method handles various types of SQL expressions including logical operators,
     * comparison operators, subqueries, and complex nested structures.
     *
     * @param expression the SQL expression to parse
     * @param indent     the current indentation level for logging (used in recursive calls)
     * @return a list of SqlParam objects representing JDBC parameters found in the expression
     */
    private List<SqlParam> parseExpression(Expression expression, String indent) {
        if (expression == null) {
            return Collections.emptyList();
        }

        if (expression instanceof AndExpression andExpr) {
            List<SqlParam> left = parseExpression(andExpr.getLeftExpression(), indent + "  ");
            List<SqlParam> right = parseExpression(andExpr.getRightExpression(), indent + "  ");
            return ListKit.merge(left, right);
        } else if (expression instanceof OrExpression orExpr) {
            List<SqlParam> left = parseExpression(orExpr.getLeftExpression(), indent + "  ");
            List<SqlParam> right = parseExpression(orExpr.getRightExpression(), indent + "  ");
            return ListKit.merge(left, right);
        } else if (expression instanceof EqualsTo equalsTo) {
            return analyzeComparison(equalsTo.getLeftExpression(), equalsTo.getRightExpression(), indent + "  ");
        } else if (expression instanceof NotEqualsTo notEquals) {
            return analyzeComparison(notEquals.getLeftExpression(), notEquals.getRightExpression(), indent + "  ");
        } else if (expression instanceof GreaterThan gt) {
            return analyzeComparison(gt.getLeftExpression(), gt.getRightExpression(), indent + "  ");
        } else if (expression instanceof GreaterThanEquals gte) {
            return analyzeComparison(gte.getLeftExpression(), gte.getRightExpression(), indent + "  ");
        } else if (expression instanceof MinorThan lt) {
            return analyzeComparison(lt.getLeftExpression(), lt.getRightExpression(), indent + "  ");
        } else if (expression instanceof MinorThanEquals lte) {
            return analyzeComparison(lte.getLeftExpression(), lte.getRightExpression(), indent + "  ");
        } else if (expression instanceof LikeExpression like) {
            return analyzeComparison(like.getLeftExpression(), like.getRightExpression(), indent + "  ");
        } else if (expression instanceof InExpression inExpr) {
            return analyzeIn(inExpr, indent + "  ");
        } else if (expression instanceof Between between) {
            return analyzeBetween(between, indent + "  ");
        } else if (expression instanceof Parenthesis parenthesis) {
            return parseExpression(parenthesis.getExpression(), indent + "  ");
        } else {
            log.fine(indent + "其他表达式: " + expression);
        }
        return Collections.emptyList();
    }

    /**
     * Analyzes comparison expressions to extract JDBC parameters.
     * This method examines both sides of a comparison operation to identify
     * parameter placeholders and their associated column information.
     *
     * @param left   the left side of the comparison expression
     * @param right  the right side of the comparison expression
     * @param indent the current indentation level for logging
     * @return a list of SqlParam objects found in the comparison
     */
    private List<SqlParam> analyzeComparison(Expression left, Expression right, String indent) {
        if (right instanceof JdbcParameter) {
            if (left instanceof Column column) {
                SqlParam param = new SqlParam();
                if (column.getTable() != null) {
                    param.setTableAlias(column.getTable().getName());
                }
                param.setFieldName(column.getColumnName());
                return Collections.singletonList(param);
            }
        }
        if (left instanceof JdbcParameter) {
            if (right instanceof Column column) {
                SqlParam param = new SqlParam();
                param.setTableAlias(column.getTable().getName());
                param.setFieldName(column.getColumnName());
                return Collections.singletonList(param);
            }
        }
        if (left instanceof Select) {
            return parseSubSelect((Select) left, indent + "  ");
        }
        if (right instanceof Select) {
            return parseSubSelect((Select) right, indent + "  ");
        }
        return Collections.emptyList();
    }

    /**
     * Analyzes IN expressions to extract JDBC parameters.
     * This method handles both list-based IN expressions and subquery-based IN expressions.
     *
     * @param inExpr the IN expression to analyze
     * @param indent the current indentation level for logging
     * @return a list of SqlParam objects found in the IN expression
     */
    private List<SqlParam> analyzeIn(InExpression inExpr, String indent) {
        Column column = (Column) inExpr.getLeftExpression();
        Expression rightExpression = inExpr.getRightExpression();
        if (rightExpression instanceof ExpressionList exprList) {
            List<Expression> expressions = exprList.getExpressions();
            List<SqlParam> result = new ArrayList<>();
            for (Expression expr : expressions) {
                if (expr instanceof JdbcParameter) {
                    SqlParam param = new SqlParam();
                    param.setTableAlias(column.getTable().getName());
                    param.setFieldName(column.getColumnName());
                    result.add(param);
                } else {
                    log.fine(indent + "    值: " + expr);
                }
            }
        } else if (rightExpression instanceof Select subSelect) {
            return parseSubSelect(subSelect, indent + "    ");
        }
        return Collections.emptyList();
    }

    /**
     * Analyzes BETWEEN expressions to extract JDBC parameters.
     * This method examines both the start and end values of a BETWEEN clause
     * to identify parameter placeholders.
     *
     * @param between the BETWEEN expression to analyze
     * @param indent  the current indentation level for logging
     * @return a list of SqlParam objects found in the BETWEEN expression
     */
    private List<SqlParam> analyzeBetween(Between between, String indent) {
        List<SqlParam> list = new ArrayList<>();
        Column column = (Column) between.getLeftExpression();
        if (between.getBetweenExpressionStart() instanceof JdbcParameter) {
            SqlParam param = new SqlParam();
            param.setTableAlias(column.getTable().getName());
            param.setFieldName(column.getColumnName());
            list.add(param);
        } else {
            log.fine(indent + "  起始值: " + between.getBetweenExpressionStart());
        }
        if (between.getBetweenExpressionEnd() instanceof JdbcParameter) {
            SqlParam param = new SqlParam();
            param.setTableAlias(column.getTable().getName());
            param.setFieldName(column.getColumnName());
            list.add(param);
        } else {
            log.fine(indent + "  结束值: " + between.getBetweenExpressionEnd());
        }
        return list;
    }

    /**
     * Parses subqueries to extract JDBC parameters.
     * This method recursively analyzes SELECT statements within expressions
     * to find parameter placeholders in nested queries.
     *
     * @param subSelect the subquery SELECT statement to parse
     * @param indent    the current indentation level for logging
     * @return a list of SqlParam objects found in the subquery
     */
    private List<SqlParam> parseSubSelect(Select subSelect, String indent) {
        PlainSelect plainSelect = subSelect.getPlainSelect();
        if (plainSelect != null) {
            // 解析子查询的表
            FromItem fromItem = plainSelect.getFromItem();
            if (fromItem instanceof Table table) {
                log.fine(indent + "子查询表: " + table.getName());
                if (table.getAlias() != null) {
                    log.fine(indent + "子查询表别名: " + table.getAlias().getName());
                }
            }

            // 解析子查询的WHERE条件
            Expression where = plainSelect.getWhere();
            if (where != null) {
                return parseExpression(where, indent + "  ");
            }
        }
        return Collections.emptyList();
    }

    /**
     * Extracts table information from a PlainSelect statement.
     * This method analyzes all components of a SELECT statement to identify
     * table references, including FROM clauses, JOINs, and optionally subqueries.
     *
     * @param plainSelect the PlainSelect statement to analyze
     * @param deeply      if true, recursively analyzes subqueries; if false, only direct references
     * @return a list of SqlTable objects representing all tables found in the statement
     */
    public List<SqlTable> getTable(PlainSelect plainSelect, boolean deeply) {
        List<SqlTable> result = new ArrayList<>();

        // 提取 SELECT 项（列）
        List<SelectItem<?>> selectItems = plainSelect.getSelectItems();
        if (selectItems != null && !selectItems.isEmpty()) {
            for (SelectItem<?> item : selectItems) {
                log.fine("SELECT 项: " + item);
            }
        }

        // 提取 FROM 表
        FromItem fromItem = plainSelect.getFromItem();
        List<SqlTable> fromTable = fromItem(fromItem, deeply);
        if (!fromTable.isEmpty()) {
            result.addAll(fromTable);
        }

        Distinct distinct = plainSelect.getDistinct();
        if (distinct != null) {
            log.fine("DISTINCT: " + distinct);
        }

        // 用于SELECT INTO语句
        List<Table> intoTables = plainSelect.getIntoTables();
        if (intoTables != null && !intoTables.isEmpty()) {
            for (Table table : intoTables) {
                log.fine("INTO TABLE: " + table.getName());
            }
        }

        // 解析 join
        List<Join> joins = plainSelect.getJoins();
        if (joins != null && !joins.isEmpty()) {
            for (Join join : joins) {
                FromItem leftItem = join.getFromItem();
                List<SqlTable> joinTables = fromItem(leftItem, deeply);
                if (!joinTables.isEmpty()) {
                    result.addAll(joinTables);
                }
                // INNER JOIN, LEFT
                log.fine("JOIN 类型: " + join.getJoinHint());
                Collection<Expression> expressions = join.getOnExpressions();
                for (Expression expression : expressions) {
                    log.fine("ON 条件: " + expression);
                }
                FromItem rightItem = join.getRightItem();
                joinTables = fromItem(rightItem, deeply);
                if (!joinTables.isEmpty()) {
                    result.addAll(joinTables);
                }
            }
        }

        // 解析 WHERE
        Expression where = plainSelect.getWhere();
        if (where != null && deeply) {
            List<SqlTable> tables = whereTable(where);
            if (tables != null && !tables.isEmpty()) {
                result.addAll(tables);
            }
        }

        Set<String> mut = new HashSet<>();
        return result.stream().filter(s -> {
            if (mut.contains(s.getName() + s.getAlias())) {
                return false;
            }
            mut.add(s.getName() + s.getAlias());
            return true;
        }).toList();
    }

    /**
     * Extracts table references from WHERE clause expressions.
     * This method delegates to WhereTableParser for specialized WHERE clause analysis.
     *
     * @param where the WHERE expression to analyze
     * @return a list of SqlTable objects found in the WHERE clause
     */
    private List<SqlTable> whereTable(Expression where) {
        return new WhereTableParser().extractTable(where);
    }

    /**
     * Extracts table information from FROM item expressions.
     * This method handles various types of FROM items including direct table references,
     * subqueries, and lateral subqueries.
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
        if (fromItem instanceof Table table) {
            log.fine("表名: " + table.getName());
            result.add(new SqlTable(table.getName(), table.getAlias() != null ? table.getAlias().getName() : ""));
        }
        if (fromItem instanceof ParenthesedSelect subSelect) {
            Alias alias = subSelect.getAlias();
            if (alias != null) {
                String name = alias.getName().trim();
                if (name.toUpperCase().startsWith("AS ")) {
                    name = name.substring(2).trim();
                }
                result.add(new SqlTable("", name));
            }
            if (deeply) {
                PlainSelect plainSelect = subSelect.getPlainSelect();
                if (plainSelect != null) {
                    result.addAll(getTable(plainSelect, true));
                }
            }
        }
        if (fromItem instanceof LateralSubSelect subSelect) {
            if (deeply) {
                PlainSelect plainSelect = subSelect.getPlainSelect();
                if (plainSelect != null) {
                    result.addAll(getTable(plainSelect, true));
                }
            }
        }
        return result;
    }

    /**
     * Singleton holder class for thread-safe lazy initialization.
     * This pattern ensures that the singleton instance is created only when needed
     * and provides thread safety without synchronization overhead.
     */
    private static class SingletonHolder {
        /**
         * The singleton instance of ExpressionParse
         */
        private static final ExpressionParse INSTANCE = new ExpressionParse();
    }
}
