package io.github.qwzhang01.sql.tool.jsqlparser;

import io.github.qwzhang01.sql.tool.model.SqlTable;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;

import java.util.List;

public class JoinComplexExpressionVisitor extends ExpressionVisitorAdapter {
    private final String joinTableName;
    private final List<SqlTable> table;

    public JoinComplexExpressionVisitor(String joinTableName, List<SqlTable> table) {
        this.joinTableName = joinTableName;
        this.table = table;
        // 主表名称替换
    }
}
