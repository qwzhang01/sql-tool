package io.github.qwzhang01.sql.tool.jsqlparser;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.update.Update;

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
// 替换 join 和 where 中的表别名
        // 合并join 和where
    }
}
