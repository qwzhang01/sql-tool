package io.github.qwzhang01.sql.tool.comprehensive;

import io.github.qwzhang01.sql.tool.helper.ParserHelper;
import io.github.qwzhang01.sql.tool.jsqlparser.visitor.ParamFinder;
import io.github.qwzhang01.sql.tool.jsqlparser.visitor.TableFinder;
import io.github.qwzhang01.sql.tool.model.SqlParam;
import io.github.qwzhang01.sql.tool.model.SqlTable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;


import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Performance tests
 */
@DisplayName("Performance Tests")
public class PerformanceTest {

    @Test
    @DisplayName("Large complex query performance test")
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

        // Verify result correctness
        assertTrue(tables.stream().anyMatch(t -> "categories".equals(t.getName())));
        assertTrue(tables.stream().anyMatch(t -> "products".equals(t.getName())));
        assertTrue(tables.stream().anyMatch(t -> "order_items".equals(t.getName())));
        assertTrue(tables.stream().anyMatch(t -> "orders".equals(t.getName())));
        assertEquals(1, params.size());

        // Performance assertion (should complete within 1 second)
        assertTrue(duration < 1000, "Parsing took too long: " + duration + "ms");
    }

    @Test
    @DisplayName("Batch SQL parsing performance test")
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

        // 5000 parses should complete within 5 seconds
        assertTrue(duration < 5000, "Batch parsing took too long: " + duration + "ms");

        double avgTime = (double) duration / 5000;
        assertTrue(avgTime < 1.0, "Average parsing time too long: " + avgTime + "ms");
    }

    @Test
    @DisplayName("SQL merge performance test")
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

        // 100 merges should complete within 1 second
        assertTrue(duration < 1000, "SQL merge took too long: " + duration + "ms");
    }

    @Test
    @DisplayName("Memory usage test")
    public void testMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();

        // Record initial memory usage
        runtime.gc(); // Force garbage collection
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();

        // Execute many SQL parsing operations
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

        // Record memory usage after operations
        runtime.gc(); // Force garbage collection
        long finalMemory = runtime.totalMemory() - runtime.freeMemory();

        long memoryIncrease = finalMemory - initialMemory;
        long memoryIncreaseMB = memoryIncrease / (1024 * 1024);

        // Memory growth should not exceed 50MB
        assertTrue(memoryIncreaseMB < 50, "Memory usage increased too much: " + memoryIncreaseMB + "MB");
    }

    @Test
    @DisplayName("Concurrent parsing test")
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

                        // Verify result
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

        // Start all threads
        for (Thread thread : threads) {
            thread.start();
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // Verify all threads completed successfully
        for (int i = 0; i < threadCount; i++) {
            assertTrue(results[i], "Thread " + i + " failed");
        }

        // Concurrent execution should complete within 3 seconds
        assertTrue(duration < 3000, "Concurrent parsing took too long: " + duration + "ms");
    }
}