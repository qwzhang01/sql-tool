package io.github.qwzhang01.sql.tool;

import io.github.qwzhang01.sql.tool.helper.ParserHelper;
import io.github.qwzhang01.sql.tool.jsqlparser.visitor.ParamFinder;
import io.github.qwzhang01.sql.tool.jsqlparser.visitor.TableFinder;
import io.github.qwzhang01.sql.tool.model.SqlParam;
import io.github.qwzhang01.sql.tool.model.SqlTable;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 简单测试用例 - 验证核心功能
 */
public class SimpleTest {

    @Test
    public void testBasicSelectParsing() {
        String sql = "SELECT id, name FROM users WHERE age > 18";

        Set<SqlTable> tables = TableFinder.findTables(sql);

        assertEquals(1, tables.size());
        SqlTable table = tables.iterator().next();
        assertEquals("users", table.getName());
    }

    @Test
    public void testParameterExtraction() {
        String sql = "SELECT * FROM users WHERE age > ? AND status = ?";

        List<SqlParam> params = ParserHelper.getParam(sql);

        assertEquals(2, params.size());
        assertEquals(1, params.get(0).getIndex());
        assertEquals(2, params.get(1).getIndex());
    }

    @Test
    public void testJoinQuery() {
        String sql = "SELECT u.name, o.total FROM users u JOIN orders o ON u.id = o.user_id";

        Set<SqlTable> tables = TableFinder.findTables(sql);

        assertEquals(2, tables.size());
        assertTrue(tables.stream().anyMatch(t -> "users".equals(t.getName())));
        assertTrue(tables.stream().anyMatch(t -> "orders".equals(t.getName())));
    }

    @Test
    public void testUpdateStatement() {
        String sql = "UPDATE users SET name = ? WHERE id = ?";

        Set<SqlTable> tables = TableFinder.findTables(sql);
        Set<SqlParam> params = ParamFinder.find(sql);

        assertEquals(1, tables.size());
        assertEquals("users", tables.iterator().next().getName());
        assertEquals(2, params.size());
    }

    @Test
    public void testInsertStatement() {
        String sql = "INSERT INTO users (name, email) VALUES (?, ?)";

        Set<SqlTable> tables = TableFinder.findTables(sql);
        Set<SqlParam> params = ParamFinder.find(sql);

        assertEquals(1, tables.size());
        assertEquals("users", tables.iterator().next().getName());
        assertEquals(2, params.size());
    }

    @Test
    public void testDeleteStatement() {
        String sql = "DELETE FROM users WHERE id = ?";

        Set<SqlTable> tables = TableFinder.findTables(sql);
        List<SqlParam> params = ParserHelper.getParam(sql);

        assertEquals(1, tables.size());
        assertEquals("users", tables.iterator().next().getName());
        assertEquals(1, params.size());
    }

    @Test
    public void testSubquery() {
        String sql = "SELECT * FROM users WHERE id IN (SELECT user_id FROM orders WHERE total > 100)";

        Set<SqlTable> tables = TableFinder.findTables(sql);

        assertEquals(2, tables.size());
        assertTrue(tables.stream().anyMatch(t -> "users".equals(t.getName())));
        assertTrue(tables.stream().anyMatch(t -> "orders".equals(t.getName())));
    }

    @Test
    public void testComplexJoin() {
        String sql = "SELECT u.name, p.title FROM users u " +
                "LEFT JOIN user_profiles p ON u.id = p.user_id " +
                "WHERE u.status = ? AND p.verified = ?";

        Set<SqlTable> tables = TableFinder.findTables(sql);
        Set<SqlParam> params = ParamFinder.find(sql);

        assertEquals(2, tables.size());
        assertEquals(2, params.size());
    }

    @Test
    public void testSqlMerge() {
        String originalSql = "SELECT * FROM users WHERE status = 'active'";
        String joinClause = "LEFT JOIN orders o ON users.id = o.user_id";
        String whereClause = "o.total > 100";

        String mergedSql = ParserHelper.addJoinAndWhere(originalSql, joinClause, whereClause);

        assertNotNull(mergedSql);
        assertTrue(mergedSql.contains("LEFT JOIN orders"));
        assertTrue(mergedSql.contains("o.total > 100"));
    }

    @Test
    public void testTableAlias() {
        String sql = "SELECT u.id, u.name FROM users AS u WHERE u.age > 25";

        Set<SqlTable> tables = TableFinder.findTables(sql);

        assertEquals(1, tables.size());
        SqlTable table = tables.iterator().next();
        assertEquals("users", table.getName());
        assertEquals("u", table.getAlias());
    }
}