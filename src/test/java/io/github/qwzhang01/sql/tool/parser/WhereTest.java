package io.github.qwzhang01.sql.tool.parser;

import io.github.qwzhang01.sql.tool.model.SqlInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("MySqlPureSqlParser 测试")
class WhereTest {

    private MySqlPureSqlParser parser;

    @BeforeEach
    void setUp() {
        parser = new MySqlPureSqlParser();
    }

    @Test
    @DisplayName("测试 where")
    void where() {
        String sql = "WHERE `user_table`.`user_id` = 1";
        SqlInfo result = parser.parseWhere(sql);

        System.out.println("");
    }
}