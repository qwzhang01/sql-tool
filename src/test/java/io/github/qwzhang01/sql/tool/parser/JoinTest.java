package io.github.qwzhang01.sql.tool.parser;

import io.github.qwzhang01.sql.tool.model.JoinInfo;
import io.github.qwzhang01.sql.tool.model.SqlInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@DisplayName("MySqlPureSqlParser 测试")
class JoinTest {

    private MySqlPureSqlParser parser;

    @BeforeEach
    void setUp() {
        parser = new MySqlPureSqlParser();
    }

    @Test
    @DisplayName("测试 left join")
    void left() {
        String sql = "LEFT JOIN `order_table` ON `user_table`.`user_id` = `order_table`.`user_id`";
        List<JoinInfo> result = parser.parseJoin(sql);

        System.out.println("");
    }
}