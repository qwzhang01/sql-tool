package io.github.qwzhang01.sql.tool.jsqlparser.visitor;

import io.github.qwzhang01.sql.tool.model.SqlTable;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.StatementVisitorAdapter;
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
 * Visitor for merging JOIN and WHERE clauses into SQL statements
 *
 * @author avinzhang
 */
public class MergeStatementVisitor extends StatementVisitorAdapter<Void> {
    public List<SqlTable> tables;

    private String sql;

    private List<Join> joins;

    private Expression where;

    public void setTables(List<SqlTable> tables) {
        this.tables = tables;
    }

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
    public <S> Void visit(Select select, S content) {
        PlainSelect plainSelect = select.getPlainSelect();
        if (plainSelect == null) {
            return null;
        }
        if (joins != null && !joins.isEmpty()) {
            List<Join> oldJoins = plainSelect.getJoins();
            for (Join join : joins) {
                Collection<Expression> ons = join.getOnExpressions();
                for (Expression on : ons) {
                    on.accept(new CompleteTableVisitor(tables));
                }
                if (!hasJoin(oldJoins, join)) {
                    plainSelect.addJoins(join);
                }
            }
        }

        if (where != null) {
            where.accept(new CompleteTableVisitor(tables));

            Expression mainWhere = plainSelect.getWhere();
            if (mainWhere != null) {
                plainSelect.setWhere(new AndExpression(mainWhere, where));
            } else {
                plainSelect.setWhere(where);
            }
        }

        sql = select.toString();
        return null;
    }

    @Override
    public <S> Void visit(Delete delete, S content) {
        if (joins != null && !joins.isEmpty()) {
            List<Join> oldJoins = delete.getJoins();
            for (Join join : joins) {
                Collection<Expression> ons = join.getOnExpressions();
                for (Expression on : ons) {
                    on.accept(new CompleteTableVisitor(tables));
                }

                if (!hasJoin(oldJoins, join)) {
                    delete.addJoins(join);
                }
            }
        }

        if (where != null) {
            where.accept(new CompleteTableVisitor(tables));

            Expression mainWhere = delete.getWhere();
            if (mainWhere != null) {
                delete.setWhere(new AndExpression(mainWhere, where));
            } else {
                delete.setWhere(where);
            }
        }
        sql = delete.toString();

        return null;
    }

    @Override
    public <S> Void visit(Update update, S content) {
        if (joins != null && !joins.isEmpty()) {
            List<Join> oldJoins = update.getJoins();
            for (Join join : joins) {
                Collection<Expression> ons = join.getOnExpressions();
                for (Expression on : ons) {
                    on.accept(new CompleteTableVisitor(tables));
                }
                if (!hasJoin(oldJoins, join)) {
                    update.addJoins(join);
                }
            }
        }

        if (where != null) {
            where.accept(new CompleteTableVisitor(tables));

            Expression mainWhere = update.getWhere();
            if (mainWhere != null) {
                update.setWhere(new AndExpression(mainWhere, where));
            } else {
                update.setWhere(where);
            }
        }
        sql = update.toString();

        return null;
    }

    @Override
    public <S> Void visit(Insert insert, S content) {
        throw new UnsupportedOperationException("INSERT statements cannot be merged");
    }

    private boolean hasJoin(List<Join> oldJoins, Join join) {
        if (oldJoins == null || oldJoins.isEmpty()) {
            return false;
        }
        for (Join oldJoin : oldJoins) {
            FromItem oldFrom = oldJoin.getFromItem();
            if (oldFrom instanceof Table oldTable) {
                String name = oldTable.getName();
                if (join.getFromItem() instanceof Table table) {
                    String addTable = table.getName();
                    if (name.equals(addTable)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}