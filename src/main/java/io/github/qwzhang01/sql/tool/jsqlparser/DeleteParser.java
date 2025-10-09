package io.github.qwzhang01.sql.tool.jsqlparser;

import io.github.qwzhang01.sql.tool.model.SqlParam;
import io.github.qwzhang01.sql.tool.model.SqlTable;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.LateralSubSelect;
import net.sf.jsqlparser.statement.select.ParenthesedSelect;

import java.util.*;
import java.util.logging.Logger;

/**
 * @author avinzhang
 */
public class DeleteParser {
    private static final Logger logger = Logger.getLogger(DeleteParser.class.getName());
    private final Delete delete;

    public DeleteParser(Delete delete) {
        this.delete = delete;
    }

    public List<SqlTable> table() {
        return getTable(delete, true);
    }

    private List<SqlTable> getTable(Delete delete, boolean deeply) {
        List<SqlTable> result = new ArrayList<>();
        Table table = delete.getTable();
        result.add(new SqlTable(table.getName(), table.getAlias() != null ? table.getAlias().getName() : ""));

        List<Join> joins = delete.getJoins();
        if (joins != null && !joins.isEmpty()) {
            for (Join join : joins) {
                FromItem leftItem = join.getFromItem();
                List<SqlTable> joinTables = fromItem(leftItem, deeply);
                if (!joinTables.isEmpty()) {
                    result.addAll(joinTables);
                }
                // INNER JOIN, LEFT
                logger.info("JOIN 类型: " + join.getJoinHint());
                Collection<Expression> expressions = join.getOnExpressions();
                for (Expression expression : expressions) {
                    logger.info("ON 条件: " + expression);
                }
                FromItem rightItem = join.getRightItem();
                joinTables = fromItem(rightItem, deeply);
                if (!joinTables.isEmpty()) {
                    result.addAll(joinTables);
                }
            }
        }

        // 解析 WHERE
        Expression where = delete.getWhere();
        if (where != null && deeply) {
            List<SqlTable> tables = new WhereTableParser().extractTable(where);
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

    private List<SqlTable> fromItem(FromItem fromItem, boolean deeply) {
        List<SqlTable> result = new ArrayList<>();
        if (fromItem == null) {
            return result;
        }
        if (fromItem instanceof Table table) {
            logger.info("表名: " + table.getName());
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
                result.addAll(ExpressionParse.getInstance().getTable(subSelect.getPlainSelect(), true));
            }

        }
        if (fromItem instanceof LateralSubSelect subSelect) {
            if (deeply) {
                result.addAll(ExpressionParse.getInstance().getTable(subSelect.getPlainSelect(), true));
            }
        }
        return result;
    }

    public List<SqlParam> param() {
        List<SqlParam> result = new ArrayList<>();
        List<Join> joins = delete.getJoins();
        if (joins != null && !joins.isEmpty()) {
            for (Join join : joins) {
                List<SqlParam> params = join(join);
                if (params != null && !params.isEmpty()) {
                    result.addAll(params);
                }
            }
        }
        Expression where = delete.getWhere();
        if (where != null) {
            List<SqlParam> sqlParams = ExpressionParse.getInstance().parseExpression(where);
            if (sqlParams != null && !sqlParams.isEmpty()) {
                result.addAll(sqlParams);
            }
        }

        return result;
    }

    private List<SqlParam> join(Join join) {
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