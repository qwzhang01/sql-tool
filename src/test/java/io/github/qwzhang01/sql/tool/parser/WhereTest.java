package io.github.qwzhang01.sql.tool.parser;

import io.github.qwzhang01.sql.tool.helper.SqlParseHelper;
import io.github.qwzhang01.sql.tool.model.SqlCondition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@DisplayName("MySqlPureSqlParser 测试")
class WhereTest {

    @Test
    @DisplayName("测试 where")
    void where() {
        String sql = "WHERE `user_table`.`user_id` = 1";
        List<SqlCondition> result = SqlParseHelper.parseWhere(sql);

        assert result.size() == 1;
    }
}