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
import net.sf.jsqlparser.statement.delete.ParenthesedDelete;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.execute.Execute;
import net.sf.jsqlparser.statement.grant.Grant;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.insert.ParenthesedInsert;
import net.sf.jsqlparser.statement.merge.Merge;
import net.sf.jsqlparser.statement.refresh.RefreshMaterializedViewStatement;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.show.ShowIndexStatement;
import net.sf.jsqlparser.statement.show.ShowTablesStatement;
import net.sf.jsqlparser.statement.truncate.Truncate;
import net.sf.jsqlparser.statement.update.ParenthesedUpdate;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.upsert.Upsert;

import java.util.logging.Logger;

/**
 * StatementVisitorAdaptor
 *
 * @author avinzhang
 */
public abstract class StatementVisitorAdaptor<T> implements StatementVisitor<T> {
    protected final Logger log = Logger.getLogger(this.getClass().getName());


    @Override
    public <S> T visit(Analyze analyze, S context) {
        log.info("analyze: " + analyze.toString());
        return null;
    }

    @Override
    public <S> T visit(SavepointStatement savepointStatement, S context) {
        log.info("savepointStatement: " + savepointStatement.toString());
        return null;
    }

    @Override
    public <S> T visit(RollbackStatement rollbackStatement, S context) {
        log.info("rollbackStatement: " + rollbackStatement.toString());
        return null;
    }

    @Override
    public <S> T visit(Comment comment, S context) {
        log.info("comment: " + comment.toString());
        return null;
    }

    @Override
    public <S> T visit(Commit commit, S context) {
        log.info("commit: " + commit.toString());
        return null;
    }

    @Override
    public <S> T visit(Delete delete, S context) {
        log.info("delete: " + delete.toString());
        return null;
    }

    @Override
    public <S> T visit(Update update, S context) {
        log.info("update: " + update.toString());
        return null;
    }

    @Override
    public <S> T visit(Insert insert, S context) {
        log.info("insert: " + insert.toString());
        return null;
    }

    @Override
    public <S> T visit(Drop drop, S context) {
        log.info("drop: " + drop.toString());
        return null;
    }

    @Override
    public <S> T visit(Truncate truncate, S context) {
        log.info("truncate: " + truncate.toString());
        return null;
    }

    @Override
    public <S> T visit(CreateIndex createIndex, S context) {
        log.info("createIndex: " + createIndex.toString());
        return null;
    }

    @Override
    public <S> T visit(CreateSchema createSchema, S context) {
        log.info("createSchema: " + createSchema.toString());
        return null;
    }

    @Override
    public <S> T visit(CreateTable createTable, S context) {
        log.info("createTable: " + createTable.toString());
        return null;
    }

    @Override
    public <S> T visit(CreateView createView, S context) {
        log.info("createView: " + createView.toString());
        return null;
    }

    @Override
    public <S> T visit(AlterView alterView, S context) {
        log.info("alterView: " + alterView.toString());
        return null;
    }

    @Override
    public <S> T visit(RefreshMaterializedViewStatement materializedView, S context) {
        log.info("materializedView: " + materializedView.toString());
        return null;
    }

    @Override
    public <S> T visit(Alter alter, S context) {
        log.info("alter: " + alter.toString());
        return null;
    }

    @Override
    public <S> T visit(Statements statements, S context) {
        log.info("statements: " + statements.toString());
        return null;
    }

    @Override
    public <S> T visit(Execute execute, S context) {
        log.info("execute: " + execute.toString());
        return null;
    }

    @Override
    public <S> T visit(SetStatement set, S context) {
        log.info("set: " + set.toString());
        return null;
    }

    @Override
    public <S> T visit(ResetStatement reset, S context) {
        log.info("reset: " + reset.toString());
        return null;
    }

    @Override
    public <S> T visit(ShowColumnsStatement showColumns, S context) {
        log.info("showColumns: " + showColumns.toString());
        return null;
    }

    @Override
    public <S> T visit(ShowIndexStatement showIndex, S context) {
        log.info("showIndex: " + showIndex.toString());
        return null;
    }

    @Override
    public <S> T visit(ShowTablesStatement showTables, S context) {
        log.info("showTables: " + showTables.toString());
        return null;
    }

    @Override
    public <S> T visit(Merge merge, S context) {
        log.info("merge: " + merge.toString());
        return null;
    }

    @Override
    public <S> T visit(Select select, S context) {
        log.info("select: " + select.toString());
        return null;
    }

    @Override
    public <S> T visit(Upsert upsert, S context) {
        log.info("upsert: " + upsert.toString());
        return null;
    }

    @Override
    public <S> T visit(UseStatement use, S context) {
        log.info("use: " + use.toString());
        return null;
    }

    @Override
    public <S> T visit(Block block, S context) {
        log.info("block: " + block.toString());
        return null;
    }

    @Override
    public <S> T visit(DescribeStatement describe, S context) {
        log.info("describe: " + describe.toString());
        return null;
    }

    @Override
    public <S> T visit(ExplainStatement explainStatement, S context) {
        log.info("explainStatement: " + explainStatement.toString());
        return null;
    }

    @Override
    public <S> T visit(ShowStatement showStatement, S context) {
        log.info("showStatement: " + showStatement.toString());
        return null;
    }

    @Override
    public <S> T visit(DeclareStatement declareStatement, S context) {
        log.info("declareStatement: " + declareStatement.toString());
        return null;
    }

    @Override
    public <S> T visit(Grant grant, S context) {
        log.info("grant: " + grant.toString());
        return null;
    }

    @Override
    public <S> T visit(CreateSequence createSequence, S context) {
        log.info("createSequence: " + createSequence.toString());
        return null;
    }

    @Override
    public <S> T visit(AlterSequence alterSequence, S context) {
        log.info("alterSequence: " + alterSequence.toString());
        return null;
    }

    @Override
    public <S> T visit(CreateFunctionalStatement createFunctionalStatement, S context) {
        log.info("createFunctionalStatement: " + createFunctionalStatement.toString());
        return null;
    }

    @Override
    public <S> T visit(CreateSynonym createSynonym, S context) {
        log.info("createSynonym: " + createSynonym.toString());
        return null;
    }

    @Override
    public <S> T visit(AlterSession alterSession, S context) {
        log.info("alterSession: " + alterSession.toString());
        return null;
    }

    @Override
    public <S> T visit(IfElseStatement ifElseStatement, S context) {
        log.info("ifElseStatement: " + ifElseStatement.toString());
        return null;
    }

    @Override
    public <S> T visit(RenameTableStatement renameTableStatement, S context) {
        log.info("renameTableStatement: " + renameTableStatement.toString());
        return null;
    }

    @Override
    public <S> T visit(PurgeStatement purgeStatement, S context) {
        log.info("purgeStatement: " + purgeStatement.toString());
        return null;
    }

    @Override
    public <S> T visit(AlterSystemStatement alterSystemStatement, S context) {
        log.info("alterSystemStatement: " + alterSystemStatement.toString());
        return null;
    }

    @Override
    public <S> T visit(UnsupportedStatement unsupportedStatement, S context) {
        log.info("unsupportedStatement: " + unsupportedStatement.toString());
        return null;
    }

    @Override
    public <S> T visit(ParenthesedInsert parenthesedInsert, S context) {

        return null;
    }

    @Override
    public <S> T visit(ParenthesedUpdate parenthesedUpdate, S context) {
        return null;
    }

    @Override
    public <S> T visit(ParenthesedDelete parenthesedDelete, S context) {
        return null;
    }

    @Override
    abstract public void visit(Delete delete);

    @Override
    abstract public void visit(Update update);

    @Override
    abstract public void visit(Insert insert);

    @Override
    abstract public void visit(Select select);
}
