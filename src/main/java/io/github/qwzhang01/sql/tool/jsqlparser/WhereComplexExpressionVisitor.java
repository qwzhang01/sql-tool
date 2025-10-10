package io.github.qwzhang01.sql.tool.jsqlparser;

import io.github.qwzhang01.sql.tool.model.SqlTable;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;

import java.util.List;

public class WhereComplexExpressionVisitor extends ExpressionVisitorAdapter {
    private final List<SqlTable> table;

    public WhereComplexExpressionVisitor(List<SqlTable> table) {
        // 主表名称替换
        this.table = table;
    }
}
