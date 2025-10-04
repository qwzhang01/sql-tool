package io.github.qwzhang01.sql.tool.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MySqlSqlCleaner 测试")
class MySqlSqlCleanerTest {

    private MySqlSqlCleaner cleaner;

    @BeforeEach
    void setUp() {
        cleaner = new MySqlSqlCleaner();
    }

    // ========== cleanSql方法测试 ==========

    @Test
    @DisplayName("测试null和空字符串")
    void testNullAndEmpty() {
        assertNull(cleaner.cleanSql(null));
        assertEquals("", cleaner.cleanSql(""));
        assertEquals("", cleaner.cleanSql("   "));
        assertEquals("", cleaner.cleanSql("\t\n\r"));
    }

    @Test
    @DisplayName("测试基础SQL清理")
    void testBasicSqlCleaning() {
        String sql = "SELECT   *   FROM   users   WHERE   id   =   1";
        String expected = "SELECT * FROM users WHERE id = 1";
        assertEquals(expected, cleaner.cleanSql(sql));
    }

    @Test
    @DisplayName("测试单行注释清理")
    void testSingleLineComments() {
        String sql = "SELECT * FROM users -- this is a comment\nWHERE id = 1";
        String expected = "SELECT * FROM users WHERE id = 1";
        assertEquals(expected, cleaner.cleanSql(sql));
    }

    @Test
    @DisplayName("测试多行注释清理")
    void testMultiLineComments() {
        String sql = "SELECT * FROM users /* this is a \n multi-line comment */ WHERE id = 1";
        String expected = "SELECT * FROM users WHERE id = 1";
        assertEquals(expected, cleaner.cleanSql(sql));
    }

    @Test
    @DisplayName("测试嵌套注释")
    void testNestedComments() {
        String sql = "SELECT * FROM users /* outer /* inner */ comment */ WHERE id = 1";
        String result = cleaner.cleanSql(sql);
        // 应该正确处理嵌套注释
        assertNotNull(result);
        assertTrue(result.contains("SELECT"));
        assertTrue(result.contains("WHERE"));
    }

    @Test
    @DisplayName("测试字符串中的注释符号")
    void testCommentsInStrings() {
        String sql = "SELECT * FROM users WHERE name = 'John -- not a comment' AND description = 'Test /* also not a comment */'";
        String result = cleaner.cleanSql(sql);

        assertTrue(result.contains("John -- not a comment"));
        assertTrue(result.contains("Test /* also not a comment */"));
    }

    @Test
    @DisplayName("测试单引号字符串")
    void testSingleQuotedStrings() {
        String sql = "SELECT * FROM users WHERE name = 'O''Reilly' -- comment";
        String result = cleaner.cleanSql(sql);

        assertTrue(result.contains("O''Reilly"));
        assertFalse(result.contains("comment"));
    }

    @Test
    @DisplayName("测试双引号字符串")
    void testDoubleQuotedStrings() {
        String sql = "SELECT * FROM users WHERE name = \"John \"\"Doe\"\"\" -- comment";
        String result = cleaner.cleanSql(sql);

        assertTrue(result.contains("John \"\"Doe\"\""));
        assertFalse(result.contains("comment"));
    }

    @Test
    @DisplayName("测试转义字符")
    void testEscapedCharacters() {
        String sql = "SELECT * FROM users WHERE name = 'John\\'s' -- comment";
        String result = cleaner.cleanSql(sql);

        assertTrue(result.contains("John\\'s"));
        assertFalse(result.contains("comment"));
    }

    @Test
    @DisplayName("测试混合注释类型")
    void testMixedCommentTypes() {
        String sql = "SELECT * -- single line\nFROM users /* multi\nline */ WHERE id = 1";
        String expected = "SELECT * FROM users WHERE id = 1";
        assertEquals(expected, cleaner.cleanSql(sql));
    }

    @Test
    @DisplayName("测试连续空白字符")
    void testConsecutiveWhitespace() {
        String sql = "SELECT\t\t*\n\n\nFROM\r\r\rusers\t\n\rWHERE   id   =   1";
        String expected = "SELECT * FROM users WHERE id = 1";
        assertEquals(expected, cleaner.cleanSql(sql));
    }

    @Test
    @DisplayName("测试行尾注释")
    void testEndOfLineComments() {
        String sql = "SELECT * FROM users WHERE id = 1 -- final comment";
        String expected = "SELECT * FROM users WHERE id = 1";
        assertEquals(expected, cleaner.cleanSql(sql));
    }

    @Test
    @DisplayName("测试只有注释的SQL")
    void testOnlyComments() {
        String sql = "-- this is only a comment\n/* and this too */";
        String result = cleaner.cleanSql(sql);
        assertTrue(result.isEmpty() || result.trim().isEmpty());
    }

    // ========== cleanAndFormatSql方法测试 ==========

    @Test
    @DisplayName("测试SQL格式化")
    void testSqlFormatting() {
        String sql = "select*from users where id=1";
        String result = cleaner.cleanAndFormatSql(sql);

        assertTrue(result.contains(" SELECT "));
        assertTrue(result.contains(" FROM "));
        assertTrue(result.contains(" WHERE "));
        assertTrue(result.contains(" = "));
    }

    @Test
    @DisplayName("测试关键字格式化")
    void testKeywordFormatting() {
        String sql = "selectid,namefromuserswhereage>18orderbyname";
        String result = cleaner.cleanAndFormatSql(sql);

        assertTrue(result.contains(" SELECT "));
        assertTrue(result.contains(" FROM "));
        assertTrue(result.contains(" WHERE "));
        assertTrue(result.contains(" ORDER "));
        assertTrue(result.contains(" BY "));
    }

    @Test
    @DisplayName("测试操作符格式化")
    void testOperatorFormatting() {
        String sql = "SELECT * FROM users WHERE id=1AND age>=18OR status<>'deleted'";
        String result = cleaner.cleanAndFormatSql(sql);

        assertTrue(result.contains(" = "));
        assertTrue(result.contains(" >= "));
        assertTrue(result.contains(" <> "));
        assertTrue(result.contains(" AND "));
        assertTrue(result.contains(" OR "));
    }

    @Test
    @DisplayName("测试括号和分号格式化")
    void testPunctuationFormatting() {
        String sql = "SELECT*FROM users WHERE id IN(1,2,3);";
        String result = cleaner.cleanAndFormatSql(sql);

        assertTrue(result.contains("("));
        assertTrue(result.contains(")"));
        assertTrue(result.contains(","));
    }

    @Test
    @DisplayName("测试复杂SQL格式化")
    void testComplexSqlFormatting() {
        String sql = "selectu.id,u.namefromusersuleftjoinpostsponu.id=p.user_idwhereu.status='active'groupbyu.idorderbyname";
        String result = cleaner.cleanAndFormatSql(sql);

        assertTrue(result.contains(" SELECT "));
        assertTrue(result.contains(" FROM "));
        assertTrue(result.contains(" LEFT "));
        assertTrue(result.contains(" JOIN "));
        assertTrue(result.contains(" ON "));
        assertTrue(result.contains(" WHERE "));
        assertTrue(result.contains(" GROUP "));
        assertTrue(result.contains(" BY "));
        assertTrue(result.contains(" ORDER "));
    }

    // ========== containsComments方法测试 ==========

    @Test
    @DisplayName("测试检测单行注释")
    void testDetectSingleLineComments() {
        assertTrue(cleaner.containsComments("SELECT * FROM users -- comment"));
        assertTrue(cleaner.containsComments("-- comment at start"));
        assertTrue(cleaner.containsComments("SELECT * FROM users WHERE id = 1 -- end comment"));
    }

    @Test
    @DisplayName("测试检测多行注释")
    void testDetectMultiLineComments() {
        assertTrue(cleaner.containsComments("SELECT * FROM users /* comment */"));
        assertTrue(cleaner.containsComments("/* comment at start */ SELECT * FROM users"));
        assertTrue(cleaner.containsComments("SELECT * /* middle comment */ FROM users"));
    }

    @Test
    @DisplayName("测试不检测字符串中的注释符号")
    void testDontDetectCommentsInStrings() {
        assertFalse(cleaner.containsComments("SELECT * FROM users WHERE name = 'John -- Smith'"));
        assertFalse(cleaner.containsComments("SELECT * FROM users WHERE desc = 'Test /* data */'"));
        assertFalse(cleaner.containsComments("SELECT * FROM users WHERE name = \"John -- Smith\""));
    }

    @Test
    @DisplayName("测试无注释SQL")
    void testNoComments() {
        assertFalse(cleaner.containsComments("SELECT * FROM users WHERE id = 1"));
        assertFalse(cleaner.containsComments("INSERT INTO users (name) VALUES ('John')"));
        assertFalse(cleaner.containsComments(""));
        assertFalse(cleaner.containsComments(null));
    }

    @Test
    @DisplayName("测试转义字符中的注释符号")
    void testEscapedCommentsInStrings() {
        assertFalse(cleaner.containsComments("SELECT * FROM users WHERE name = 'John\\'s -- test'"));
        assertFalse(cleaner.containsComments("SELECT * FROM users WHERE desc = \"Test \\\"/* data */\\\"\""));
    }

    // ========== removeCommentsOnly方法测试 ==========

    @Test
    @DisplayName("测试只移除注释保留格式")
    void testRemoveCommentsOnly() {
        String sql = "SELECT *\nFROM users -- this is a comment\nWHERE id = 1";
        String result = cleaner.removeCommentsOnly(sql);

        assertFalse(result.contains("this is a comment"));
        assertTrue(result.contains("SELECT *"));
        assertTrue(result.contains("\n")); // 应该保留换行符
    }

    @Test
    @DisplayName("测试移除多行注释保留格式")
    void testRemoveMultiLineCommentsOnly() {
        String sql = "SELECT *\nFROM users /* this is a\nmulti-line comment */\nWHERE id = 1";
        String result = cleaner.removeCommentsOnly(sql);

        assertFalse(result.contains("this is a"));
        assertFalse(result.contains("multi-line comment"));
        assertTrue(result.contains("SELECT *"));
        assertTrue(result.contains("\n")); // 应该保留换行符
    }

    @Test
    @DisplayName("测试保留字符串中的注释符号")
    void testPreserveCommentsInStringsOnly() {
        String sql = "SELECT * FROM users WHERE name = 'John -- not comment' -- real comment";
        String result = cleaner.removeCommentsOnly(sql);

        assertTrue(result.contains("John -- not comment"));
        assertFalse(result.contains("real comment"));
    }

    @Test
    @DisplayName("测试空SQL和null")
    void testRemoveCommentsOnlyNullAndEmpty() {
        assertNull(cleaner.removeCommentsOnly(null));
        assertEquals("", cleaner.removeCommentsOnly(""));
        assertEquals("   ", cleaner.removeCommentsOnly("   "));
    }

    // ========== 边界情况和异常测试 ==========

    @Test
    @DisplayName("测试超长SQL")
    void testVeryLongSql() {
        StringBuilder longSql = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            longSql.append("SELECT * FROM table").append(i).append(" UNION ");
        }
        longSql.append("SELECT * FROM final_table");

        String result = cleaner.cleanSql(longSql.toString());
        assertNotNull(result);
        assertTrue(result.length() > 0);
    }

    @Test
    @DisplayName("测试只有空白字符的SQL")
    void testOnlyWhitespace() {
        String sql = "   \t\n\r   ";
        String result = cleaner.cleanSql(sql);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("测试未闭合的字符串")
    void testUnclosedString() {
        String sql = "SELECT * FROM users WHERE name = 'unclosed string";
        String result = cleaner.cleanSql(sql);
        // 应该能处理未闭合的字符串而不崩溃
        assertNotNull(result);
    }

    @Test
    @DisplayName("测试未闭合的多行注释")
    void testUnclosedMultiLineComment() {
        String sql = "SELECT * FROM users /* unclosed comment";
        String result = cleaner.cleanSql(sql);
        // 应该能处理未闭合的注释而不崩溃
        assertNotNull(result);
    }

    @Test
    @DisplayName("测试特殊字符")
    void testSpecialCharacters() {
        String sql = "SELECT * FROM users WHERE name = '测试用户' -- 中文注释";
        String result = cleaner.cleanSql(sql);

        assertTrue(result.contains("测试用户"));
        assertFalse(result.contains("中文注释"));
    }

    @Test
    @DisplayName("测试Unicode字符")
    void testUnicodeCharacters() {
        String sql = "SELECT * FROM users WHERE emoji = '😀' -- emoji comment";
        String result = cleaner.cleanSql(sql);

        assertTrue(result.contains("😀"));
        assertFalse(result.contains("emoji comment"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "SELECT * FROM users WHERE id = 1",
            "INSERT INTO users (name) VALUES ('John')",
            "UPDATE users SET name = 'Jane' WHERE id = 1",
            "DELETE FROM users WHERE id = 1"
    })
    @DisplayName("测试各种SQL类型的清理")
    void testDifferentSqlTypes(String sql) {
        String result = cleaner.cleanSql(sql);
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    @DisplayName("测试连续的注释符号")
    void testConsecutiveCommentMarkers() {
        String sql = "SELECT * FROM users ---- multiple dashes /* /* nested */ WHERE id = 1";
        String result = cleaner.cleanSql(sql);

        assertTrue(result.contains("SELECT"));
        assertTrue(result.contains("WHERE"));
    }

    @Test
    @DisplayName("测试注释中的SQL关键字")
    void testSqlKeywordsInComments() {
        String sql = "SELECT * FROM users -- SELECT FROM WHERE comment";
        String result = cleaner.cleanSql(sql);

        // 注释中的关键字应该被移除
        String[] parts = result.split("\\s+");
        int selectCount = 0;
        for (String part : parts) {
            if ("SELECT".equalsIgnoreCase(part)) {
                selectCount++;
            }
        }
        assertEquals(1, selectCount); // 应该只有一个SELECT
    }
}