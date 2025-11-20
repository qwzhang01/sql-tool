package io.github.qwzhang01.sql.tool.jsqlparser.visitor;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.StatementVisitorAdapter;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.update.Update;

import java.util.List;

/**
 * Visitor for extracting JOIN and WHERE clauses from SQL statements
 *
 * @author avinzhang
 */
public class SplitStatementVisitor extends StatementVisitorAdapter<Void> {

    private List<Join> joins;
    private Expression where;

    public Expression getWhere() {
        return where;
    }

    public List<Join> getJoins() {
        return joins;
    }

    @Override
    public <S> Void visit(Delete delete, S content) {
        this.joins = delete.getJoins();
        this.where = delete.getWhere();
        return null;
    }

    @Override
    public <S> Void visit(Update update, S content) {
        this.joins = update.getJoins();
        this.where = update.getWhere();
        return null;
    }

    @Override
    public <S> Void visit(Select select, S content) {
        PlainSelect plainSelect = select.getPlainSelect();
        if (plainSelect != null) {
            this.joins = plainSelect.getJoins();
            this.where = plainSelect.getWhere();
        }
        return null;
    }
}
