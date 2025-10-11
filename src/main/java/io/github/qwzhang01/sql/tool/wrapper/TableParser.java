package io.github.qwzhang01.sql.tool.wrapper;

import io.github.qwzhang01.sql.tool.model.SqlTable;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.schema.Table;

/**
 * CCJSqlParserUtil 工具
 * 异常捕捉
 *
 * @author avinzhang
 */
public class TableParser {

    // 私有构造函数，防止外部实例化
    private TableParser() {
        // 初始化代码可以放在这里
    }

    // 获取单例实例的公共方法
    public static TableParser getInstance() {
        return SqlParserHolder.INSTANCE;
    }

    // 可以在这里添加其他业务方法
    public SqlTable parse(Table table) {
        return new SqlTable(table.getName(), table.getAlias() == null ? "" : table.getAlias().getName());
    }

    public SqlTable parse(Alias table) {
        return new SqlTable(table.getName(), "");
    }

    // 静态内部类，用于持有单例实例
    private static class SqlParserHolder {
        private static final TableParser INSTANCE = new TableParser();
    }
}
