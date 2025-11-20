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
 * Edge cases and exception tests
 */
@DisplayName("Edge Cases and Exception Tests")
public class EdgeCaseTest {

    @Test
    @DisplayName("Empty SQL test")
    public void testEmptySQL() {
        assertThrows(Exception.class, () -> {
            TableFinder.findTables("");
        });

        assertThrows(Exception.class, () -> {
            ParamFinder.find("");
        });
    }

    @Test
    @DisplayName("Null SQL test")
    public void testNullSQL() {
        assertThrows(Exception.class, () -> {
            TableFinder.findTables(null);
        });

        assertThrows(Exception.class, () -> {
            ParamFinder.find(null);
        });
    }

    @Test
    @DisplayName("Invalid SQL test")
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
    @DisplayName("Incomplete SQL test")
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
    @DisplayName("Parameterized test - Various SELECT formats")
    public void testVariousSelectFormats(String sql) {
        assertDoesNotThrow(() -> {
            var tables = TableFinder.findTables(sql);
            assertNotNull(tables);
            assertFalse(tables.isEmpty());
        });
    }

    @Test
    @DisplayName("Special characters and quotes test")
    public void testSpecialCharactersAndQuotes() {
        String sql = """
                SELECT u.`name`, u."email", u.[phone]
                FROM `users` u
                WHERE u.name = 'O''Connor' AND u.description LIKE '%"test"%'
                """;
        assertThrows(SqlIllegalException.class, () -> TableFinder.findTables(sql));
    }

    @Test
    @DisplayName("Mixed case test")
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
    @DisplayName("Very long SQL test")
    public void testVeryLongSQL() {
        StringBuilder sqlBuilder = new StringBuilder("SELECT ");

        // Generate 100 columns
        for (int i = 1; i <= 100; i++) {
            if (i > 1) sqlBuilder.append(", ");
            sqlBuilder.append("u.column").append(i);
        }

        sqlBuilder.append(" FROM users u WHERE ");

        // Generate 100 conditions
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
    @DisplayName("Deep nested subquery test")
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
    @DisplayName("SQL with comments test")
    public void testSQLWithComments() {
        String sql = """
                -- This is a SQL query for user information
                SELECT u.name, u.email /* User basic info */
                FROM users u -- User table
                WHERE u.status = 'active' /* Only active users */
                -- Order by name
                ORDER BY u.name
                """;

        assertDoesNotThrow(() -> {
            var tables = TableFinder.findTables(sql);
            assertEquals(1, tables.size());
            assertEquals("users", tables.iterator().next().getName());
        });
    }
}