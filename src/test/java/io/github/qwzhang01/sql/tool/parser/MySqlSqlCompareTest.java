package io.github.qwzhang01.sql.tool.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("MySqlSqlCompare 测试")
class MySqlSqlCompareTest {

    private MySqlSqlCompare compare;

    @BeforeEach
    void setUp() {
        compare = new MySqlSqlCompare();
    }

    // ========== 基础相等测试 ==========

    @Test
    @DisplayName("测试null值比较")
    void testNullComparison() {
        assertTrue(compare.equal(null, null));
        assertFalse(compare.equal(null, "test"));
        assertFalse(compare.equal("test", null));
    }

    @Test
    @DisplayName("测试相同字符串比较")
    void testIdenticalStrings() {
        assertTrue(compare.equal("users", "users"));
        assertTrue(compare.equal("user_id", "user_id"));
        assertTrue(compare.equal("", ""));
    }

    @Test
    @DisplayName("测试不同字符串比较")
    void testDifferentStrings() {
        assertFalse(compare.equal("users", "posts"));
        assertFalse(compare.equal("user_id", "post_id"));
        assertFalse(compare.equal("test", ""));
    }

    // ========== 大小写不敏感测试 ==========

    @Test
    @DisplayName("测试大小写不敏感比较")
    void testCaseInsensitive() {
        assertTrue(compare.equal("users", "USERS"));
        assertTrue(compare.equal("USER_ID", "user_id"));
        assertTrue(compare.equal("UsErS", "uSeRs"));
        assertTrue(compare.equal("MyTable", "MYTABLE"));
    }

    @ParameterizedTest
    @CsvSource({
            "users, USERS",
            "USER_ID, user_id",
            "MyTable, mytable",
            "column_name, COLUMN_NAME",
            "test123, TEST123"
    })
    @DisplayName("测试各种大小写组合")
    void testVariousCaseCombinations(String str1, String str2) {
        assertTrue(compare.equal(str1, str2));
    }

    // ========== 引号处理测试 ==========

    @Test
    @DisplayName("测试反引号处理")
    void testBacktickHandling() {
        assertTrue(compare.equal("`users`", "users"));
        assertTrue(compare.equal("users", "`users`"));
        assertTrue(compare.equal("`users`", "`users`"));
        assertTrue(compare.equal("`user_table`", "USER_TABLE"));
    }

    @Test
    @DisplayName("测试双引号处理")
    void testDoubleQuoteHandling() {
        assertTrue(compare.equal("\"users\"", "users"));
        assertTrue(compare.equal("users", "\"users\""));
        assertTrue(compare.equal("\"users\"", "\"users\""));
        assertTrue(compare.equal("\"user_table\"", "USER_TABLE"));
    }

    @Test
    @DisplayName("测试单引号处理")
    void testSingleQuoteHandling() {
        assertTrue(compare.equal("'users'", "users"));
        assertTrue(compare.equal("users", "'users'"));
        assertTrue(compare.equal("'users'", "'users'"));
        assertTrue(compare.equal("'user_table'", "USER_TABLE"));
    }

    @Test
    @DisplayName("测试混合引号处理")
    void testMixedQuoteHandling() {
        assertTrue(compare.equal("`users`", "\"users\""));
        assertTrue(compare.equal("'users'", "`users`"));
        assertTrue(compare.equal("\"users\"", "'users'"));
        assertTrue(compare.equal("`user_table`", "\"USER_TABLE\""));
    }

    @Test
    @DisplayName("测试多层引号处理")
    void testNestedQuoteHandling() {
        assertTrue(compare.equal("```users```", "users"));
        assertTrue(compare.equal("\"\"\"users\"\"\"", "users"));
        assertTrue(compare.equal("'''users'''", "users"));
    }

    // ========== 空白字符处理测试 ==========

    @Test
    @DisplayName("测试前后空白字符")
    void testTrimming() {
        assertTrue(compare.equal("  users  ", "users"));
        assertTrue(compare.equal("users", "  users  "));
        assertTrue(compare.equal("  users  ", "  users  "));
        assertTrue(compare.equal("\tusers\n", "users"));
        assertTrue(compare.equal("\r\nusers\r\n", "USERS"));
    }

    @Test
    @DisplayName("测试各种空白字符")
    void testVariousWhitespace() {
        assertTrue(compare.equal("\t\r\n users \t\r\n", "USERS"));
        assertTrue(compare.equal("   users   ", "`USERS`"));
        assertTrue(compare.equal("\tusers\n", "\"users\""));
    }

    // ========== 复合情况测试 ==========

    @Test
    @DisplayName("测试引号和大小写组合")
    void testQuotesAndCaseCombination() {
        assertTrue(compare.equal("`Users`", "USERS"));
        assertTrue(compare.equal("\"user_ID\"", "user_id"));
        assertTrue(compare.equal("'MyTable'", "MYTABLE"));
    }

    @Test
    @DisplayName("测试引号、大小写和空白字符组合")
    void testQuotesCaseAndWhitespaceCombination() {
        assertTrue(compare.equal("  `Users`  ", "USERS"));
        assertTrue(compare.equal("\t\"user_ID\"\n", "user_id"));
        assertTrue(compare.equal("  'MyTable'  ", "MYTABLE"));
        assertTrue(compare.equal("  `  users  `  ", "USERS"));
    }

    // ========== 特殊字符测试 ==========

    @Test
    @DisplayName("测试包含数字的标识符")
    void testIdentifiersWithNumbers() {
        assertTrue(compare.equal("table1", "TABLE1"));
        assertTrue(compare.equal("`user123`", "USER123"));
        assertTrue(compare.equal("\"col_2\"", "COL_2"));
    }

    @Test
    @DisplayName("测试包含下划线的标识符")
    void testIdentifiersWithUnderscores() {
        assertTrue(compare.equal("user_name", "USER_NAME"));
        assertTrue(compare.equal("`user_table_info`", "USER_TABLE_INFO"));
        assertTrue(compare.equal("\"_private_col\"", "_PRIVATE_COL"));
    }

    @Test
    @DisplayName("测试特殊字符标识符")
    void testSpecialCharacterIdentifiers() {
        // 测试包含美元符号的标识符（某些数据库支持）
        assertTrue(compare.equal("$special", "$SPECIAL"));
        assertTrue(compare.equal("`$column`", "$COLUMN"));
    }

    // ========== 边界情况测试 ==========

    @Test
    @DisplayName("测试空字符串")
    void testEmptyStrings() {
        assertTrue(compare.equal("", ""));
        assertTrue(compare.equal("``", ""));
        assertTrue(compare.equal("\"\"", ""));
        assertTrue(compare.equal("''", ""));
        assertTrue(compare.equal("   ", ""));
    }

    @Test
    @DisplayName("测试只有引号的字符串")
    void testOnlyQuotes() {
        assertTrue(compare.equal("`", ""));
        assertTrue(compare.equal("\"", ""));
        assertTrue(compare.equal("'", ""));
        assertTrue(compare.equal("```", ""));
        assertTrue(compare.equal("\"\"\"", ""));
    }

    @Test
    @DisplayName("测试只有空白字符的字符串")
    void testOnlyWhitespace() {
        assertTrue(compare.equal("   ", ""));
        assertTrue(compare.equal("\t\n\r", ""));
        assertTrue(compare.equal("  \t  ", "   "));
    }

    // ========== 长字符串测试 ==========

    @Test
    @DisplayName("测试长标识符")
    void testLongIdentifiers() {
        String longName1 = "very_long_table_name_with_many_underscores_and_words";
        String longName2 = "VERY_LONG_TABLE_NAME_WITH_MANY_UNDERSCORES_AND_WORDS";
        assertTrue(compare.equal(longName1, longName2));

        String quotedLongName = "`" + longName1 + "`";
        assertTrue(compare.equal(quotedLongName, longName2));
    }

    @Test
    @DisplayName("测试超长字符串")
    void testVeryLongStrings() {
        StringBuilder sb1 = new StringBuilder();
        StringBuilder sb2 = new StringBuilder();

        for (int i = 0; i < 1000; i++) {
            sb1.append("a");
            sb2.append("A");
        }

        assertTrue(compare.equal(sb1.toString(), sb2.toString()));
    }

    // ========== 异常情况测试 ==========

    @ParameterizedTest
    @ValueSource(strings = {
            "normal_table",
            "`quoted_table`",
            "\"double_quoted\"",
            "'single_quoted'",
            "  spaced_table  ",
            "MixedCase",
            "table123",
            "_underscore_start",
            "table$special"
    })
    @DisplayName("测试各种格式的标识符与标准格式比较")
    void testVariousFormatsAgainstStandard(String identifier) {
        String standard = "normal_table";
        // 这些应该不相等，除非是相同的标识符
        if (identifier.replaceAll("[`\"'\\s]", "").equalsIgnoreCase(standard)) {
            assertTrue(compare.equal(identifier, standard));
        } else {
            assertFalse(compare.equal(identifier, standard));
        }
    }

    @Test
    @DisplayName("测试Unicode字符")
    void testUnicodeCharacters() {
        assertTrue(compare.equal("用户表", "用户表"));
        assertTrue(compare.equal("`用户表`", "用户表"));
        assertTrue(compare.equal("table_测试", "TABLE_测试"));
    }

    @Test
    @DisplayName("测试Emoji字符")
    void testEmojiCharacters() {
        assertTrue(compare.equal("table😀", "TABLE😀"));
        assertTrue(compare.equal("`table😀`", "table😀"));
    }

    // ========== 性能测试 ==========

    @Test
    @DisplayName("测试大量比较操作性能")
    void testPerformance() {
        String[] identifiers = {
                "users", "USERS", "`users`", "\"users\"", "'users'",
                "user_table", "USER_TABLE", "`user_table`", "posts", "POSTS"
        };

        long startTime = System.currentTimeMillis();

        // 执行大量比较操作
        for (int i = 0; i < 10000; i++) {
            for (String id1 : identifiers) {
                for (String id2 : identifiers) {
                    compare.equal(id1, id2);
                }
            }
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // 性能测试：应该在合理时间内完成
        assertTrue(duration < 5000, "比较操作耗时过长: " + duration + "ms");
    }

    // ========== 实际使用场景测试 ==========

    @Test
    @DisplayName("测试实际数据库标识符比较场景")
    void testRealWorldScenarios() {
        // 表名比较
        assertTrue(compare.equal("user_accounts", "USER_ACCOUNTS"));
        assertTrue(compare.equal("`user_accounts`", "user_accounts"));

        // 列名比较
        assertTrue(compare.equal("user_id", "USER_ID"));
        assertTrue(compare.equal("\"first_name\"", "first_name"));

        // 索引名比较
        assertTrue(compare.equal("idx_user_email", "IDX_USER_EMAIL"));
        assertTrue(compare.equal("`idx_user_email`", "idx_user_email"));

        // 约束名比较
        assertTrue(compare.equal("fk_user_role", "FK_USER_ROLE"));
        assertTrue(compare.equal("'fk_user_role'", "fk_user_role"));
    }

    @Test
    @DisplayName("测试SQL关键字比较")
    void testSqlKeywordComparison() {
        // 虽然通常不会比较关键字，但测试一下兼容性
        assertTrue(compare.equal("SELECT", "select"));
        assertTrue(compare.equal("FROM", "from"));
        assertTrue(compare.equal("WHERE", "where"));
        assertTrue(compare.equal("`ORDER`", "order")); // ORDER是关键字，可能需要引号
    }
}