package io.github.qwzhang01.sql.tool.jsqlparser;

import io.github.qwzhang01.sql.tool.model.SqlTable;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.update.Update;

import java.util.List;

/**
 * 获取脚本所有表信息
 *
 * @author avinzhang
 */
public class TablesStatementVisitor extends AbstractStatementVisitor {

    private final boolean deeply;
    private List<SqlTable> tables;

    public TablesStatementVisitor(boolean deeply) {
        this.deeply = deeply;
    }

    public List<SqlTable> getTables() {
        return tables;
    }

    @Override
    public void visit(Delete delete) {
        log.info("delete: " + delete.toString());
        this.tables = new DeleteParser(delete).table();
    }

    @Override
    public void visit(Update update) {
        log.info("update: " + update.toString());
        this.tables = new UpdateParser(update).table();
    }

    @Override
    public void visit(Insert insert) {
        log.info("insert: " + insert.toString());
        this.tables = new InsertParser(insert).table();
    }

    @Override
    public void visit(Select select) {
        log.info("select: " + select.toString());
        this.tables = new SelectParser(select).table(deeply);
    }
}
