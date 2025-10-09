package io.github.qwzhang01.sql.tool.jsqlparser;

import net.sf.jsqlparser.statement.*;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.alter.AlterSession;
import net.sf.jsqlparser.statement.alter.AlterSystemStatement;
import net.sf.jsqlparser.statement.alter.RenameTableStatement;
import net.sf.jsqlparser.statement.alter.sequence.AlterSequence;
import net.sf.jsqlparser.statement.analyze.Analyze;
import net.sf.jsqlparser.statement.comment.Comment;
import net.sf.jsqlparser.statement.create.index.CreateIndex;
import net.sf.jsqlparser.statement.create.schema.CreateSchema;
import net.sf.jsqlparser.statement.create.sequence.CreateSequence;
import net.sf.jsqlparser.statement.create.synonym.CreateSynonym;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.view.AlterView;
import net.sf.jsqlparser.statement.create.view.CreateView;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.execute.Execute;
import net.sf.jsqlparser.statement.grant.Grant;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.merge.Merge;
import net.sf.jsqlparser.statement.refresh.RefreshMaterializedViewStatement;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.show.ShowIndexStatement;
import net.sf.jsqlparser.statement.show.ShowTablesStatement;
import net.sf.jsqlparser.statement.truncate.Truncate;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.upsert.Upsert;

import java.util.logging.Logger;

public abstract class AbstractStatementVisitor implements StatementVisitor {
    protected final Logger log = Logger.getLogger(this.getClass().getName());

    @Override
    public void visit(Analyze analyze) {
        log.info("analyze: " + analyze.toString());
    }

    @Override
    public void visit(SavepointStatement savepointStatement) {
        log.info("savepointStatement: " + savepointStatement.toString());
    }

    @Override
    public void visit(RollbackStatement rollbackStatement) {
        log.info("rollbackStatement: " + rollbackStatement.toString());
    }

    @Override
    public void visit(Comment comment) {
        log.info("comment: " + comment.toString());
    }

    @Override
    public void visit(Commit commit) {
        log.info("commit: " + commit.toString());
    }

    @Override
    abstract public void visit(Delete delete);

    @Override
    abstract public void visit(Update update);

    @Override
    abstract public void visit(Insert insert);

    @Override
    public void visit(Drop drop) {
        log.info("drop: " + drop.toString());
    }

    @Override
    public void visit(Truncate truncate) {
        log.info("truncate: " + truncate.toString());
    }

    @Override
    public void visit(CreateIndex createIndex) {
        log.info("createIndex: " + createIndex.toString());
    }

    @Override
    public void visit(CreateSchema aThis) {
        log.info("createSchema: " + aThis.toString());
    }

    @Override
    public void visit(CreateTable createTable) {
        log.info("createTable: " + createTable.toString());
    }

    @Override
    public void visit(CreateView createView) {
        log.info("createView: " + createView.toString());
    }

    @Override
    public void visit(AlterView alterView) {
        log.info("alterView: " + alterView.toString());
    }

    @Override
    public void visit(RefreshMaterializedViewStatement materializedView) {
        log.info("refreshMaterializedViewStatement: " + materializedView.toString());
    }

    @Override
    public void visit(Alter alter) {
        log.info("alter: " + alter.toString());
    }

    @Override
    public void visit(Statements stmts) {
        log.info("stmts: " + stmts.toString());
    }

    @Override
    public void visit(Execute execute) {
        log.info("execute: " + execute.toString());
    }

    @Override
    public void visit(SetStatement set) {
        log.info("set: " + set.toString());
    }

    @Override
    public void visit(ResetStatement reset) {
        log.info("reset: " + reset.toString());
    }

    @Override
    public void visit(ShowColumnsStatement set) {
        log.info("showColumnsStatement: " + set.toString());
    }

    @Override
    public void visit(ShowIndexStatement showIndex) {
        log.info("showIndex: " + showIndex.toString());
    }

    @Override
    public void visit(ShowTablesStatement showTables) {
        log.info("showTables: " + showTables.toString());
    }

    @Override
    public void visit(Merge merge) {
        log.info("merge: " + merge.toString());
    }

    @Override
    abstract public void visit(Select select);


    @Override
    public void visit(Upsert upsert) {
        log.info("upsert: " + upsert.toString());
    }

    @Override
    public void visit(UseStatement use) {
        log.info("use: " + use.toString());
    }

    @Override
    public void visit(Block block) {
        log.info("block: " + block.toString());
    }

    @Override
    public void visit(DescribeStatement describe) {
        log.info("describe: " + describe.toString());
    }

    @Override
    public void visit(ExplainStatement aThis) {
        log.info("explain: " + aThis.toString());
    }

    @Override
    public void visit(ShowStatement aThis) {
        log.info("show: " + aThis.toString());
    }

    @Override
    public void visit(DeclareStatement aThis) {
        log.info("declare: " + aThis.toString());
    }

    @Override
    public void visit(Grant grant) {
        log.info("grant: " + grant.toString());
    }

    @Override
    public void visit(CreateSequence createSequence) {
        log.info("createSequence: " + createSequence.toString());
    }

    @Override
    public void visit(AlterSequence alterSequence) {
        log.info("alterSequence: " + alterSequence.toString());
    }

    @Override
    public void visit(CreateFunctionalStatement createFunctionalStatement) {
        log.info("createFunctionalStatement: " + createFunctionalStatement.toString());
    }

    @Override
    public void visit(CreateSynonym createSynonym) {
        log.info("createSynonym: " + createSynonym.toString());
    }

    @Override
    public void visit(AlterSession alterSession) {
        log.info("alterSession: " + alterSession.toString());
    }

    @Override
    public void visit(IfElseStatement aThis) {
        log.info("ifElseStatement: " + aThis.toString());
    }

    @Override
    public void visit(RenameTableStatement renameTableStatement) {
        log.info("renameTableStatement: " + renameTableStatement.toString());
    }

    @Override
    public void visit(PurgeStatement purgeStatement) {
        log.info("purgeStatement: " + purgeStatement.toString());
    }

    @Override
    public void visit(AlterSystemStatement alterSystemStatement) {
        log.info("alterSystemStatement: " + alterSystemStatement.toString());
    }

    @Override
    public void visit(UnsupportedStatement unsupportedStatement) {
        log.info("unsupportedStatement: " + unsupportedStatement.toString());
    }
}
