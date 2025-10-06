
package io.github.qwzhang01.sql.tool.parser;

import io.github.qwzhang01.sql.tool.helper.SqlParseHelper;
import io.github.qwzhang01.sql.tool.model.SqlObj;

public class SqlPlaceholderExample {

    // 测试代码
    public static void main(String[] args) {
        String sql = """
                SELECT u.* FROM users u
                LEFT JOIN dept d ON d.id = u.dept_id
                WHERE u.name = ?
                AND u.age > ?
                AND u.city IN (?, ?, ?)
                AND u.salary BETWEEN ? AND ?
                AND u.status LIKE ?
                """;
        SqlObj sqlObj = SqlParseHelper.parseSQL(sql);

        System.out.println("");
    }
}
