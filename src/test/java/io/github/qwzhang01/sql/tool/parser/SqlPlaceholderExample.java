/*
package io.github.qwzhang01.sql.tool.parser;

import io.github.qwzhang01.sql.tool.model.SqlParam;
import io.github.qwzhang01.sql.tool.util.SqlPlaceholderParser;

import java.util.List;

*/
/**
 * SQL占位符解析示例
 * 演示如何使用SqlPlaceholderParser解析SQL中的占位符
 *
 * @author Avin Zhang
 * @since 1.0.0
 *//*

public class SqlPlaceholderExample {

    public static void main(String[] args) {
        SqlPlaceholderParser parser = new SqlPlaceholderParser();

        // 示例1: SELECT语句
        System.out.println("=== SELECT语句示例 ===");
        String selectSql = "SELECT u.id, u.name FROM users u WHERE u.age > ? AND u.status = ?";
        List<SqlParam> selectParams = parser.parsePlaceholders(selectSql);
        printResults(selectSql, selectParams);

        // 示例2: INSERT语句
        System.out.println("\n=== INSERT语句示例 ===");
        String insertSql = "INSERT INTO users (name, age, email) VALUES (?, ?, ?)";
        List<SqlParam> insertParams = parser.parsePlaceholders(insertSql);
        printResults(insertSql, insertParams);

        // 示例3: UPDATE语句
        System.out.println("\n=== UPDATE语句示例 ===");
        String updateSql = "UPDATE users SET name = ?, age = ? WHERE id = ?";
        List<SqlParam> updateParams = parser.parsePlaceholders(updateSql);
        printResults(updateSql, updateParams);

        // 示例4: DELETE语句
        System.out.println("\n=== DELETE语句示例 ===");
        String deleteSql = "DELETE FROM users WHERE age < ? AND status = ?";
        List<SqlParam> deleteParams = parser.parsePlaceholders(deleteSql);
        printResults(deleteSql, deleteParams);

        // 示例5: 复杂SELECT语句
        System.out.println("\n=== 复杂SELECT语句示例 ===");
        String complexSql = "SELECT u.id, u.name, p.title FROM users u " +
                "JOIN posts p ON u.id = p.user_id " +
                "WHERE u.age > ? AND p.status = ? " +
                "ORDER BY u.name " +
                "LIMIT ?";
        List<SqlParam> complexParams = parser.parsePlaceholders(complexSql);
        printResults(complexSql, complexParams);

        // 示例6: 带BETWEEN条件的SELECT语句
        System.out.println("\n=== BETWEEN条件示例 ===");
        String betweenSql = "SELECT * FROM users WHERE age BETWEEN ? AND ? AND status = ?";
        List<SqlParam> betweenParams = parser.parsePlaceholders(betweenSql);
        printResults(betweenSql, betweenParams);

        System.out.println("\n=== in 条件示例 ===");
        String inSql = "SELECT * FROM users WHERE age IN (?,?,?) AND status = ?";
        List<SqlParam> inParams = parser.parsePlaceholders(inSql);
        printResults(betweenSql, inParams);
    }

    private static void printResults(String sql, List<SqlParam> params) {
        System.out.println("SQL: " + sql);
        System.out.println("占位符数量: " + params.size());

        for (SqlParam param : params) {
            System.out.printf("  [%d] 类型: %s, 字段: %s, 表名: %s, 表别名: %s%n",
                    param.getIndex(),
                    param.getPlaceholderType(),
                    param.getFieldName(),
                    param.getTableName(),
                    param.getTableAlias());
        }
    }
}*/
