package io.github.qwzhang01.sql.tool.jsqlparser;

import io.github.qwzhang01.sql.tool.model.SqlTable;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.*;

import java.util.ArrayList;
import java.util.List;

public class WhereTableParser {
    private List<SqlTable> tables = null;

    public List<SqlTable> extractTable(Expression where) {
        tables = new ArrayList<>();
        where.accept(new WhereExpressionVisitor());
        return tables;
    }

    // 自定义 ExpressionVisitor，用于提取表名
    private class WhereExpressionVisitor extends ExpressionVisitorAdapter {
        @Override
        public void visit(Column column) {
            // 从 Column 中提取表名
            Table table = column.getTable();
            if (table != null && table.getName() != null) {
                tables.add(new SqlTable(table.getName(), table.getAlias() != null ? table.getAlias().getName() : null));
            }
        }

        @Override
        public void visit(Select subSelect) {
            PlainSelect plainSelect = subSelect.getPlainSelect();
            // 处理子查询中的表名
            // 解析 FROM 子句
            FromItem fromItem = plainSelect.getFromItem();
            if (fromItem != null) {
                fromItem.accept(new TableNameFromItemVisitor());
            }
            // 解析 JOIN 子句
            if (plainSelect.getJoins() != null) {
                plainSelect.getJoins().forEach(join -> {
                    join.getRightItem().accept(new TableNameFromItemVisitor());
                });
            }
            // 递归解析子查询的 WHERE 子句
            Expression where = plainSelect.getWhere();
            if (where != null) {
                where.accept(this);
            }
        }

        @Override
        public void visit(InExpression inExpression) {
            // 处理 IN 子查询
            if (inExpression.getRightExpression() instanceof Select) {
                inExpression.getRightExpression().accept(this);
            }
            // 继续处理左侧表达式
            if (inExpression.getLeftExpression() != null) {
                inExpression.getLeftExpression().accept(this);
            }
        }
    }

    // 自定义 FromItemVisitor，用于提取 FROM 和 JOIN 中的表名
    private class TableNameFromItemVisitor extends FromItemVisitorAdapter {
        @Override
        public void visit(Table table) {
            // 直接提取表名
            if (table.getName() != null) {
                tables.add(new SqlTable(table.getName(), table.getAlias() != null ? table.getAlias().getName() : null));
            }
        }

        @Override
        public void visit(ParenthesedSelect subSelect) {
            PlainSelect plainSelect = subSelect.getPlainSelect();
            FromItem fromItem = plainSelect.getFromItem();
            if (fromItem != null) {
                fromItem.accept(this);
            }
            if (plainSelect.getJoins() != null) {
                plainSelect.getJoins().forEach(join -> {
                    join.getRightItem().accept(this);
                });
            }
            // 递归处理子查询的 WHERE
            Expression where = plainSelect.getWhere();
            if (where != null) {
                where.accept(new WhereExpressionVisitor());
            }
        }
    }
}