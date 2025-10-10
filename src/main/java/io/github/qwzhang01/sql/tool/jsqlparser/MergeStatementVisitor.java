package io.github.qwzhang01.sql.tool.jsqlparser;

import io.github.qwzhang01.sql.tool.model.SqlTable;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.update.Update;

import java.util.Collection;
import java.util.List;

/**
 * sql 拼接
 *
 * @author avinzhang
 */
public class MergeStatementVisitor extends AbstractStatementVisitor {
    private String sql;

    private List<Join> joins;
    private Expression where;

    public void setJoins(List<Join> joins) {
        this.joins = joins;
    }

    public void setWhere(Expression where) {
        this.where = where;
    }

    public String getSql() {
        return sql;
    }

    @Override
    public void visit(Delete delete) {
        throw new UnsupportedOperationException("删除脚本无法拼接");
    }

    @Override
    public void visit(Update update) {
        throw new UnsupportedOperationException("更新脚本无法拼接");
    }

    @Override
    public void visit(Insert insert) {
        throw new UnsupportedOperationException("插入脚本无法拼接");
    }

    @Override
    public void visit(Select select) {
        PlainSelect plainSelect = select.getPlainSelect();
        // 替换 join 中的主表别名，合并 join
        List<SqlTable> table = new SelectParser(select).table(true);
        if (joins != null && !joins.isEmpty()) {
            for (Join join : joins) {
                String joinTableName = "";
                FromItem item = join.getFromItem();
                if (item instanceof Table joinTable) {
                    joinTableName = joinTable.getName();
                    Alias alias = joinTable.getAlias();
                    if (alias != null) {
                        joinTableName = alias.getName();
                    }
                }
                Collection<Expression> ons = join.getOnExpressions();
                for (Expression on : ons) {
                    on.accept(new JoinComplexExpressionVisitor(joinTableName, table));
                }
            }

            plainSelect.addJoins(joins);
        }

        // 替换 where 中主表别名 ，合并 where
        if (where != null) {
            where.accept(new WhereComplexExpressionVisitor(table));

            Expression mainWhere = plainSelect.getWhere();
            if (mainWhere != null) {
                // 合并 WHERE：原始 WHERE AND 额外 WHERE
                plainSelect.setWhere(new AndExpression(mainWhere, where));
            } else {
                // 无原始 WHERE，直接设置新 WHERE
                plainSelect.setWhere(where);
            }
        }

        sql = select.toString();
    }
}