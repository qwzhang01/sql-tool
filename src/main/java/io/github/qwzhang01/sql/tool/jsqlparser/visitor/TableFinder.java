package io.github.qwzhang01.sql.tool.jsqlparser.visitor;


import io.github.qwzhang01.sql.tool.model.SqlTable;
import io.github.qwzhang01.sql.tool.wrapper.SqlParser;
import io.github.qwzhang01.sql.tool.wrapper.TableParser;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.arithmetic.*;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.conditional.XorExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
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
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.statement.show.ShowIndexStatement;
import net.sf.jsqlparser.statement.show.ShowTablesStatement;
import net.sf.jsqlparser.statement.truncate.Truncate;
import net.sf.jsqlparser.statement.update.ParenthesedUpdate;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.update.UpdateSet;
import net.sf.jsqlparser.statement.upsert.Upsert;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Visitor class for finding all tables used within SQL statements.
 * This visitor traverses the entire SQL AST to discover all table references,
 * including those in subqueries, JOINs, and WITH clauses. It distinguishes between
 * actual tables and other sources (like aliases and subqueries).
 *
 * <p>Override the extractTableName method to modify how table names are extracted
 * (e.g., to exclude schema names).</p>
 *
 * @author Avin Zhang
 * @since 1.0.0
 */
public class TableFinder<Void> implements SelectVisitor<Void>, FromItemVisitor<Void>, ExpressionVisitor<Void>, SelectItemVisitor<Void>, StatementVisitor<Void> {

    /**
     * The current parent table context (used for nested queries)
     */
    private SqlTable parentTable;

    /**
     * Set of discovered tables
     */
    private Set<SqlTable> tables;
    
    /**
     * Flag indicating whether column processing is allowed (for expression parsing)
     */
    private boolean allowColumnProcessing = false;

    /**
     * Set of other named items (aliases, subquery names, etc.)
     */
    private Set<SqlTable> otherItemNames;

    /**
     * Finds all actual tables in a SQL statement (excludes aliases and subquery names)
     *
     * @param sqlStr the SQL statement to parse
     * @return set of SqlTable objects representing actual tables
     */
    public static Set<SqlTable> findTables(String sqlStr) {
        TableFinder<?> tablesNamesFinder = new TableFinder<>();
        return tablesNamesFinder.getTables(SqlParser.getInstance().parse(sqlStr));
    }

    /**
     * Finds all tables and other sources (including aliases and subquery names)
     *
     * @param sqlStr the SQL statement to parse
     * @return set of SqlTable objects representing all table sources
     */
    public static Set<SqlTable> findTablesOrOtherSources(String sqlStr) {
        TableFinder<?> tablesNamesFinder = new TableFinder<>();
        return tablesNamesFinder.getTablesOrOtherSources(SqlParser.getInstance().parse(sqlStr));
    }

    /**
     * Finds all tables referenced in a SQL expression
     *
     * @param exprStr the SQL expression to parse
     * @return set of SqlTable objects found in the expression
     */
    public static Set<SqlTable> findTablesInExpression(String exprStr) {
        TableFinder<?> tablesNamesFinder = new TableFinder<>();
        return tablesNamesFinder.getTables(SqlParser.getInstance().parseExpression(exprStr));
    }

    /**
     * Throws an exception for unsupported statement types
     *
     * @param type the unsupported type
     * @param <T>  type parameter
     */
    private static <T> void throwUnsupported(T type) {
        throw new UnsupportedOperationException(String.format("Finding tables from %s is not supported", type.getClass().getSimpleName()));
    }

    public Set<SqlTable> getTables(Statement statement) {
        init(false);
        statement.accept(this, null);
        // @todo: assess this carefully, maybe we want to remove more specifically
        // only Aliases on WithItems, Parenthesed Selects and Lateral Selects
        otherItemNames.forEach(tables::remove);

        return tables;
    }

    public Set<SqlTable> getTablesOrOtherSources(Statement statement) {
        init(false);
        statement.accept(this, null);

        HashSet<SqlTable> tablesOrOtherSources = new HashSet<>(tables);
        tablesOrOtherSources.addAll(otherItemNames);

        return tablesOrOtherSources;
    }

    @Override
    public <S> Void visit(Select select, S context) {
        List<WithItem<?>> withItemsList = select.getWithItemsList();
        if (withItemsList != null && !withItemsList.isEmpty()) {
            for (WithItem<?> withItem : withItemsList) {
                withItem.accept((SelectVisitor<?>) this, context);
            }
        }
        select.accept((SelectVisitor<?>) this, context);
        return null;
    }

    @Override
    public void visit(Select select) {
        StatementVisitor.super.visit(select);
    }

    @Override
    public <S> Void visit(TranscodingFunction transcodingFunction, S context) {
        transcodingFunction.getExpression().accept(this, context);
        return null;
    }

    @Override
    public <S> Void visit(TrimFunction trimFunction, S context) {
        if (trimFunction.getExpression() != null) {
            trimFunction.getExpression().accept(this, context);
        }
        if (trimFunction.getFromExpression() != null) {
            trimFunction.getFromExpression().accept(this, context);
        }
        return null;
    }

    @Override
    public <S> Void visit(RangeExpression rangeExpression, S context) {
        rangeExpression.getStartExpression().accept(this, context);
        rangeExpression.getEndExpression().accept(this, context);
        return null;
    }

    public Set<SqlTable> getTables(Expression expr) {
        init(true);
        expr.accept(this, null);
        return tables;
    }

    @Override
    public <S> Void visit(WithItem<?> withItem, S context) {
        otherItemNames.add(TableParser.getInstance().parse(withItem.getAlias()));
        withItem.getSelect().accept((SelectVisitor<?>) this, context);
        return null;
    }

    @Override
    public void visit(WithItem<?> withItem) {
        SelectVisitor.super.visit(withItem);
    }

    @Override
    public <S> Void visit(ParenthesedSelect select, S context) {
        if (select.getAlias() != null) {
            SqlTable defineTable = TableParser.getInstance().parse(select.getAlias());
            otherItemNames.add(defineTable);
            parentTable = defineTable;
        }
        List<WithItem<?>> withItemsList = select.getWithItemsList();
        if (withItemsList != null && !withItemsList.isEmpty()) {
            for (WithItem<?> withItem : withItemsList) {
                withItem.accept((SelectVisitor<?>) this, context);
            }
        }
        select.getSelect().accept((SelectVisitor<?>) this, context);
        parentTable = null;
        return null;
    }

    @Override
    public void visit(ParenthesedSelect parenthesedSelect) {
        SelectVisitor.super.visit(parenthesedSelect);
    }

    @Override
    public <S> Void visit(PlainSelect plainSelect, S context) {
        List<WithItem<?>> withItemsList = plainSelect.getWithItemsList();
        if (withItemsList != null && !withItemsList.isEmpty()) {
            for (WithItem<?> withItem : withItemsList) {
                withItem.accept((SelectVisitor<?>) this, context);
            }
        }
        if (plainSelect.getSelectItems() != null) {
            for (SelectItem<?> item : plainSelect.getSelectItems()) {
                item.accept(this, context);
            }
        }

        if (plainSelect.getFromItem() != null) {
            plainSelect.getFromItem().accept(this, context);
        }

        visitJoins(plainSelect.getJoins(), context);
        if (plainSelect.getWhere() != null) {
            plainSelect.getWhere().accept(this, context);
        }

        if (plainSelect.getHaving() != null) {
            plainSelect.getHaving().accept(this, context);
        }

        if (plainSelect.getOracleHierarchical() != null) {
            plainSelect.getOracleHierarchical().accept(this, context);
        }
        return null;
    }

    @Override
    public void visit(PlainSelect plainSelect) {
        SelectVisitor.super.visit(plainSelect);
    }

    /**
     * Extracts table name from a JSQLParser Table object.
     * Override this method to customize table name extraction (e.g., to include/exclude schema).
     *
     * @param table the JSQLParser Table object
     * @return SqlTable object representing the extracted table information
     */
    protected SqlTable extractTableName(Table table) {
        return TableParser.getInstance().parse(table);
    }

    @Override
    public <S> Void visit(Table table, S context) {
        SqlTable tableWholeName = extractTableName(table);
        if (!otherItemNames.contains(tableWholeName)) {
            if (parentTable != null) {
                Set<SqlTable> children = parentTable.getChildren();
                if (children == null || children.isEmpty()) {
                    children = new HashSet<>();
                }
                children.add(tableWholeName);
                parentTable.setChildren(children);
            } else {
                tables.add(tableWholeName);
            }
        }
        return null;
    }

    @Override
    public void visit(Table tableName) {
        this.visit(tableName, null);
    }

    @Override
    public <S> Void visit(Addition addition, S context) {
        visitBinaryExpression(addition);
        return null;
    }

    @Override
    public <S> Void visit(AndExpression andExpression, S context) {
        visitBinaryExpression(andExpression);
        return null;
    }

    @Override
    public <S> Void visit(Between between, S context) {
        between.getLeftExpression().accept(this, context);
        between.getBetweenExpressionStart().accept(this, context);
        between.getBetweenExpressionEnd().accept(this, context);
        return null;
    }

    @Override
    public <S> Void visit(OverlapsCondition overlapsCondition, S context) {
        overlapsCondition.getLeft().accept(this, context);
        overlapsCondition.getRight().accept(this, context);
        return null;
    }

    @Override
    public <S> Void visit(Column tableColumn, S context) {
        if (allowColumnProcessing && tableColumn.getTable() != null && tableColumn.getTable().getName() != null) {
            visit(tableColumn.getTable(), context);
        }
        return null;
    }

    @Override
    public <S> Void visit(Division division, S context) {
        visitBinaryExpression(division);
        return null;
    }

    @Override
    public <S> Void visit(IntegerDivision division, S context) {
        visitBinaryExpression(division);
        return null;
    }

    @Override
    public <S> Void visit(DoubleValue doubleValue, S context) {

        return null;
    }

    @Override
    public <S> Void visit(EqualsTo equalsTo, S context) {
        visitBinaryExpression(equalsTo);
        return null;
    }

    @Override
    public <S> Void visit(Function function, S context) {
        ExpressionList<?> exprList = function.getParameters();
        if (exprList != null) {
            visit(exprList, context);
        }
        return null;
    }

    @Override
    public <S> Void visit(GreaterThan greaterThan, S context) {
        visitBinaryExpression(greaterThan);
        return null;
    }

    @Override
    public <S> Void visit(GreaterThanEquals greaterThanEquals, S context) {
        visitBinaryExpression(greaterThanEquals);
        return null;
    }

    @Override
    public <S> Void visit(InExpression inExpression, S context) {
        inExpression.getLeftExpression().accept(this, context);
        inExpression.getRightExpression().accept(this, context);
        return null;
    }

    @Override
    public <S> Void visit(IncludesExpression includesExpression, S context) {
        includesExpression.getLeftExpression().accept(this, context);
        includesExpression.getRightExpression().accept(this, context);
        return null;
    }

    @Override
    public <S> Void visit(ExcludesExpression excludesExpression, S context) {
        excludesExpression.getLeftExpression().accept(this, context);
        excludesExpression.getRightExpression().accept(this, context);
        return null;
    }

    @Override
    public <S> Void visit(FullTextSearch fullTextSearch, S context) {

        return null;
    }

    @Override
    public <S> Void visit(SignedExpression signedExpression, S context) {
        signedExpression.getExpression().accept(this, context);
        return null;
    }

    @Override
    public <S> Void visit(IsNullExpression isNullExpression, S context) {

        return null;
    }

    @Override
    public <S> Void visit(IsBooleanExpression isBooleanExpression, S context) {

        return null;
    }

    @Override
    public <S> Void visit(JdbcParameter jdbcParameter, S context) {
        return null;
    }

    @Override
    public <S> Void visit(LikeExpression likeExpression, S context) {
        visitBinaryExpression(likeExpression);
        return null;
    }

    @Override
    public <S> Void visit(ExistsExpression existsExpression, S context) {
        existsExpression.getRightExpression().accept(this, context);
        return null;
    }

    @Override
    public <S> Void visit(MemberOfExpression memberOfExpression, S context) {
        memberOfExpression.getLeftExpression().accept(this, context);
        memberOfExpression.getRightExpression().accept(this, context);
        return null;
    }

    @Override
    public <S> Void visit(LongValue longValue, S context) {

        return null;
    }

    @Override
    public <S> Void visit(MinorThan minorThan, S context) {
        visitBinaryExpression(minorThan);
        return null;
    }

    @Override
    public <S> Void visit(MinorThanEquals minorThanEquals, S context) {
        visitBinaryExpression(minorThanEquals);
        return null;
    }

    @Override
    public <S> Void visit(Multiplication multiplication, S context) {
        visitBinaryExpression(multiplication);
        return null;
    }

    @Override
    public <S> Void visit(NotEqualsTo notEqualsTo, S context) {
        visitBinaryExpression(notEqualsTo);
        return null;
    }

    @Override
    public <S> Void visit(DoubleAnd doubleAnd, S context) {
        visitBinaryExpression(doubleAnd);
        return null;
    }

    @Override
    public <S> Void visit(Contains contains, S context) {
        visitBinaryExpression(contains);
        return null;
    }

    @Override
    public <S> Void visit(ContainedBy containedBy, S context) {
        visitBinaryExpression(containedBy);
        return null;
    }

    @Override
    public <S> Void visit(NullValue nullValue, S context) {

        return null;
    }

    @Override
    public <S> Void visit(OrExpression orExpression, S context) {
        visitBinaryExpression(orExpression);
        return null;
    }

    @Override
    public <S> Void visit(XorExpression xorExpression, S context) {
        visitBinaryExpression(xorExpression);
        return null;
    }

    @Override
    public <S> Void visit(StringValue stringValue, S context) {

        return null;
    }

    @Override
    public <S> Void visit(BooleanValue booleanValue, S context) {

        return null;
    }

    @Override
    public <S> Void visit(Subtraction subtraction, S context) {
        visitBinaryExpression(subtraction);
        return null;
    }

    @Override
    public <S> Void visit(NotExpression notExpr, S context) {
        notExpr.getExpression().accept(this, context);
        return null;
    }

    @Override
    public <S> Void visit(BitwiseRightShift expr, S context) {
        visitBinaryExpression(expr);
        return null;
    }

    @Override
    public <S> Void visit(BitwiseLeftShift expr, S context) {
        visitBinaryExpression(expr);
        return null;
    }

    private void visitBinaryExpression(BinaryExpression binaryExpression) {
        binaryExpression.getLeftExpression().accept(this, null);
        binaryExpression.getRightExpression().accept(this, null);
    }

    @Override
    public <S> Void visit(ExpressionList<?> expressionList, S context) {
        for (Expression expression : expressionList) {
            expression.accept(this, context);
        }
        return null;
    }

    @Override
    public <S> Void visit(DateValue dateValue, S context) {

        return null;
    }

    @Override
    public <S> Void visit(TimestampValue timestampValue, S context) {

        return null;
    }

    @Override
    public <S> Void visit(TimeValue timeValue, S context) {

        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.
     * CaseExpression)
     */
    @Override
    public <S> Void visit(CaseExpression caseExpression, S context) {
        if (caseExpression.getSwitchExpression() != null) {
            caseExpression.getSwitchExpression().accept(this, context);
        }
        if (caseExpression.getWhenClauses() != null) {
            for (WhenClause when : caseExpression.getWhenClauses()) {
                when.accept(this, context);
            }
        }
        if (caseExpression.getElseExpression() != null) {
            caseExpression.getElseExpression().accept(this, context);
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.WhenClause)
     */
    @Override
    public <S> Void visit(WhenClause whenClause, S context) {
        if (whenClause.getWhenExpression() != null) {
            whenClause.getWhenExpression().accept(this, context);
        }
        if (whenClause.getThenExpression() != null) {
            whenClause.getThenExpression().accept(this, context);
        }
        return null;
    }

    @Override
    public <S> Void visit(AnyComparisonExpression anyComparisonExpression, S context) {
        anyComparisonExpression.getSelect().accept((ExpressionVisitor<?>) this, context);
        return null;
    }

    @Override
    public <S> Void visit(Concat concat, S context) {
        visitBinaryExpression(concat);
        return null;
    }

    @Override
    public <S> Void visit(Matches matches, S context) {
        visitBinaryExpression(matches);
        return null;
    }

    @Override
    public <S> Void visit(BitwiseAnd bitwiseAnd, S context) {
        visitBinaryExpression(bitwiseAnd);
        return null;
    }

    @Override
    public <S> Void visit(BitwiseOr bitwiseOr, S context) {
        visitBinaryExpression(bitwiseOr);
        return null;
    }

    @Override
    public <S> Void visit(BitwiseXor bitwiseXor, S context) {
        visitBinaryExpression(bitwiseXor);
        return null;
    }

    @Override
    public <S> Void visit(CastExpression cast, S context) {
        cast.getLeftExpression().accept(this, context);
        return null;
    }

    @Override
    public <S> Void visit(Modulo modulo, S context) {
        visitBinaryExpression(modulo);
        return null;
    }

    @Override
    public <S> Void visit(AnalyticExpression analytic, S context) {
        if (analytic.getExpression() != null) {
            analytic.getExpression().accept(this, context);
        }
        if (analytic.getDefaultValue() != null) {
            analytic.getDefaultValue().accept(this, context);
        }
        if (analytic.getOffset() != null) {
            analytic.getOffset().accept(this, context);
        }
        if (analytic.getKeep() != null) {
            analytic.getKeep().accept(this, context);
        }
        if (analytic.getFuncOrderBy() != null) {
            for (OrderByElement element : analytic.getOrderByElements()) {
                element.getExpression().accept(this, context);
            }
        }

        if (analytic.getWindowElement() != null) {
            analytic.getWindowElement().getRange().getStart().getExpression().accept(this, context);
            analytic.getWindowElement().getRange().getEnd().getExpression().accept(this, context);
            analytic.getWindowElement().getOffset().getExpression().accept(this, context);
        }
        return null;
    }

    @Override
    public <S> Void visit(SetOperationList list, S context) {
        List<WithItem<?>> withItemsList = list.getWithItemsList();
        if (withItemsList != null && !withItemsList.isEmpty()) {
            for (WithItem<?> withItem : withItemsList) {
                withItem.accept((SelectVisitor<?>) this, context);
            }
        }
        for (Select selectBody : list.getSelects()) {
            selectBody.accept((SelectVisitor<?>) this, context);
        }
        return null;
    }

    @Override
    public void visit(SetOperationList setOpList) {
        SelectVisitor.super.visit(setOpList);
    }

    @Override
    public <S> Void visit(ExtractExpression eexpr, S context) {
        if (eexpr.getExpression() != null) {
            eexpr.getExpression().accept(this, context);
        }
        return null;
    }

    @Override
    public <S> Void visit(LateralSubSelect lateralSubSelect, S context) {
        if (lateralSubSelect.getAlias() != null) {
            otherItemNames.add(TableParser.getInstance().parse(lateralSubSelect.getAlias()));
        }
        lateralSubSelect.getSelect().accept((SelectVisitor<?>) this, context);
        return null;
    }

    @Override
    public void visit(LateralSubSelect lateralSubSelect) {
        SelectVisitor.super.visit(lateralSubSelect);
    }

    @Override
    public <S> Void visit(TableStatement tableStatement, S context) {
        tableStatement.getTable().accept(this, null);
        return null;
    }

    @Override
    public void visit(TableStatement tableStatement) {
        SelectVisitor.super.visit(tableStatement);
    }

    /**
     * Initializes the table names collector.
     * Column processing is only allowed for expression parsing, where table names
     * might only be found in column references. For complete statements, only FROM
     * items are used to avoid mistaking aliases for table names.
     *
     * @param allowColumnProcessing true to allow extracting tables from column references
     */
    protected void init(boolean allowColumnProcessing) {
        otherItemNames = new HashSet<>();
        tables = new HashSet<>();
        this.allowColumnProcessing = allowColumnProcessing;
    }

    @Override
    public <S> Void visit(IntervalExpression intervalExpression, S context) {
        if (intervalExpression.getExpression() != null) {
            intervalExpression.getExpression().accept(this, context);
        }
        return null;
    }

    @Override
    public <S> Void visit(JdbcNamedParameter jdbcNamedParameter, S context) {

        return null;
    }

    @Override
    public <S> Void visit(OracleHierarchicalExpression hierarchicalExpression, S context) {
        if (hierarchicalExpression.getStartExpression() != null) {
            hierarchicalExpression.getStartExpression().accept(this, context);
        }

        if (hierarchicalExpression.getConnectExpression() != null) {
            hierarchicalExpression.getConnectExpression().accept(this, context);
        }
        return null;
    }

    @Override
    public <S> Void visit(RegExpMatchOperator regExpMatchOperator, S context) {
        visitBinaryExpression(regExpMatchOperator);
        return null;
    }

    @Override
    public <S> Void visit(JsonExpression jsonExpr, S context) {
        if (jsonExpr.getExpression() != null) {
            jsonExpr.getExpression().accept(this, context);
        }
        return null;
    }

    @Override
    public <S> Void visit(JsonOperator jsonExpr, S context) {
        visitBinaryExpression(jsonExpr);
        return null;
    }

    @Override
    public <S> Void visit(AllColumns allColumns, S context) {

        return null;
    }

    @Override
    public <S> Void visit(AllTableColumns allTableColumns, S context) {

        return null;
    }

    @Override
    public <S> Void visit(AllValue allValue, S context) {

        return null;
    }

    @Override
    public <S> Void visit(IsDistinctExpression isDistinctExpression, S context) {
        visitBinaryExpression(isDistinctExpression);
        return null;
    }

    @Override
    public <S> Void visit(SelectItem<?> item, S context) {
        item.getExpression().accept(this, context);
        return null;
    }

    @Override
    public void visit(SelectItem<? extends Expression> selectItem) {
        SelectItemVisitor.super.visit(selectItem);
    }

    @Override
    public <S> Void visit(UserVariable userVariable, S context) {

        return null;
    }

    @Override
    public <S> Void visit(NumericBind numericBind, S context) {


        return null;
    }

    @Override
    public <S> Void visit(KeepExpression keepExpression, S context) {

        return null;
    }

    @Override
    public <S> Void visit(MySQLGroupConcat groupConcat, S context) {

        return null;
    }

    @Override
    public <S> Void visit(Delete delete, S context) {
        visit(delete.getTable(), context);

        if (delete.getUsingList() != null) {
            for (Table using : delete.getUsingList()) {
                visit(using, context);
            }
        }

        visitJoins(delete.getJoins(), context);

        if (delete.getWhere() != null) {
            delete.getWhere().accept(this, context);
        }
        return null;
    }

    @Override
    public void visit(Delete delete) {
        StatementVisitor.super.visit(delete);
    }

    @Override
    public <S> Void visit(ParenthesedDelete delete, S context) {
        return visit(delete.getDelete(), context);
    }

    @Override
    public <S> Void visit(Update update, S context) {
        if (update.getWithItemsList() != null) {
            for (WithItem<?> withItem : update.getWithItemsList()) {
                withItem.accept((SelectVisitor<?>) this, context);
            }
        }

        visit(update.getTable(), context);

        if (update.getStartJoins() != null) {
            for (Join join : update.getStartJoins()) {
                join.getRightItem().accept(this, context);
            }
        }

        if (update.getUpdateSets() != null) {
            for (UpdateSet updateSet : update.getUpdateSets()) {
                updateSet.getColumns().accept(this, context);
                updateSet.getValues().accept(this, context);
            }
        }

        if (update.getFromItem() != null) {
            update.getFromItem().accept(this, context);
        }

        if (update.getJoins() != null) {
            for (Join join : update.getJoins()) {
                join.getRightItem().accept(this, context);
                for (Expression expression : join.getOnExpressions()) {
                    expression.accept(this, context);
                }
            }
        }

        if (update.getWhere() != null) {
            update.getWhere().accept(this, context);
        }
        return null;
    }

    @Override
    public <S> Void visit(ParenthesedUpdate update, S context) {
        return visit(update.getUpdate(), context);
    }

    @Override
    public void visit(Update update) {
        StatementVisitor.super.visit(update);
    }

    @Override
    public <S> Void visit(Insert insert, S context) {
        visit(insert.getTable(), context);
        if (insert.getWithItemsList() != null) {
            for (WithItem<?> withItem : insert.getWithItemsList()) {
                withItem.accept((SelectVisitor<?>) this, context);
            }
        }
        if (insert.getSelect() != null) {
            visit(insert.getSelect(), context);
        }
        return null;
    }

    @Override
    public <S> Void visit(ParenthesedInsert insert, S context) {
        return visit(insert.getInsert(), context);
    }

    @Override
    public void visit(Insert insert) {
        StatementVisitor.super.visit(insert);
    }

    @Override
    public <S> Void visit(Analyze analyze, S context) {
        visit(analyze.getTable(), context);
        return null;
    }

    @Override
    public void visit(Analyze analyze) {
        StatementVisitor.super.visit(analyze);
    }

    @Override
    public <S> Void visit(Drop drop, S context) {
        visit(drop.getName(), context);
        return null;
    }

    @Override
    public void visit(Drop drop) {
        StatementVisitor.super.visit(drop);
    }

    @Override
    public <S> Void visit(Truncate truncate, S context) {
        visit(truncate.getTable(), context);
        return null;
    }

    @Override
    public void visit(Truncate truncate) {
        StatementVisitor.super.visit(truncate);
    }

    @Override
    public <S> Void visit(CreateIndex createIndex, S context) {
        throwUnsupported(createIndex);
        return null;
    }

    @Override
    public void visit(CreateIndex createIndex) {
        StatementVisitor.super.visit(createIndex);
    }

    @Override
    public <S> Void visit(CreateSchema createSchema, S context) {
        throwUnsupported(createSchema);
        return null;
    }

    @Override
    public void visit(CreateSchema createSchema) {
        StatementVisitor.super.visit(createSchema);
    }

    @Override
    public <S> Void visit(CreateTable create, S context) {
        visit(create.getTable(), null);
        if (create.getSelect() != null) {
            create.getSelect().accept((SelectVisitor<?>) this, context);
        }
        return null;
    }

    @Override
    public void visit(CreateTable createTable) {
        StatementVisitor.super.visit(createTable);
    }

    @Override
    public <S> Void visit(CreateView create, S context) {
        visit(create.getView(), null);
        if (create.getSelect() != null) {
            create.getSelect().accept((SelectVisitor<?>) this, context);
        }
        return null;
    }

    @Override
    public void visit(CreateView createView) {
        StatementVisitor.super.visit(createView);
    }

    @Override
    public <S> Void visit(Alter alter, S context) {
        return alter.getTable().accept(this, context);
    }

    @Override
    public void visit(Alter alter) {
        alter.getTable().accept(this, null);
    }

    @Override
    public <S> Void visit(Statements statements, S context) {
        for (Statement statement : statements) {
            statement.accept(this, context);
        }
        return null;
    }

    @Override
    public void visit(Statements statements) {
        StatementVisitor.super.visit(statements);
    }

    @Override
    public <S> Void visit(Execute execute, S context) {
        throwUnsupported(execute);
        return null;
    }

    @Override
    public void visit(Execute execute) {
        StatementVisitor.super.visit(execute);
    }

    @Override
    public <S> Void visit(SetStatement setStatement, S context) {
        throwUnsupported(setStatement);
        return null;
    }

    @Override
    public void visit(SetStatement set) {
        StatementVisitor.super.visit(set);
    }

    @Override
    public <S> Void visit(ResetStatement reset, S context) {
        throwUnsupported(reset);
        return null;
    }

    @Override
    public void visit(ResetStatement reset) {
        StatementVisitor.super.visit(reset);
    }

    @Override
    public <S> Void visit(ShowColumnsStatement showColumnsStatement, S context) {
        throwUnsupported(showColumnsStatement);
        return null;
    }

    @Override
    public void visit(ShowColumnsStatement showColumns) {
        StatementVisitor.super.visit(showColumns);
    }

    @Override
    public <S> Void visit(ShowIndexStatement showIndex, S context) {
        throwUnsupported(showIndex);
        return null;
    }

    @Override
    public void visit(ShowIndexStatement showIndex) {
        StatementVisitor.super.visit(showIndex);
    }

    @Override
    public <S> Void visit(RowConstructor<?> rowConstructor, S context) {
        for (Expression expr : rowConstructor) {
            expr.accept(this, context);
        }
        return null;
    }

    @Override
    public <S> Void visit(RowGetExpression rowGetExpression, S context) {
        rowGetExpression.getExpression().accept(this, context);
        return null;
    }

    @Override
    public <S> Void visit(HexValue hexValue, S context) {
        return null;
    }

    @Override
    public <S> Void visit(Merge merge, S context) {
        visit(merge.getTable(), context);
        if (merge.getWithItemsList() != null) {
            for (WithItem<?> withItem : merge.getWithItemsList()) {
                withItem.accept((SelectVisitor<?>) this, context);
            }
        }

        if (merge.getFromItem() != null) {
            merge.getFromItem().accept(this, context);
        }
        return null;
    }

    @Override
    public void visit(Merge merge) {
        StatementVisitor.super.visit(merge);
    }

    @Override
    public <S> Void visit(OracleHint hint, S context) {
        return null;
    }

    @Override
    public <S> Void visit(TableFunction tableFunction, S context) {
        visit(tableFunction.getFunction(), null);
        return null;
    }

    @Override
    public void visit(TableFunction tableFunction) {
        FromItemVisitor.super.visit(tableFunction);
    }

    @Override
    public <S> Void visit(AlterView alterView, S context) {
        throwUnsupported(alterView);
        return null;
    }

    @Override
    public void visit(AlterView alterView) {
        StatementVisitor.super.visit(alterView);
    }

    @Override
    public <S> Void visit(RefreshMaterializedViewStatement materializedView, S context) {
        visit(materializedView.getView(), context);
        return null;
    }

    @Override
    public void visit(RefreshMaterializedViewStatement materializedView) {
        StatementVisitor.super.visit(materializedView);
    }

    @Override
    public <S> Void visit(TimeKeyExpression timeKeyExpression, S context) {
        return null;
    }

    @Override
    public <S> Void visit(DateTimeLiteralExpression literal, S context) {
        return null;
    }

    @Override
    public <S> Void visit(Commit commit, S context) {
        return null;
    }

    @Override
    public void visit(Commit commit) {
        StatementVisitor.super.visit(commit);
    }

    @Override
    public <S> Void visit(Upsert upsert, S context) {
        visit(upsert.getTable(), context);
        if (upsert.getExpressions() != null) {
            upsert.getExpressions().accept(this, context);
        }
        if (upsert.getSelect() != null) {
            visit(upsert.getSelect(), context);
        }
        return null;
    }

    @Override
    public void visit(Upsert upsert) {
        StatementVisitor.super.visit(upsert);
    }

    @Override
    public <S> Void visit(UseStatement use, S context) {
        return null;
    }

    @Override
    public void visit(UseStatement use) {
        StatementVisitor.super.visit(use);
    }

    @Override
    public <S> Void visit(ParenthesedFromItem parenthesis, S context) {
        parenthesis.getFromItem().accept(this, context);
        // support join keyword in fromItem
        visitJoins(parenthesis.getJoins(), context);
        return null;
    }

    @Override
    public void visit(ParenthesedFromItem parenthesedFromItem) {
        FromItemVisitor.super.visit(parenthesedFromItem);
    }

    /**
     * Visits all JOIN clauses to extract table references
     *
     * @param joins   list of JOIN clauses to visit
     * @param context the visitor context
     * @param <S>     context type parameter
     */
    private <S> void visitJoins(List<Join> joins, S context) {
        if (joins == null) {
            return;
        }
        for (Join join : joins) {
            join.getFromItem().accept(this, context);
            join.getRightItem().accept(this, context);
            for (Expression expression : join.getOnExpressions()) {
                expression.accept(this, context);
            }
        }
    }

    @Override
    public <S> Void visit(Block block, S context) {
        if (block.getStatements() != null) {
            visit(block.getStatements(), context);
        }
        return null;
    }

    @Override
    public void visit(Block block) {
        StatementVisitor.super.visit(block);
    }

    @Override
    public <S> Void visit(Comment comment, S context) {
        if (comment.getTable() != null) {
            visit(comment.getTable(), context);
        }
        if (comment.getColumn() != null) {
            Table table = comment.getColumn().getTable();
            if (table != null) {
                visit(table, context);
            }
        }
        return null;
    }

    @Override
    public void visit(Comment comment) {
        StatementVisitor.super.visit(comment);
    }

    @Override
    public <S> Void visit(Values values, S context) {
        values.getExpressions().accept(this, context);
        return null;
    }

    @Override
    public void visit(Values values) {
        SelectVisitor.super.visit(values);
    }

    @Override
    public <S> Void visit(DescribeStatement describe, S context) {
        describe.getTable().accept(this, context);
        return null;
    }

    @Override
    public void visit(DescribeStatement describe) {
        StatementVisitor.super.visit(describe);
    }

    @Override
    public <S> Void visit(ExplainStatement explainStatement, S context) {
        if (explainStatement.getStatement() != null) {
            explainStatement.getStatement().accept((StatementVisitor<?>) this, context);
        }
        return null;
    }

    @Override
    public void visit(ExplainStatement explainStatement) {
        StatementVisitor.super.visit(explainStatement);
    }

    @Override
    public <S> Void visit(NextValExpression nextVal, S context) {
        return null;
    }

    @Override
    public <S> Void visit(CollateExpression collateExpression, S context) {
        collateExpression.getLeftExpression().accept(this, context);
        return null;
    }

    @Override
    public <S> Void visit(ShowStatement showStatement, S context) {
        return null;
    }

    @Override
    public void visit(ShowStatement showStatement) {
        StatementVisitor.super.visit(showStatement);
    }

    @Override
    public <S> Void visit(SimilarToExpression expr, S context) {
        visitBinaryExpression(expr);
        return null;
    }

    @Override
    public <S> Void visit(DeclareStatement declareStatement, S context) {
        return null;
    }

    @Override
    public void visit(DeclareStatement declareStatement) {
        StatementVisitor.super.visit(declareStatement);
    }

    @Override
    public <S> Void visit(Grant grant, S context) {
        return null;
    }

    @Override
    public void visit(Grant grant) {
        StatementVisitor.super.visit(grant);
    }

    @Override
    public <S> Void visit(ArrayExpression array, S context) {
        array.getObjExpression().accept(this, context);
        if (array.getStartIndexExpression() != null) {
            array.getIndexExpression().accept(this, context);
        }
        if (array.getStartIndexExpression() != null) {
            array.getStartIndexExpression().accept(this, context);
        }
        if (array.getStopIndexExpression() != null) {
            array.getStopIndexExpression().accept(this, context);
        }
        return null;
    }

    @Override
    public <S> Void visit(ArrayConstructor array, S context) {
        for (Expression expression : array.getExpressions()) {
            expression.accept(this, context);
        }
        return null;
    }

    @Override
    public <S> Void visit(CreateSequence createSequence, S context) {
        throwUnsupported(createSequence);
        return null;
    }

    @Override
    public void visit(CreateSequence createSequence) {
        StatementVisitor.super.visit(createSequence);
    }

    @Override
    public <S> Void visit(AlterSequence alterSequence, S context) {
        throwUnsupported(alterSequence);
        return null;
    }

    @Override
    public void visit(AlterSequence alterSequence) {
        StatementVisitor.super.visit(alterSequence);
    }

    @Override
    public <S> Void visit(CreateFunctionalStatement createFunctionalStatement, S context) {
        throwUnsupported(createFunctionalStatement);
        return null;
    }

    @Override
    public void visit(CreateFunctionalStatement createFunctionalStatement) {
        StatementVisitor.super.visit(createFunctionalStatement);
    }

    @Override
    public <S> Void visit(ShowTablesStatement showTables, S context) {
        throwUnsupported(showTables);
        return null;
    }

    @Override
    public void visit(ShowTablesStatement showTables) {
        StatementVisitor.super.visit(showTables);
    }

    @Override
    public <S> Void visit(TSQLLeftJoin tsqlLeftJoin, S context) {
        visitBinaryExpression(tsqlLeftJoin);
        return null;
    }

    @Override
    public <S> Void visit(TSQLRightJoin tsqlRightJoin, S context) {
        visitBinaryExpression(tsqlRightJoin);
        return null;
    }

    @Override
    public <S> Void visit(StructType structType, S context) {
        if (structType.getArguments() != null) {
            for (SelectItem<?> selectItem : structType.getArguments()) {
                selectItem.getExpression().accept(this, context);
            }
        }
        return null;
    }

    @Override
    public <S> Void visit(LambdaExpression lambdaExpression, S context) {
        lambdaExpression.getExpression().accept(this, context);
        return null;
    }

    @Override
    public <S> Void visit(HighExpression highExpression, S context) {
        highExpression.getExpression().accept(this, context);
        return null;
    }

    @Override
    public <S> Void visit(LowExpression lowExpression, S context) {
        lowExpression.getExpression().accept(this, context);
        return null;
    }

    @Override
    public <S> Void visit(Plus plus, S context) {
        visitBinaryExpression(plus);
        return null;
    }

    @Override
    public <S> Void visit(PriorTo priorTo, S context) {
        visitBinaryExpression(priorTo);
        return null;
    }

    @Override
    public <S> Void visit(Inverse inverse, S context) {
        inverse.getExpression().accept(this, context);
        return null;
    }

    @Override
    public <S> Void visit(CosineSimilarity cosineSimilarity, S context) {
        cosineSimilarity.getLeftExpression().accept(this, context);
        cosineSimilarity.getRightExpression().accept(this, context);
        return null;
    }

    @Override
    public <S> Void visit(VariableAssignment variableAssignment, S context) {
        variableAssignment.getVariable().accept(this, context);
        variableAssignment.getExpression().accept(this, context);
        return null;
    }

    @Override
    public <S> Void visit(XMLSerializeExpr xmlSerializeExpr, S context) {

        return null;
    }

    @Override
    public <S> Void visit(CreateSynonym createSynonym, S context) {
        throwUnsupported(createSynonym);
        return null;
    }

    @Override
    public void visit(CreateSynonym createSynonym) {
        StatementVisitor.super.visit(createSynonym);
    }

    @Override
    public <S> Void visit(TimezoneExpression timezoneExpression, S context) {
        timezoneExpression.getLeftExpression().accept(this, context);
        return null;
    }

    @Override
    public <S> Void visit(SavepointStatement savepointStatement, S context) {
        return null;
    }

    @Override
    public void visit(SavepointStatement savepointStatement) {
        StatementVisitor.super.visit(savepointStatement);
    }

    @Override
    public <S> Void visit(RollbackStatement rollbackStatement, S context) {

        return null;
    }

    @Override
    public void visit(RollbackStatement rollbackStatement) {
        StatementVisitor.super.visit(rollbackStatement);
    }

    @Override
    public <S> Void visit(AlterSession alterSession, S context) {

        return null;
    }

    @Override
    public void visit(AlterSession alterSession) {
        StatementVisitor.super.visit(alterSession);
    }

    @Override
    public <S> Void visit(JsonAggregateFunction expression, S context) {
        Expression expr = expression.getExpression();
        if (expr != null) {
            expr.accept(this, context);
        }

        expr = expression.getFilterExpression();
        if (expr != null) {
            expr.accept(this, context);
        }
        return null;
    }

    @Override
    public <S> Void visit(JsonFunction expression, S context) {
        for (JsonFunctionExpression expr : expression.getExpressions()) {
            expr.getExpression().accept(this, context);
        }
        return null;
    }

    @Override
    public <S> Void visit(ConnectByRootOperator connectByRootOperator, S context) {
        connectByRootOperator.getColumn().accept(this, context);
        return null;
    }

    @Override
    public <S> Void visit(ConnectByPriorOperator connectByPriorOperator, S context) {
        connectByPriorOperator.getColumn().accept(this, context);
        return null;
    }

    @Override
    public <S> Void visit(IfElseStatement ifElseStatement, S context) {
        ifElseStatement.getIfStatement().accept(this, context);
        if (ifElseStatement.getElseStatement() != null) {
            ifElseStatement.getElseStatement().accept(this, context);
        }
        return null;
    }

    @Override
    public void visit(IfElseStatement ifElseStatement) {
        StatementVisitor.super.visit(ifElseStatement);
    }

    @Override
    public <S> Void visit(OracleNamedFunctionParameter oracleNamedFunctionParameter, S context) {
        oracleNamedFunctionParameter.getExpression().accept(this, context);
        return null;
    }

    @Override
    public <S> Void visit(RenameTableStatement renameTableStatement, S context) {
        for (Map.Entry<Table, Table> e : renameTableStatement.getTableNames()) {
            e.getKey().accept(this, context);
            e.getValue().accept(this, context);
        }
        return null;
    }

    @Override
    public void visit(RenameTableStatement renameTableStatement) {
        StatementVisitor.super.visit(renameTableStatement);
    }

    @Override
    public <S> Void visit(PurgeStatement purgeStatement, S context) {
        if (purgeStatement.getPurgeObjectType() == PurgeObjectType.TABLE) {
            ((Table) purgeStatement.getObject()).accept(this, context);
        }
        return null;
    }

    @Override
    public void visit(PurgeStatement purgeStatement) {
        StatementVisitor.super.visit(purgeStatement);
    }

    @Override
    public <S> Void visit(AlterSystemStatement alterSystemStatement, S context) {
        // no tables involved in this statement
        return null;
    }

    @Override
    public void visit(AlterSystemStatement alterSystemStatement) {
        StatementVisitor.super.visit(alterSystemStatement);
    }

    @Override
    public <S> Void visit(UnsupportedStatement unsupportedStatement, S context) {
        // no tables involved in this statement
        return null;
    }

    @Override
    public void visit(UnsupportedStatement unsupportedStatement) {
        StatementVisitor.super.visit(unsupportedStatement);
    }

    @Override
    public <S> Void visit(GeometryDistance geometryDistance, S context) {
        visitBinaryExpression(geometryDistance);
        return null;
    }

}
