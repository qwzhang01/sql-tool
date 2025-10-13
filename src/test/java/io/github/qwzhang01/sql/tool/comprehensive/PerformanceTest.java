package io.github.qwzhang01.sql.tool.comprehensive;

import io.github.qwzhang01.sql.tool.helper.ParserHelper;
import io.github.qwzhang01.sql.tool.jsqlparser.visitor.ParamFinder;
import io.github.qwzhang01.sql.tool.jsqlparser.visitor.TableFinder;
import io.github.qwzhang01.sql.tool.model.SqlParam;
import io.github.qwzhang01.sql.tool.model.SqlTable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 性能测试
 */
@DisplayName("性能测试")
public class PerformanceTest {

    @Test
    @DisplayName("大型复杂查询性能测试")
    @Timeout(value = 2, unit = TimeUnit.SECONDS)
    public void testLargeComplexQueryPerformance() {
        String complexSql = """
                WITH RECURSIVE category_hierarchy AS (
                    SELECT id, name, parent_id, 0 as level
                    FROM categories
                    WHERE parent_id IS NULL
                    UNION ALL
                    SELECT c.id, c.name, c.parent_id, ch.level + 1
                    FROM categories c
                    INNER JOIN category_hierarchy ch ON c.parent_id = ch.id
                ),
                sales_summary AS (
                    SELECT 
                        p.category_id,
                        SUM(oi.quantity * oi.price) as total_sales,
                        COUNT(DISTINCT o.id) as order_count,
                        AVG(oi.price) as avg_price
                    FROM products p
                    JOIN order_items oi ON p.id = oi.product_id
                    JOIN orders o ON oi.order_id = o.id
                    WHERE o.order_date >= ? AND o.status = 'completed'
                    GROUP BY p.category_id
                )
                SELECT 
                    ch.name as category_name,
                    ch.level,
                    COALESCE(ss.total_sales, 0) as sales,
                    COALESCE(ss.order_count, 0) as orders,
                    COALESCE(ss.avg_price, 0) as avg_price,
                    ROW_NUMBER() OVER (PARTITION BY ch.level ORDER BY COALESCE(ss.total_sales, 0) DESC) as rank_in_level
                FROM category_hierarchy ch
                LEFT JOIN sales_summary ss ON ch.id = ss.category_id
                ORDER BY ch.level, sales DESC
                """;

        long startTime = System.currentTimeMillis();

        Set<SqlTable> tables = TableFinder.findTablesOrOtherSources(complexSql);
        Set<SqlParam> params = ParamFinder.find(complexSql);

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // 验证结果正确性
        assertTrue(tables.stream().anyMatch(t -> "categories".equals(t.getName())));
        assertTrue(tables.stream().anyMatch(t -> "products".equals(t.getName())));
        assertTrue(tables.stream().anyMatch(t -> "order_items".equals(t.getName())));
        assertTrue(tables.stream().anyMatch(t -> "orders".equals(t.getName())));
        assertEquals(1, params.size());

        // 性能断言（应该在1秒内完成）
        assertTrue(duration < 1000, "解析时间过长: " + duration + "ms");
    }

    @Test
    @DisplayName("批量SQL解析性能测试")
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    public void testBatchSqlParsingPerformance() {
        String[] sqls = {
                "SELECT * FROM users WHERE id = ?",
                "SELECT u.name, o.total FROM users u JOIN orders o ON u.id = o.user_id WHERE o.status = ?",
                "UPDATE users SET last_login = ? WHERE id = ?",
                "DELETE FROM sessions WHERE created_date < ?",
                "INSERT INTO audit_log (action, user_id, timestamp) VALUES (?, ?, ?)"
        };

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < 1000; i++) {
            for (String sql : sqls) {
                TableFinder.findTables(sql);
                ParamFinder.find(sql);
            }
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // 5000次解析应该在5秒内完成
        assertTrue(duration < 5000, "批量解析时间过长: " + duration + "ms");

        double avgTime = (double) duration / 5000;
        assertTrue(avgTime < 1.0, "平均解析时间过长: " + avgTime + "ms");
    }

    @Test
    @DisplayName("SQL合并性能测试")
    @Timeout(value = 1, unit = TimeUnit.SECONDS)
    public void testSqlMergePerformance() {
        String baseSql = """
                SELECT u.id, u.name, u.email, COUNT(o.id) as order_count
                FROM users u
                LEFT JOIN orders o ON u.id = o.user_id
                WHERE u.status = 'active'
                GROUP BY u.id, u.name, u.email
                HAVING COUNT(o.id) > 0
                ORDER BY u.name
                """;

        String joinClause = "LEFT JOIN user_profiles up ON u.id = up.user_id";
        String whereClause = "up.verified = 1 AND u.created_date >= ?";

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < 100; i++) {
            String mergedSql = ParserHelper.addJoinAndWhere(baseSql, joinClause, whereClause);
            assertNotNull(mergedSql);
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // 100次合并应该在1秒内完成
        assertTrue(duration < 1000, "SQL合并时间过长: " + duration + "ms");
    }

    @Test
    @DisplayName("内存使用测试")
    public void testMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();

        // 记录初始内存使用
        runtime.gc(); // 强制垃圾回收
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();

        // 执行大量SQL解析操作
        String sql = """
                SELECT u.id, u.name, p.title, a.street, o.total, oi.quantity
                FROM users u
                LEFT JOIN user_profiles p ON u.id = p.user_id
                LEFT JOIN addresses a ON u.id = a.user_id
                LEFT JOIN orders o ON u.id = o.user_id
                LEFT JOIN order_items oi ON o.id = oi.order_id
                WHERE u.status = ? AND o.order_date >= ?
                """;

        for (int i = 0; i < 1000; i++) {
            TableFinder.findTables(sql);
            ParamFinder.find(sql);
        }

        // 记录操作后内存使用
        runtime.gc(); // 强制垃圾回收
        long finalMemory = runtime.totalMemory() - runtime.freeMemory();

        long memoryIncrease = finalMemory - initialMemory;
        long memoryIncreaseMB = memoryIncrease / (1024 * 1024);

        // 内存增长不应该超过50MB
        assertTrue(memoryIncreaseMB < 50, "内存使用增长过多: " + memoryIncreaseMB + "MB");
    }

    @Test
    @DisplayName("并发解析测试")
    @Timeout(value = 3, unit = TimeUnit.SECONDS)
    public void testConcurrentParsing() throws InterruptedException {
        String sql = """
                SELECT u.name, COUNT(o.id) as order_count
                FROM users u
                LEFT JOIN orders o ON u.id = o.user_id
                WHERE u.status = ? AND o.order_date >= ?
                GROUP BY u.id, u.name
                """;

        int threadCount = 10;
        Thread[] threads = new Thread[threadCount];
        boolean[] results = new boolean[threadCount];

        for (int i = 0; i < threadCount; i++) {
            final int threadIndex = i;
            threads[i] = new Thread(() -> {
                try {
                    for (int j = 0; j < 100; j++) {
                        Set<SqlTable> tables = TableFinder.findTables(sql);
                        Set<SqlParam> params = ParamFinder.find(sql);

                        // 验证结果
                        if (tables.size() != 2 || params.size() != 2) {
                            results[threadIndex] = false;
                            return;
                        }
                    }
                    results[threadIndex] = true;
                } catch (Exception e) {
                    results[threadIndex] = false;
                }
            });
        }

        long startTime = System.currentTimeMillis();

        // 启动所有线程
        for (Thread thread : threads) {
            thread.start();
        }

        // 等待所有线程完成
        for (Thread thread : threads) {
            thread.join();
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // 验证所有线程都成功完成
        for (int i = 0; i < threadCount; i++) {
            assertTrue(results[i], "线程 " + i + " 执行失败");
        }

        // 并发执行应该在3秒内完成
        assertTrue(duration < 3000, "并发解析时间过长: " + duration + "ms");
    }
}