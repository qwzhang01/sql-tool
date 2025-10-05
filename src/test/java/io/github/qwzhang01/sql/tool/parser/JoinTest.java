package io.github.qwzhang01.sql.tool.parser;

import io.github.qwzhang01.sql.tool.model.JoinInfo;
import io.github.qwzhang01.sql.tool.util.SqlParserUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@DisplayName("MySqlPureSqlParser 测试")
class JoinTest {

    @Test
    @DisplayName("测试 left join")
    void left() {
        String sql = "LEFT JOIN `order_table` ON `user_table`.`user_id` = `order_table`.`user_id`";
        List<JoinInfo> result = SqlParserUtils.parseJoin(sql);

        assert result.size() == 1;
    }
}