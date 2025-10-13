package io.github.qwzhang01.sql.tool.comprehensive;

import io.github.qwzhang01.sql.tool.jsqlparser.visitor.ParamFinder;
import io.github.qwzhang01.sql.tool.jsqlparser.visitor.TableFinder;
import io.github.qwzhang01.sql.tool.model.SqlParam;
import io.github.qwzhang01.sql.tool.model.SqlTable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 复杂SQL解析测试
 */
@DisplayName("复杂SQL解析测试")
public class ComplexSqlParserTest {

    @Test
    @DisplayName("WITH子句测试 - CTE")
    public void testWithClauseQuery() {
        String sql = """
                WITH active_users AS (
                    SELECT id, name FROM users WHERE status = 'active'
                ),
                recent_orders AS (
                    SELECT user_id, COUNT(*) as order_count 
                    FROM orders 
                    WHERE order_date >= '2023-01-01' 
                    GROUP BY user_id
                )
                SELECT au.name, ro.order_count
                FROM active_users au
                LEFT JOIN recent_orders ro ON au.id = ro.user_id
                """;

        Set<SqlTable> tables = TableFinder.findTablesOrOtherSources(sql);

        assertTrue(tables.stream().anyMatch(t -> "users".equals(t.getName())));
        assertTrue(tables.stream().anyMatch(t -> "orders".equals(t.getName())));
    }

    @Test
    @DisplayName("UNION查询测试")
    public void testUnionQuery() {
        String sql = """
                SELECT 'customer' as type, id, name FROM customers WHERE status = 'active'
                UNION ALL
                SELECT 'supplier' as type, id, company_name FROM suppliers WHERE active = 1
                UNION
                SELECT 'employee' as type, id, full_name FROM employees WHERE department_id = ?
                """;

        Set<SqlTable> tables = TableFinder.findTables(sql);
        Set<SqlParam> params = ParamFinder.find(sql);

        assertEquals(3, tables.size());
        assertTrue(tables.stream().anyMatch(t -> "customers".equals(t.getName())));
        assertTrue(tables.stream().anyMatch(t -> "suppliers".equals(t.getName())));
        assertTrue(tables.stream().anyMatch(t -> "employees".equals(t.getName())));
        assertEquals(1, params.size());
    }

    @Test
    @DisplayName("窗口函数测试")
    public void testWindowFunctionQuery() {
        String sql = """
                SELECT 
                    employee_id,
                    salary,
                    department_id,
                    ROW_NUMBER() OVER (PARTITION BY department_id ORDER BY salary DESC) as rank_in_dept,
                    AVG(salary) OVER (PARTITION BY department_id) as avg_dept_salary
                FROM employees e
                JOIN departments d ON e.department_id = d.id
                WHERE e.status = ? AND d.active = 1
                """;

        Set<SqlTable> tables = TableFinder.findTables(sql);
        Set<SqlParam> params = ParamFinder.find(sql);

        assertEquals(2, tables.size());
        assertTrue(tables.stream().anyMatch(t -> "employees".equals(t.getName())));
        assertTrue(tables.stream().anyMatch(t -> "departments".equals(t.getName())));
        assertEquals(1, params.size());
    }

    @Test
    @DisplayName("CASE WHEN表达式测试")
    public void testCaseWhenExpression() {
        String sql = """
                SELECT 
                    u.name,
                    CASE 
                        WHEN u.age < 18 THEN 'Minor'
                        WHEN u.age BETWEEN 18 AND 65 THEN 'Adult'
                        ELSE 'Senior'
                    END as age_group,
                    CASE u.status
                        WHEN 'active' THEN 1
                        WHEN 'inactive' THEN 0
                        ELSE -1
                    END as status_code
                FROM users u
                WHERE u.registration_date >= ?
                """;

        Set<SqlTable> tables = TableFinder.findTables(sql);
        Set<SqlParam> params = ParamFinder.find(sql);

        assertEquals(1, tables.size());
        assertEquals("users", tables.iterator().next().getName());
        assertEquals(1, params.size());
    }

    @Test
    @DisplayName("复杂嵌套查询测试")
    public void testComplexNestedQuery() {
        String sql = """
                SELECT 
                    main.customer_id,
                    main.total_orders,
                    main.avg_amount,
                    recent.recent_orders
                FROM (
                    SELECT 
                        o.customer_id,
                        COUNT(*) as total_orders,
                        AVG(o.total_amount) as avg_amount
                    FROM orders o
                    WHERE o.order_date >= '2023-01-01'
                    GROUP BY o.customer_id
                ) main
                LEFT JOIN (
                    SELECT 
                        customer_id,
                        COUNT(*) as recent_orders
                    FROM orders
                    WHERE order_date >= DATE_SUB(NOW(), INTERVAL 30 DAY)
                    GROUP BY customer_id
                ) recent ON main.customer_id = recent.customer_id
                WHERE main.total_orders > ?
                """;

        Set<SqlTable> tables = TableFinder.findTablesOrOtherSources(sql);

        boolean equals = false;
        for (SqlTable table : tables) {
            equals = "orders".equals(table.getName());
            if (!equals) {
                equals = table.getChildren().stream().anyMatch(t -> "orders".equals(t.getName()));
            }
        }
        assertTrue(equals);

        Set<SqlParam> params = ParamFinder.find(sql);
        assertEquals(1, params.size());
    }

    @Test
    @DisplayName("数据库函数和表达式测试")
    public void testDatabaseFunctionsAndExpressions() {
        String sql = """
                SELECT 
                    u.id,
                    UPPER(u.name) as name_upper,
                    CONCAT(u.first_name, ' ', u.last_name) as full_name,
                    DATE_FORMAT(u.created_date, '%Y-%m-%d') as formatted_date,
                    COALESCE(u.phone, 'N/A') as phone_display,
                    IF(u.age >= 18, 'Adult', 'Minor') as age_category
                FROM users u
                WHERE u.created_date BETWEEN ? AND ?
                AND u.email LIKE CONCAT('%', ?, '%')
                """;

        Set<SqlTable> tables = TableFinder.findTables(sql);
        Set<SqlParam> params = ParamFinder.find(sql);

        assertEquals(1, tables.size());
        assertEquals("users", tables.iterator().next().getName());
        assertEquals(3, params.size());
    }
}