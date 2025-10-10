package io.github.qwzhang01.sql.tool.jsqlparser;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.update.Update;

import java.util.List;

/**
 * sql 拼接
 *
 * @author avinzhang
 */
public class GetStatementVisitorAdaptor extends StatementVisitorAdaptor {

    private List<Join> joins;

    private Expression where;

    public Expression getWhere() {
        return where;
    }

    public List<Join> getJoins() {
        return joins;
    }

    @Override
    public void visit(Delete delete) {
        this.joins = delete.getJoins();
        this.where = delete.getWhere();
    }

    @Override
    public void visit(Update update) {
        this.joins = update.getJoins();
        this.where = update.getWhere();
    }

    @Override
    public void visit(Insert insert) {
        throw new UnsupportedOperationException("插入脚本无法拼接");
    }

    @Override
    public void visit(Select select) {
        PlainSelect plainSelect = select.getPlainSelect();
        this.joins = plainSelect.getJoins();
        this.where = plainSelect.getWhere();
    }
}
