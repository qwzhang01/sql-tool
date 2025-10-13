package io.github.qwzhang01.sql.tool.comprehensive;

import io.github.qwzhang01.sql.tool.helper.ParserHelper;
import io.github.qwzhang01.sql.tool.jsqlparser.visitor.ParamFinder;
import io.github.qwzhang01.sql.tool.jsqlparser.visitor.TableFinder;
import io.github.qwzhang01.sql.tool.model.SqlParam;
import io.github.qwzhang01.sql.tool.model.SqlTable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SQL合并功能测试
 */
@DisplayName("SQL合并功能测试")
public class SqlMergeTest {

    @Test
    @DisplayName("添加JOIN和WHERE子句")
    public void testAddJoinAndWhere() {
        String originalSql = """
                SELECT u.name, u.email
                FROM users u
                WHERE u.status = 'active'
                """;

        String joinClause = "LEFT JOIN user_profiles up ON u.id = up.user_id";
        String whereClause = "up.verified = 1 AND u.created_date >= '2023-01-01'";

        String mergedSql = ParserHelper.addJoinAndWhere(originalSql, joinClause, whereClause);

        assertNotNull(mergedSql);
        assertTrue(mergedSql.contains("LEFT JOIN user_profiles"));
        assertTrue(mergedSql.contains("up.verified = 1"));
        assertTrue(mergedSql.contains("u.status = 'active'"));

        // 验证合并后的SQL仍然可以解析
        Set<SqlTable> tables = TableFinder.findTables(mergedSql);
        assertEquals(2, tables.size());
    }

    @Test
    @DisplayName("只添加JOIN子句")
    public void testAddOnlyJoin() {
        String originalSql = "SELECT * FROM orders o WHERE o.status = 'pending'";
        String joinClause = "INNER JOIN customers c ON o.customer_id = c.id";

        String mergedSql = ParserHelper.addJoinAndWhere(originalSql, joinClause, null);

        assertNotNull(mergedSql);
        assertTrue(mergedSql.contains("INNER JOIN customers"));
        assertTrue(mergedSql.contains("o.status = 'pending'"));

        Set<SqlTable> tables = TableFinder.findTables(mergedSql);
        assertEquals(2, tables.size());
    }

    @Test
    @DisplayName("只添加WHERE子句")
    public void testAddOnlyWhere() {
        String originalSql = "SELECT * FROM products p";
        String whereClause = "p.price > 100 AND p.category_id = ?";

        String mergedSql = ParserHelper.addJoinAndWhere(originalSql, null, whereClause);

        assertNotNull(mergedSql);
        assertTrue(mergedSql.contains("p.price > 100"));

        Set<SqlParam> params = ParamFinder.find(mergedSql);
        assertEquals(1, params.size());
    }

    @Test
    @DisplayName("复杂SQL合并测试")
    public void testComplexSqlMerge() {
        String originalSql = """
                SELECT u.id, u.name, COUNT(o.id) as order_count
                FROM users u
                LEFT JOIN orders o ON u.id = o.user_id
                WHERE u.status = 'active'
                GROUP BY u.id, u.name
                HAVING COUNT(o.id) > 0
                """;

        String joinClause = "LEFT JOIN user_profiles up ON u.id = up.user_id";
        String whereClause = "up.verified = 1 AND o.order_date >= ?";

        String mergedSql = ParserHelper.addJoinAndWhere(originalSql, joinClause, whereClause);

        assertNotNull(mergedSql);
        assertTrue(mergedSql.contains("LEFT JOIN user_profiles"));
        assertTrue(mergedSql.contains("up.verified = 1"));
        assertTrue(mergedSql.contains("GROUP BY"));
        assertTrue(mergedSql.contains("HAVING"));

        Set<SqlTable> tables = TableFinder.findTables(mergedSql);
        assertEquals(3, tables.size());

        Set<SqlParam> params = ParamFinder.find(mergedSql);
        assertEquals(1, params.size());
    }

    @Test
    @DisplayName("带参数的JOIN合并")
    public void testJoinMergeWithParameters() {
        String originalSql = "SELECT * FROM users u WHERE u.age > ?";
        String joinClause = "LEFT JOIN orders o ON u.id = o.user_id AND o.status = ?";

        String mergedSql = ParserHelper.addJoinAndWhere(originalSql, joinClause, null);

        assertNotNull(mergedSql);
        assertTrue(mergedSql.contains("LEFT JOIN orders"));

        Set<SqlParam> params = ParamFinder.find(mergedSql);
        assertEquals(2, params.size());
    }

    @Test
    @DisplayName("多个JOIN合并测试")
    public void testMultipleJoinMerge() {
        String originalSql = "SELECT * FROM users u";
        String joinClause = """
                LEFT JOIN orders o ON u.id = o.user_id
                INNER JOIN products p ON o.product_id = p.id
                """;

        String mergedSql = ParserHelper.addJoinAndWhere(originalSql, joinClause, null);

        assertNotNull(mergedSql);
        assertTrue(mergedSql.contains("LEFT JOIN orders"));
        assertTrue(mergedSql.contains("INNER JOIN products"));

        Set<SqlTable> tables = TableFinder.findTables(mergedSql);
        assertEquals(3, tables.size());
    }

    @Test
    @DisplayName("WHERE子句自动添加WHERE关键字")
    public void testAutoAddWhereKeyword() {
        String originalSql = "SELECT * FROM users u";
        String whereClause = "u.status = 'active' AND u.age > 18";

        String mergedSql = ParserHelper.addJoinAndWhere(originalSql, null, whereClause);

        assertNotNull(mergedSql);
        assertTrue(mergedSql.contains("WHERE u.status = 'active'"));
        assertTrue(mergedSql.contains("u.age > 18"));
    }

    @Test
    @DisplayName("已有WHERE子句的合并")
    public void testMergeWithExistingWhere() {
        String originalSql = "SELECT * FROM users u WHERE u.status = 'active'";
        String whereClause = "u.age > 18 AND u.verified = 1";

        String mergedSql = ParserHelper.addJoinAndWhere(originalSql, null, whereClause);

        assertNotNull(mergedSql);
        assertTrue(mergedSql.contains("u.status = 'active'"));
        assertTrue(mergedSql.contains("u.age > 18"));
        assertTrue(mergedSql.contains("u.verified = 1"));
        // 应该用AND连接原有和新增的WHERE条件
        assertTrue(mergedSql.contains("AND"));
    }
}