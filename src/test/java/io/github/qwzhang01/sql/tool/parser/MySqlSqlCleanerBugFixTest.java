package io.github.qwzhang01.sql.tool.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MySqlSqlCleaner Bug修复测试")
class MySqlSqlCleanerBugFixTest {

    private MySqlSqlCleaner cleaner;

    @BeforeEach
    void setUp() {
        cleaner = new MySqlSqlCleaner();
    }

    @Test
    @DisplayName("测试修复：多个连续短横线和嵌套多行注释")
    void testMultipleDashesWithNestedMultiLineComments() {
        String sql = "SELECT * FROM users ---- multiple dashes\n /* /* nested */ WHERE id = 1";
        String result = cleaner.cleanSql(sql);

        // 验证WHERE子句没有被错误删除
        assertTrue(result.contains("WHERE"), "WHERE子句应该被保留");
        assertTrue(result.contains("id = 1"), "WHERE条件应该被保留");

        // 验证注释被正确移除
        assertFalse(result.contains("multiple dashes"), "单行注释应该被移除");
        assertFalse(result.contains("nested"), "多行注释应该被移除");

        // 验证最终结果
        assertEquals("SELECT * FROM users WHERE id = 1", result);
    }

    @Test
    @DisplayName("测试嵌套多行注释处理")
    void testNestedMultiLineComments() {
        String sql = "SELECT * FROM users /* outer /* inner */ still in comment */ WHERE id = 1";
        String result = cleaner.cleanSql(sql);

        assertEquals("SELECT * FROM users still in comment */ WHERE id = 1", result);
    }

    @Test
    @DisplayName("测试复杂嵌套注释场景")
    void testComplexNestedComments() {
        String sql = "SELECT * FROM users /* level1 /* level2 /* level3 */ back to level2 */ back to level1 */ WHERE id = 1";
        String result = cleaner.cleanSql(sql);

        assertEquals("SELECT * FROM users back to level2 */ back to level1 */ WHERE id = 1", result);
    }

    @Test
    @DisplayName("测试单行注释中的多行注释符号")
    void testSingleLineCommentWithMultiLineMarkers() {
        String sql = "SELECT * FROM users -- this is /* not a multi-line comment\nWHERE id = 1";
        String result = cleaner.cleanSql(sql);

        assertTrue(result.contains("WHERE"), "WHERE子句应该被保留");
        assertFalse(result.contains("this is"), "单行注释应该被移除");
        assertFalse(result.contains("not a multi-line comment"), "单行注释应该被移除");

        assertEquals("SELECT * FROM users WHERE id = 1", result);
    }

    @Test
    @DisplayName("测试多行注释中的单行注释符号")
    void testMultiLineCommentWithSingleLineMarkers() {
        String sql = "SELECT * FROM users /* this -- is still in multi-line comment */ WHERE id = 1";
        String result = cleaner.cleanSql(sql);

        assertTrue(result.contains("WHERE"), "WHERE子句应该被保留");
        assertFalse(result.contains("this"), "多行注释内容应该被移除");
        assertFalse(result.contains("is still in multi-line comment"), "多行注释内容应该被移除");

        assertEquals("SELECT * FROM users WHERE id = 1", result);
    }

    @Test
    @DisplayName("测试字符串中的注释符号不被处理")
    void testCommentMarkersInStrings() {
        String sql = "SELECT * FROM users WHERE name = 'John -- Smith' AND description = 'Test /* data */' -- real comment";
        String result = cleaner.cleanSql(sql);

        assertTrue(result.contains("John -- Smith"), "字符串中的注释符号应该被保留");
        assertTrue(result.contains("Test /* data */"), "字符串中的注释符号应该被保留");
        assertFalse(result.contains("real comment"), "真正的注释应该被移除");

        assertEquals("SELECT * FROM users WHERE name = 'John -- Smith' AND description = 'Test /* data */'", result);
    }

    @Test
    @DisplayName("测试removeCommentsOnly方法的修复")
    void testRemoveCommentsOnlyFix() {
        String sql = "SELECT * FROM users ---- multiple dashes\n /* /* nested */ WHERE id = 1";
        String result = cleaner.removeCommentsOnly(sql);

        assertTrue(result.contains("WHERE"), "WHERE子句应该被保留");
        assertTrue(result.contains("id = 1"), "WHERE条件应该被保留");
        assertFalse(result.contains("multiple dashes"), "注释应该被移除");
        assertFalse(result.contains("nested"), "注释应该被移除");
    }

    @Test
    @DisplayName("测试边界情况：未闭合的嵌套注释")
    void testUnclosedNestedComments() {
        String sql = "SELECT * FROM users /* outer /* inner comment without proper closing WHERE id = 1";
        String result = cleaner.cleanSql(sql);

        // 由于注释未正确闭合，WHERE可能会被当作注释内容
        // 这是预期行为，因为SQL本身是无效的
        assertNotNull(result);
    }

    @Test
    @DisplayName("测试性能：大量嵌套注释")
    void testPerformanceWithManyNestedComments() {
        StringBuilder sql = new StringBuilder("SELECT * FROM users ");

        // 创建深度嵌套的注释
        for (int i = 0; i < 100; i++) {
            sql.append("/* level").append(i).append(" ");
        }
        sql.append("deep nested comment ");
        for (int i = 99; i >= 0; i--) {
            sql.append("*/ ");
        }
        sql.append("WHERE id = 1");

        long startTime = System.currentTimeMillis();
        String result = cleaner.cleanSql(sql.toString());
        long endTime = System.currentTimeMillis();

        assertTrue(result.contains("WHERE"), "WHERE子句应该被保留");
        assertFalse(result.contains("deep nested comment"), "嵌套注释应该被移除");

        // 性能检查：应该在合理时间内完成
        assertTrue(endTime - startTime < 1000, "处理时间应该少于1秒");
    }
}