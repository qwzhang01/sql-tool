package io.github.qwzhang01.sql.tool.comprehensive;

import io.github.qwzhang01.sql.tool.jsqlparser.visitor.ParamFinder;
import io.github.qwzhang01.sql.tool.jsqlparser.visitor.TableFinder;
import io.github.qwzhang01.sql.tool.model.SqlParam;
import io.github.qwzhang01.sql.tool.model.SqlTable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 基础SQL解析测试
 */
@DisplayName("基础SQL解析测试")
public class BasicSqlParserTest {

    @Test
    @DisplayName("基础SELECT语句 - 表名提取")
    public void testBasicSelectTableExtraction() {
        String sql = "SELECT id, name FROM users WHERE age > 18";

        Set<SqlTable> tables = TableFinder.findTables(sql);

        assertEquals(1, tables.size());
        SqlTable table = tables.iterator().next();
        assertEquals("users", table.getName());
    }

    @Test
    @DisplayName("带别名的SELECT语句")
    public void testSelectWithAlias() {
        String sql = "SELECT u.id, u.name FROM users u WHERE u.age > 18";

        Set<SqlTable> tables = TableFinder.findTables(sql);

        assertEquals(1, tables.size());
        SqlTable table = tables.iterator().next();
        assertEquals("users", table.getName());
        assertEquals("u", table.getAlias());
    }

    @Test
    @DisplayName("多表JOIN查询")
    public void testMultiTableJoin() {
        String sql = "SELECT u.name, o.order_date, p.product_name " +
                "FROM users u " +
                "INNER JOIN orders o ON u.id = o.user_id " +
                "LEFT JOIN products p ON o.product_id = p.id " +
                "WHERE u.status = 'active'";

        Set<SqlTable> tables = TableFinder.findTables(sql);

        assertEquals(3, tables.size());
        assertTrue(tables.stream().anyMatch(t -> "users".equals(t.getName())));
        assertTrue(tables.stream().anyMatch(t -> "orders".equals(t.getName())));
        assertTrue(tables.stream().anyMatch(t -> "products".equals(t.getName())));
    }

    @Test
    @DisplayName("参数占位符提取")
    public void testParameterExtraction() {
        String sql = "SELECT u.name, u.email " +
                "FROM users u " +
                "WHERE u.age BETWEEN ? AND ? " +
                "AND u.status = ?";

        Set<SqlParam> params = ParamFinder.find(sql);

        assertEquals(3, params.size());
        assertTrue(params.stream().anyMatch(p -> p.getIndex() == 1));
        assertTrue(params.stream().anyMatch(p -> p.getIndex() == 2));
        assertTrue(params.stream().anyMatch(p -> p.getIndex() == 3));
    }

    @Test
    @DisplayName("UPDATE语句解析")
    public void testUpdateStatement() {
        String sql = "UPDATE users u " +
                "SET u.last_login = ?, u.login_count = u.login_count + 1 " +
                "WHERE u.id = ?";

        Set<SqlTable> tables = TableFinder.findTables(sql);
        Set<SqlParam> params = ParamFinder.find(sql);

        assertEquals(1, tables.size());
        assertEquals("users", tables.iterator().next().getName());
        assertEquals(2, params.size());
    }

    @Test
    @DisplayName("DELETE语句解析")
    public void testDeleteStatement() {
        String sql = "DELETE FROM users " +
                "WHERE created_date < ? " +
                "AND status = 'inactive'";

        Set<SqlTable> tables = TableFinder.findTables(sql);
        Set<SqlParam> params = ParamFinder.find(sql);

        assertEquals(1, tables.size());
        assertEquals("users", tables.iterator().next().getName());
        assertEquals(1, params.size());
    }

    @Test
    @DisplayName("INSERT语句解析")
    public void testInsertStatement() {
        String sql = "INSERT INTO audit_log (user_id, action, created_date) " +
                "VALUES (?, 'login', NOW())";

        Set<SqlTable> tables = TableFinder.findTables(sql);
        Set<SqlParam> params = ParamFinder.find(sql);

        assertEquals(1, tables.size());
        assertEquals("audit_log", tables.iterator().next().getName());
        assertEquals(1, params.size());
    }

    @Test
    @DisplayName("子查询表名提取")
    public void testSubqueryTableExtraction() {
        String sql = "SELECT u.name, " +
                "(SELECT COUNT(*) FROM orders o WHERE o.user_id = u.id) as order_count " +
                "FROM users u " +
                "WHERE EXISTS (SELECT 1 FROM subscriptions s WHERE s.user_id = u.id)";

        Set<SqlTable> tables = TableFinder.findTables(sql);

        assertEquals(3, tables.size());
        assertTrue(tables.stream().anyMatch(t -> "users".equals(t.getName())));
        assertTrue(tables.stream().anyMatch(t -> "orders".equals(t.getName())));
        assertTrue(tables.stream().anyMatch(t -> "subscriptions".equals(t.getName())));
    }
}