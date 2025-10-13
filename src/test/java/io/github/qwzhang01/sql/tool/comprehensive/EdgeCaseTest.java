package io.github.qwzhang01.sql.tool.comprehensive;

import io.github.qwzhang01.sql.tool.exception.SqlIllegalException;
import io.github.qwzhang01.sql.tool.jsqlparser.visitor.ParamFinder;
import io.github.qwzhang01.sql.tool.jsqlparser.visitor.TableFinder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 边界情况和异常测试
 */
@DisplayName("边界情况和异常测试")
public class EdgeCaseTest {

    @Test
    @DisplayName("空SQL测试")
    public void testEmptySQL() {
        assertThrows(Exception.class, () -> {
            TableFinder.findTables("");
        });

        assertThrows(Exception.class, () -> {
            ParamFinder.find("");
        });
    }

    @Test
    @DisplayName("null SQL测试")
    public void testNullSQL() {
        assertThrows(Exception.class, () -> {
            TableFinder.findTables(null);
        });

        assertThrows(Exception.class, () -> {
            ParamFinder.find(null);
        });
    }

    @Test
    @DisplayName("无效SQL测试")
    public void testInvalidSQL() {
        String invalidSql = "INVALID SQL STATEMENT";

        assertThrows(Exception.class, () -> {
            TableFinder.findTables(invalidSql);
        });

        assertThrows(Exception.class, () -> {
            ParamFinder.find(invalidSql);
        });
    }

    @Test
    @DisplayName("不完整SQL测试")
    public void testIncompleteSQL() {
        String incompleteSql = "SELECT * FROM";

        assertThrows(Exception.class, () -> {
            TableFinder.findTables(incompleteSql);
        });
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "SELECT * FROM table1",
            "select id, name from users where age > 18",
            "SELECT DISTINCT category FROM products ORDER BY category",
            "SELECT COUNT(*) FROM orders GROUP BY customer_id HAVING COUNT(*) > 5"
    })
    @DisplayName("参数化测试 - 各种SELECT语句格式")
    public void testVariousSelectFormats(String sql) {
        assertDoesNotThrow(() -> {
            var tables = TableFinder.findTables(sql);
            assertNotNull(tables);
            assertFalse(tables.isEmpty());
        });
    }

    @Test
    @DisplayName("特殊字符和引号测试")
    public void testSpecialCharactersAndQuotes() {
        String sql = """
                SELECT u.`name`, u."email", u.[phone]
                FROM `users` u
                WHERE u.name = 'O''Connor' AND u.description LIKE '%"test"%'
                """;
        assertThrows(SqlIllegalException.class, () -> TableFinder.findTables(sql));
    }

    @Test
    @DisplayName("大小写混合测试")
    public void testMixedCase() {
        String sql = """
                Select U.Name, U.Email
                From Users U
                Where U.Status = 'Active'
                Order By U.Name
                """;

        assertDoesNotThrow(() -> {
            var tables = TableFinder.findTables(sql);
            assertEquals(1, tables.size());
            assertEquals("Users", tables.iterator().next().getName());
        });
    }

    @Test
    @DisplayName("超长SQL测试")
    public void testVeryLongSQL() {
        StringBuilder sqlBuilder = new StringBuilder("SELECT ");

        // 生成100个列
        for (int i = 1; i <= 100; i++) {
            if (i > 1) sqlBuilder.append(", ");
            sqlBuilder.append("u.column").append(i);
        }

        sqlBuilder.append(" FROM users u WHERE ");

        // 生成100个条件
        for (int i = 1; i <= 100; i++) {
            if (i > 1) sqlBuilder.append(" AND ");
            sqlBuilder.append("u.field").append(i).append(" = ?");
        }

        String longSql = sqlBuilder.toString();

        assertDoesNotThrow(() -> {
            var tables = TableFinder.findTables(longSql);
            var params = ParamFinder.find(longSql);

            assertEquals(1, tables.size());
            assertEquals(100, params.size());
        });
    }

    @Test
    @DisplayName("深度嵌套子查询测试")
    public void testDeeplyNestedSubqueries() {
        String sql = """
                SELECT u.name
                FROM users u
                WHERE u.id IN (
                    SELECT o.user_id
                    FROM orders o
                    WHERE o.id IN (
                        SELECT oi.order_id
                        FROM order_items oi
                        WHERE oi.product_id IN (
                            SELECT p.id
                            FROM products p
                            WHERE p.category_id IN (
                                SELECT c.id
                                FROM categories c
                                WHERE c.parent_id = ?
                            )
                        )
                    )
                )
                """;

        assertDoesNotThrow(() -> {
            var tables = TableFinder.findTables(sql);
            var params = ParamFinder.find(sql);

            assertEquals(5, tables.size());
            assertEquals(1, params.size());
        });
    }

    @Test
    @DisplayName("注释SQL测试")
    public void testSQLWithComments() {
        String sql = """
                -- 这是一个查询用户信息的SQL
                SELECT u.name, u.email /* 用户基本信息 */
                FROM users u -- 用户表
                WHERE u.status = 'active' /* 只查询活跃用户 */
                -- 按名称排序
                ORDER BY u.name
                """;

        assertDoesNotThrow(() -> {
            var tables = TableFinder.findTables(sql);
            assertEquals(1, tables.size());
            assertEquals("users", tables.iterator().next().getName());
        });
    }
}