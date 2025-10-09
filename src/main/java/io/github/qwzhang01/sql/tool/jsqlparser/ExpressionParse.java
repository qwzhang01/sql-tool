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
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.*;

import java.util.*;
import java.util.logging.Logger;

public class ExpressionParse {
    private final Logger log = Logger.getLogger(this.getClass().getName());

    private ExpressionParse() {
    }

    public static ExpressionParse getInstance() {
        return ExpressionParse.SingletonHolder.INSTANCE;
    }

    public List<SqlParam> parseExpression(Expression expression) {
        return parseExpression(expression, "  ");
    }

    /**
     * 递归解析表达式，识别占位符和相关信息
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
            log.info(indent + "其他表达式: " + expression);
        }
        return Collections.emptyList();
    }

    /**
     * 分析比较表达式的左右两边
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
     * 分析比较表达式的左右两边
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
                    log.info(indent + "    值: " + expr);
                }
            }
        } else if (rightExpression instanceof Select subSelect) {
            return parseSubSelect(subSelect, indent + "    ");
        }
        return Collections.emptyList();
    }

    private List<SqlParam> analyzeBetween(Between between, String indent) {
        List<SqlParam> list = new ArrayList<>();
        Column column = (Column) between.getLeftExpression();
        if (between.getBetweenExpressionStart() instanceof JdbcParameter) {
            SqlParam param = new SqlParam();
            param.setTableAlias(column.getTable().getName());
            param.setFieldName(column.getColumnName());
            list.add(param);
        } else {
            log.info(indent + "  起始值: " + between.getBetweenExpressionStart());
        }
        if (between.getBetweenExpressionEnd() instanceof JdbcParameter) {
            SqlParam param = new SqlParam();
            param.setTableAlias(column.getTable().getName());
            param.setFieldName(column.getColumnName());
            list.add(param);
        } else {
            log.info(indent + "  结束值: " + between.getBetweenExpressionEnd());
        }
        return list;
    }

    /**
     * 解析子查询
     */
    private List<SqlParam> parseSubSelect(Select subSelect, String indent) {
        PlainSelect plainSelect = subSelect.getPlainSelect();
        if (plainSelect != null) {
            // 解析子查询的表
            FromItem fromItem = plainSelect.getFromItem();
            if (fromItem instanceof Table table) {
                log.info(indent + "子查询表: " + table.getName());
                if (table.getAlias() != null) {
                    log.info(indent + "子查询表别名: " + table.getAlias().getName());
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

    public List<SqlTable> getTable(PlainSelect plainSelect, boolean deeply) {
        List<SqlTable> result = new ArrayList<>();

        // 提取 SELECT 项（列）
        List<SelectItem<?>> selectItems = plainSelect.getSelectItems();
        if (selectItems != null && !selectItems.isEmpty()) {
            for (SelectItem<?> item : selectItems) {
                log.info("SELECT 项: " + item);
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
            log.info("DISTINCT: " + distinct);
        }

        // 用于SELECT INTO语句
        List<Table> intoTables = plainSelect.getIntoTables();
        if (intoTables != null && !intoTables.isEmpty()) {
            for (Table table : intoTables) {
                log.info("INTO TABLE: " + table.getName());
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
                log.info("JOIN 类型: " + join.getJoinHint());
                Collection<Expression> expressions = join.getOnExpressions();
                for (Expression expression : expressions) {
                    log.info("ON 条件: " + expression);
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
            if (mut.contains(s.getTableName() + s.getAlias())) {
                return false;
            }
            mut.add(s.getTableName() + s.getAlias());
            return true;
        }).toList();
    }

    private List<SqlTable> whereTable(Expression where) {
        return new WhereTableParser().extractTable(where);
    }

    private List<SqlTable> fromItem(FromItem fromItem, boolean deeply) {
        List<SqlTable> result = new ArrayList<>();
        if (fromItem == null) {
            return result;
        }
        if (fromItem instanceof Table table) {
            log.info("表名: " + table.getName());
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
                result.addAll(getTable(subSelect.getPlainSelect(), true));
            }

        }
        if (fromItem instanceof LateralSubSelect subSelect) {
            if (deeply) {
                result.addAll(getTable(subSelect.getPlainSelect(), true));
            }
        }
        return result;
    }

    private static class SingletonHolder {
        private static final ExpressionParse INSTANCE = new ExpressionParse();
    }
}
