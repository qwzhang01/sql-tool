package io.github.qwzhang01.sql.tool.jsqlparser;

import io.github.qwzhang01.sql.tool.model.SqlParam;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.update.Update;

import java.util.List;

/**
 * 获取脚本所有代占位符的参数
 *
 * @author avinzhang
 */
public class ParamStatementVisitorAdaptor extends StatementVisitorAdaptor {

    private List<SqlParam> params;

    public List<SqlParam> getParams() {
        return params;
    }

    @Override
    public void visit(Delete delete) {
        log.info("delete: " + delete.toString());
        this.params = new DeleteParser(delete).param();
    }

    @Override
    public void visit(Update update) {
        log.info("update: " + update.toString());
        this.params = new UpdateParser(update).param();
    }

    @Override
    public void visit(Insert insert) {
        log.info("insert: " + insert.toString());
        this.params = new InsertParser(insert).param();
    }

    @Override
    public void visit(Select select) {
        log.info("select: " + select.toString());
        this.params = new SelectParser(select).param();
    }
}
