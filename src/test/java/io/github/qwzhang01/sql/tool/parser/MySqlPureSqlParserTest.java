package io.github.qwzhang01.sql.tool.parser;

import io.github.qwzhang01.sql.tool.exception.UnSuportedException;
import io.github.qwzhang01.sql.tool.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MySqlPureSqlParser 测试")
class MySqlPureSqlParserTest {

    private MySqlPureSqlParser parser;

    @BeforeEach
    void setUp() {
        parser = new MySqlPureSqlParser();
    }

    // ========== 基础功能测试 ==========

    @Test
    @DisplayName("测试空SQL和null")
    void testNullAndEmptySql() {
        assertNull(parser.parse(null));
        assertNull(parser.parse(""));
        assertNull(parser.parse("   "));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    @DisplayName("测试各种空白字符SQL")
    void testWhitespaceSql(String sql) {
        SqlInfo result = parser.parse(sql);
        assertNull(result);
    }

    @Test
    @DisplayName("测试带参数的parse方法")
    void testParseWithParameters() {
        String sql = "SELECT * FROM users WHERE id = ?";
        Map<String, Object> params = new HashMap<>();
        params.put("id", 1);

        SqlInfo result = parser.parse(sql, params);
        assertNotNull(result);
        assertEquals(params, result.getParameterMap());
    }

    @Test
    @DisplayName("测试不支持的SQL类型")
    void testUnsupportedSqlType() {
        assertThrows(UnSuportedException.class, () -> {
            parser.parse("CREATE TABLE test (id INT)");
        });

        assertThrows(UnSuportedException.class, () -> {
            parser.parse("DROP TABLE test");
        });

        assertThrows(UnSuportedException.class, () -> {
            parser.parse("ALTER TABLE test ADD COLUMN name VARCHAR(50)");
        });
    }

    // ========== SELECT语句测试 ==========
    @Test
    @DisplayName("测试基础SELECT语句")
    void testBasicSelect() {
        String sql = "SELECT * FROM users";
        SqlInfo result = parser.parse(sql);

        assertNotNull(result);
        assertEquals(SqlInfo.SqlType.SELECT, result.getSqlType());
        assertEquals("users", result.getMainTable().getTableName());
        assertNull(result.getMainTable().getAlias());
    }

    @Test
    @DisplayName("测试基础SELECT语句")
    void testQuotSelect() {
        String sql = "SELECT * FROM user_table  LEFT JOIN `order_table` ON `user_table`.`user_id` = `order_table`.`user_id`";

        SqlInfo result = parser.parse(sql);

        assertNotNull(result);
        assertEquals(SqlInfo.SqlType.SELECT, result.getSqlType());
        assertEquals("users", result.getMainTable().getTableName());
        assertNull(result.getMainTable().getAlias());
    }

    @Test
    @DisplayName("测试SELECT指定字段")
    void testSelectSpecificColumns() {
        String sql = "SELECT id, name, email FROM users";
        SqlInfo result = parser.parse(sql);

        assertNotNull(result);
        assertEquals(3, result.getSelectColumns().size());
        assertEquals("id", result.getSelectColumns().get(0).getColumnName());
        assertEquals("name", result.getSelectColumns().get(1).getColumnName());
        assertEquals("email", result.getSelectColumns().get(2).getColumnName());
    }

    @Test
    @DisplayName("测试SELECT带别名")
    void testSelectWithAlias() {
        String sql = "SELECT u.id, u.name AS user_name, email alias_email FROM users u";
        SqlInfo result = parser.parse(sql);

        assertNotNull(result);
        assertEquals(3, result.getSelectColumns().size());

        ColumnInfo col1 = result.getSelectColumns().get(0);
        assertEquals("id", col1.getColumnName());
        assertEquals("u", col1.getTableAlias());

        ColumnInfo col2 = result.getSelectColumns().get(1);
        assertEquals("name", col2.getColumnName());
        assertEquals("user_name", col2.getAlias());
        assertEquals("u", col2.getTableAlias());

        ColumnInfo col3 = result.getSelectColumns().get(2);
        assertEquals("email", col3.getColumnName());
        assertEquals("alias_email", col3.getAlias());

        assertEquals("users", result.getMainTable().getTableName());
        assertEquals("u", result.getMainTable().getAlias());
    }

    @Test
    @DisplayName("测试复杂SELECT语句")
    void testComplexSelect() {
        String sql = "SELECT u.id, u.name, p.title FROM users u " +
                "LEFT JOIN posts p ON u.id = p.user_id " +
                "WHERE u.status = 'active' AND p.published = 1 " +
                "GROUP BY u.id " +
                "HAVING COUNT(p.id) > 0 " +
                "ORDER BY u.name ASC, p.created_at DESC " +
                "LIMIT 10 OFFSET 5";

        SqlInfo result = parser.parse(sql);

        assertNotNull(result);
        assertEquals(SqlInfo.SqlType.SELECT, result.getSqlType());

        // 验证主表
        assertEquals("users", result.getMainTable().getTableName());
        assertEquals("u", result.getMainTable().getAlias());

        // 验证JOIN
        assertEquals(1, result.getJoinTables().size());
        JoinInfo join = result.getJoinTables().get(0);
        assertEquals(JoinInfo.JoinType.LEFT_JOIN, join.getJoinType());
        assertEquals("posts", join.getTableName());
        assertEquals("p", join.getAlias());
        assertEquals("u.id = p.user_id", join.getCondition());

        // 验证WHERE条件
        assertEquals(2, result.getWhereConditions().size());

        // 验证GROUP BY
        assertEquals(1, result.getGroupByColumns().size());
        assertEquals("u.id", result.getGroupByColumns().get(0));

        // 验证HAVING
        assertEquals("COUNT(p.id) > 0", result.getHavingCondition());

        // 验证ORDER BY
        assertEquals(2, result.getOrderByColumns().size());
        assertEquals("u.name", result.getOrderByColumns().get(0).getColumnName());
        assertEquals(OrderByInfo.Direction.ASC, result.getOrderByColumns().get(0).getDirection());
        assertEquals("p.created_at", result.getOrderByColumns().get(1).getColumnName());
        assertEquals(OrderByInfo.Direction.DESC, result.getOrderByColumns().get(1).getDirection());

        // 验证LIMIT
        assertNotNull(result.getLimitInfo());
        assertEquals(10, result.getLimitInfo().getLimit());
        assertEquals(5, result.getLimitInfo().getOffset());
    }

    @Test
    @DisplayName("测试各种JOIN类型")
    void testDifferentJoinTypes() {
        String[] joinSqls = {
                "SELECT * FROM users u INNER JOIN posts p ON u.id = p.user_id",
                "SELECT * FROM users u LEFT JOIN posts p ON u.id = p.user_id",
                "SELECT * FROM users u RIGHT JOIN posts p ON u.id = p.user_id",
                "SELECT * FROM users u FULL OUTER JOIN posts p ON u.id = p.user_id",
                "SELECT * FROM users u CROSS JOIN posts p",
                "SELECT * FROM users u JOIN posts p ON u.id = p.user_id"
        };

        JoinInfo.JoinType[] expectedTypes = {
                JoinInfo.JoinType.INNER_JOIN,
                JoinInfo.JoinType.LEFT_JOIN,
                JoinInfo.JoinType.RIGHT_JOIN,
                JoinInfo.JoinType.FULL_JOIN,
                JoinInfo.JoinType.CROSS_JOIN,
                JoinInfo.JoinType.INNER_JOIN
        };

        for (int i = 0; i < joinSqls.length; i++) {
            SqlInfo result = parser.parse(joinSqls[i]);
            assertNotNull(result);
            assertEquals(1, result.getJoinTables().size());
            assertEquals(expectedTypes[i], result.getJoinTables().get(0).getJoinType());
        }
    }

    @Test
    @DisplayName("测试多个JOIN")
    void testMultipleJoins() {
        String sql = "SELECT * FROM users u " +
                "LEFT JOIN posts p ON u.id = p.user_id " +
                "INNER JOIN categories c ON p.category_id = c.id";

        SqlInfo result = parser.parse(sql);
        assertNotNull(result);
        assertEquals(2, result.getJoinTables().size());

        assertEquals(JoinInfo.JoinType.LEFT_JOIN, result.getJoinTables().get(0).getJoinType());
        assertEquals("posts", result.getJoinTables().get(0).getTableName());

        assertEquals(JoinInfo.JoinType.INNER_JOIN, result.getJoinTables().get(1).getJoinType());
        assertEquals("categories", result.getJoinTables().get(1).getTableName());
    }

    // ========== WHERE条件测试 ==========

    @Test
    @DisplayName("测试基本WHERE条件")
    void testBasicWhereConditions() {
        String sql = "SELECT * FROM users WHERE id = 1";
        SqlInfo result = parser.parse(sql);

        assertNotNull(result);
        assertEquals(1, result.getWhereConditions().size());

        WhereCondition condition = result.getWhereConditions().get(0);
        assertEquals("id", condition.getLeftOperand());
        assertEquals("=", condition.getOperator());
        assertEquals("1", condition.getRightOperand());
    }

    @Test
    @DisplayName("测试各种比较操作符")
    void testComparisonOperators() {
        String[] operators = {"=", ">", "<", ">=", "<=", "!=", "<>"};

        for (String op : operators) {
            String sql = "SELECT * FROM users WHERE age " + op + " 25";
            SqlInfo result = parser.parse(sql);

            assertNotNull(result);
            assertEquals(1, result.getWhereConditions().size());
            assertEquals(op, result.getWhereConditions().get(0).getOperator());
        }
    }

    @Test
    @DisplayName("测试LIKE条件")
    void testLikeConditions() {
        String sql = "SELECT * FROM users WHERE name LIKE '%john%'";
        SqlInfo result = parser.parse(sql);

        assertNotNull(result);
        assertEquals(1, result.getWhereConditions().size());

        WhereCondition condition = result.getWhereConditions().get(0);
        assertEquals("name", condition.getLeftOperand());
        assertEquals("LIKE", condition.getOperator());
        assertEquals("'%john%'", condition.getRightOperand());
    }

    @Test
    @DisplayName("测试NOT LIKE条件")
    void testNotLikeConditions() {
        String sql = "SELECT * FROM users WHERE name NOT LIKE '%admin%'";
        SqlInfo result = parser.parse(sql);

        assertNotNull(result);
        assertEquals(1, result.getWhereConditions().size());

        WhereCondition condition = result.getWhereConditions().get(0);
        assertEquals("name", condition.getLeftOperand());
        assertEquals("NOT LIKE", condition.getOperator());
        assertEquals("'%admin%'", condition.getRightOperand());
    }

    @Test
    @DisplayName("测试IN条件")
    void testInConditions() {
        String sql = "SELECT * FROM users WHERE id IN (1, 2, 3)";
        SqlInfo result = parser.parse(sql);

        assertNotNull(result);
        assertEquals(1, result.getWhereConditions().size());

        WhereCondition condition = result.getWhereConditions().get(0);
        assertEquals("id", condition.getLeftOperand());
        assertEquals("IN", condition.getOperator());
        assertEquals("(1, 2, 3)", condition.getRightOperand());
    }

    @Test
    @DisplayName("测试NOT IN条件")
    void testNotInConditions() {
        String sql = "SELECT * FROM users WHERE status NOT IN ('deleted', 'banned')";
        SqlInfo result = parser.parse(sql);

        assertNotNull(result);
        assertEquals(1, result.getWhereConditions().size());

        WhereCondition condition = result.getWhereConditions().get(0);
        assertEquals("status", condition.getLeftOperand());
        assertEquals("NOT IN", condition.getOperator());
        assertEquals("('deleted', 'banned')", condition.getRightOperand());
    }

    @Test
    @DisplayName("测试BETWEEN条件")
    void testBetweenConditions() {
        String sql = "SELECT * FROM users WHERE age BETWEEN 18 AND 65";
        SqlInfo result = parser.parse(sql);

        assertNotNull(result);
        assertEquals(1, result.getWhereConditions().size());

        WhereCondition condition = result.getWhereConditions().get(0);
        assertEquals("age", condition.getLeftOperand());
        assertEquals("BETWEEN", condition.getOperator());
        assertEquals("18 AND 65", condition.getRightOperand());
    }

    @Test
    @DisplayName("测试NOT BETWEEN条件")
    void testNotBetweenConditions() {
        String sql = "SELECT * FROM users WHERE age NOT BETWEEN 0 AND 17";
        SqlInfo result = parser.parse(sql);

        assertNotNull(result);
        assertEquals(1, result.getWhereConditions().size());

        WhereCondition condition = result.getWhereConditions().get(0);
        assertEquals("age", condition.getLeftOperand());
        assertEquals("NOT BETWEEN", condition.getOperator());
        assertEquals("0 AND 17", condition.getRightOperand());
    }

    @Test
    @DisplayName("测试IS NULL条件")
    void testIsNullConditions() {
        String sql = "SELECT * FROM users WHERE deleted_at IS NULL";
        SqlInfo result = parser.parse(sql);

        assertNotNull(result);
        assertEquals(1, result.getWhereConditions().size());

        WhereCondition condition = result.getWhereConditions().get(0);
        assertEquals("deleted_at", condition.getLeftOperand());
        assertEquals("IS NULL", condition.getOperator());
        assertNull(condition.getRightOperand());
    }

    @Test
    @DisplayName("测试IS NOT NULL条件")
    void testIsNotNullConditions() {
        String sql = "SELECT * FROM users WHERE email IS NOT NULL";
        SqlInfo result = parser.parse(sql);

        assertNotNull(result);
        assertEquals(1, result.getWhereConditions().size());

        WhereCondition condition = result.getWhereConditions().get(0);
        assertEquals("email", condition.getLeftOperand());
        assertEquals("IS NOT NULL", condition.getOperator());
        assertNull(condition.getRightOperand());
    }

    @Test
    @DisplayName("测试复杂WHERE条件（AND/OR）")
    void testComplexWhereConditions() {
        String sql = "SELECT * FROM users WHERE (status = 'active' AND age > 18) OR (role = 'admin')";
        SqlInfo result = parser.parse(sql);

        assertNotNull(result);
        assertTrue(result.getWhereConditions().size() >= 2);
    }

    @Test
    @DisplayName("测试带括号的WHERE条件")
    void testWhereConditionsWithParentheses() {
        String sql = "SELECT * FROM users WHERE (id = 1)";
        SqlInfo result = parser.parse(sql);

        assertNotNull(result);
        assertEquals(1, result.getWhereConditions().size());

        WhereCondition condition = result.getWhereConditions().get(0);
        assertEquals("id", condition.getLeftOperand());
        assertEquals("=", condition.getOperator());
        assertEquals("1", condition.getRightOperand());
    }

    // ========== LIMIT测试 ==========

    @Test
    @DisplayName("测试简单LIMIT")
    void testSimpleLimit() {
        String sql = "SELECT * FROM users LIMIT 10";
        SqlInfo result = parser.parse(sql);

        assertNotNull(result);
        assertNotNull(result.getLimitInfo());
        assertEquals(10, result.getLimitInfo().getLimit());
        assertEquals(0, result.getLimitInfo().getOffset());
    }

    @Test
    @DisplayName("测试LIMIT OFFSET")
    void testLimitOffset() {
        String sql = "SELECT * FROM users LIMIT 10 OFFSET 5";
        SqlInfo result = parser.parse(sql);

        assertNotNull(result);
        assertNotNull(result.getLimitInfo());
        assertEquals(10, result.getLimitInfo().getLimit());
        assertEquals(5, result.getLimitInfo().getOffset());
    }

    @Test
    @DisplayName("测试MySQL风格LIMIT")
    void testMySqlStyleLimit() {
        String sql = "SELECT * FROM users LIMIT 5, 10";
        SqlInfo result = parser.parse(sql);

        assertNotNull(result);
        assertNotNull(result.getLimitInfo());
        assertEquals(10, result.getLimitInfo().getLimit());
        assertEquals(5, result.getLimitInfo().getOffset());
    }

    // ========== ORDER BY测试 ==========

    @Test
    @DisplayName("测试ORDER BY ASC")
    void testOrderByAsc() {
        String sql = "SELECT * FROM users ORDER BY name ASC";
        SqlInfo result = parser.parse(sql);

        assertNotNull(result);
        assertEquals(1, result.getOrderByColumns().size());

        OrderByInfo orderBy = result.getOrderByColumns().get(0);
        assertEquals("name", orderBy.getColumnName());
        assertEquals(OrderByInfo.Direction.ASC, orderBy.getDirection());
    }

    @Test
    @DisplayName("测试ORDER BY DESC")
    void testOrderByDesc() {
        String sql = "SELECT * FROM users ORDER BY created_at DESC";
        SqlInfo result = parser.parse(sql);

        assertNotNull(result);
        assertEquals(1, result.getOrderByColumns().size());

        OrderByInfo orderBy = result.getOrderByColumns().get(0);
        assertEquals("created_at", orderBy.getColumnName());
        assertEquals(OrderByInfo.Direction.DESC, orderBy.getDirection());
    }

    @Test
    @DisplayName("测试ORDER BY默认方向")
    void testOrderByDefault() {
        String sql = "SELECT * FROM users ORDER BY name";
        SqlInfo result = parser.parse(sql);

        assertNotNull(result);
        assertEquals(1, result.getOrderByColumns().size());

        OrderByInfo orderBy = result.getOrderByColumns().get(0);
        assertEquals("name", orderBy.getColumnName());
        assertEquals(OrderByInfo.Direction.ASC, orderBy.getDirection());
    }

    @Test
    @DisplayName("测试多个ORDER BY")
    void testMultipleOrderBy() {
        String sql = "SELECT * FROM users ORDER BY name ASC, age DESC, created_at";
        SqlInfo result = parser.parse(sql);

        assertNotNull(result);
        assertEquals(3, result.getOrderByColumns().size());

        assertEquals("name", result.getOrderByColumns().get(0).getColumnName());
        assertEquals(OrderByInfo.Direction.ASC, result.getOrderByColumns().get(0).getDirection());

        assertEquals("age", result.getOrderByColumns().get(1).getColumnName());
        assertEquals(OrderByInfo.Direction.DESC, result.getOrderByColumns().get(1).getDirection());

        assertEquals("created_at", result.getOrderByColumns().get(2).getColumnName());
        assertEquals(OrderByInfo.Direction.ASC, result.getOrderByColumns().get(2).getDirection());
    }

    // ========== GROUP BY测试 ==========

    @Test
    @DisplayName("测试GROUP BY")
    void testGroupBy() {
        String sql = "SELECT department, COUNT(*) FROM employees GROUP BY department";
        SqlInfo result = parser.parse(sql);

        assertNotNull(result);
        assertEquals(1, result.getGroupByColumns().size());
        assertEquals("department", result.getGroupByColumns().get(0));
    }

    @Test
    @DisplayName("测试多个GROUP BY")
    void testMultipleGroupBy() {
        String sql = "SELECT department, position, COUNT(*) FROM employees GROUP BY department, position";
        SqlInfo result = parser.parse(sql);

        assertNotNull(result);
        assertEquals(2, result.getGroupByColumns().size());
        assertEquals("department", result.getGroupByColumns().get(0));
        assertEquals("position", result.getGroupByColumns().get(1));
    }

    // ========== INSERT语句测试 ==========

    @Test
    @DisplayName("测试基础INSERT语句")
    void testBasicInsert() {
        String sql = "INSERT INTO users (name, email, age) VALUES ('John', 'john@example.com', 25)";
        SqlInfo result = parser.parse(sql);

        assertNotNull(result);
        assertEquals(SqlInfo.SqlType.INSERT, result.getSqlType());
        assertEquals("users", result.getMainTable().getTableName());

        assertEquals(3, result.getInsertColumns().size());
        assertEquals("name", result.getInsertColumns().get(0));
        assertEquals("email", result.getInsertColumns().get(1));
        assertEquals("age", result.getInsertColumns().get(2));

        assertEquals(3, result.getInsertValues().size());
        assertEquals("'John'", result.getInsertValues().get(0));
        assertEquals("'john@example.com'", result.getInsertValues().get(1));
        assertEquals("25", result.getInsertValues().get(2));
    }

    @Test
    @DisplayName("测试INSERT带NULL值")
    void testInsertWithNull() {
        String sql = "INSERT INTO users (name, email, deleted_at) VALUES ('John', 'john@example.com', NULL)";
        SqlInfo result = parser.parse(sql);

        assertNotNull(result);
        assertEquals(3, result.getInsertValues().size());
        assertEquals("NULL", result.getInsertValues().get(2));
    }

    // ========== UPDATE语句测试 ==========

    @Test
    @DisplayName("测试基础UPDATE语句")
    void testBasicUpdate() {
        String sql = "UPDATE users SET name = 'John Doe', age = 26 WHERE id = 1";
        SqlInfo result = parser.parse(sql);

        assertNotNull(result);
        assertEquals(SqlInfo.SqlType.UPDATE, result.getSqlType());
        assertEquals("users", result.getMainTable().getTableName());

        assertNotNull(result.getUpdateValues());
        assertEquals(2, result.getUpdateValues().size());
        assertEquals("'John Doe'", result.getUpdateValues().get("name"));
        assertEquals("26", result.getUpdateValues().get("age"));

        assertEquals(1, result.getWhereConditions().size());
    }

    @Test
    @DisplayName("测试UPDATE带表别名")
    void testUpdateWithAlias() {
        String sql = "UPDATE users u SET u.name = 'John' WHERE u.id = 1";
        SqlInfo result = parser.parse(sql);

        assertNotNull(result);
        assertEquals("users", result.getMainTable().getTableName());
        assertEquals("u", result.getMainTable().getAlias());
    }

    @Test
    @DisplayName("测试UPDATE带LIMIT")
    void testUpdateWithLimit() {
        String sql = "UPDATE users SET status = 'inactive' WHERE last_login < '2023-01-01' LIMIT 100";
        SqlInfo result = parser.parse(sql);

        assertNotNull(result);
        assertNotNull(result.getLimitInfo());
        assertEquals(100, result.getLimitInfo().getLimit());
    }

    // ========== DELETE语句测试 ==========

    @Test
    @DisplayName("测试基础DELETE语句")
    void testBasicDelete() {
        String sql = "DELETE FROM users WHERE id = 1";
        SqlInfo result = parser.parse(sql);

        assertNotNull(result);
        assertEquals(SqlInfo.SqlType.DELETE, result.getSqlType());
        assertEquals("users", result.getMainTable().getTableName());
        assertEquals(1, result.getWhereConditions().size());
    }

    @Test
    @DisplayName("测试DELETE带表别名")
    void testDeleteWithAlias() {
        String sql = "DELETE FROM users u WHERE u.status = 'deleted'";
        SqlInfo result = parser.parse(sql);

        assertNotNull(result);
        assertEquals("users", result.getMainTable().getTableName());
        assertEquals("u", result.getMainTable().getAlias());
    }

    @Test
    @DisplayName("测试DELETE带LIMIT")
    void testDeleteWithLimit() {
        String sql = "DELETE FROM logs WHERE created_at < '2023-01-01' LIMIT 1000";
        SqlInfo result = parser.parse(sql);

        assertNotNull(result);
        assertNotNull(result.getLimitInfo());
        assertEquals(1000, result.getLimitInfo().getLimit());
    }

    @Test
    @DisplayName("测试DELETE全表")
    void testDeleteAll() {
        String sql = "DELETE FROM temp_table";
        SqlInfo result = parser.parse(sql);

        assertNotNull(result);
        assertEquals(SqlInfo.SqlType.DELETE, result.getSqlType());
        assertEquals("temp_table", result.getMainTable().getTableName());
        assertTrue(result.getWhereConditions() == null || result.getWhereConditions().isEmpty());
    }

    // ========== toSql方法测试 ==========

    @Test
    @DisplayName("测试SqlInfo转换为SELECT SQL")
    void testToSelectSql() {
        String originalSql = "SELECT u.id, u.name FROM users u WHERE u.status = 'active' ORDER BY u.name";
        SqlInfo sqlInfo = parser.parse(originalSql);
        String generatedSql = parser.toSql(sqlInfo);

        assertNotNull(generatedSql);
        assertTrue(generatedSql.toUpperCase().contains("SELECT"));
        assertTrue(generatedSql.toUpperCase().contains("FROM"));
        assertTrue(generatedSql.toUpperCase().contains("WHERE"));
        assertTrue(generatedSql.toUpperCase().contains("ORDER BY"));
    }

    @Test
    @DisplayName("测试SqlInfo转换为INSERT SQL")
    void testToInsertSql() {
        String originalSql = "INSERT INTO users (name, email) VALUES ('John', 'john@example.com')";
        SqlInfo sqlInfo = parser.parse(originalSql);
        String generatedSql = parser.toSql(sqlInfo);

        assertNotNull(generatedSql);
        assertTrue(generatedSql.toUpperCase().contains("INSERT INTO"));
        assertTrue(generatedSql.toUpperCase().contains("VALUES"));
    }

    @Test
    @DisplayName("测试SqlInfo转换为UPDATE SQL")
    void testToUpdateSql() {
        String originalSql = "UPDATE users SET name = 'John' WHERE id = 1";
        SqlInfo sqlInfo = parser.parse(originalSql);
        String generatedSql = parser.toSql(sqlInfo);

        assertNotNull(generatedSql);
        assertTrue(generatedSql.toUpperCase().contains("UPDATE"));
        assertTrue(generatedSql.toUpperCase().contains("SET"));
        assertTrue(generatedSql.toUpperCase().contains("WHERE"));
    }

    @Test
    @DisplayName("测试SqlInfo转换为DELETE SQL")
    void testToDeleteSql() {
        String originalSql = "DELETE FROM users WHERE id = 1";
        SqlInfo sqlInfo = parser.parse(originalSql);
        String generatedSql = parser.toSql(sqlInfo);

        assertNotNull(generatedSql);
        assertTrue(generatedSql.toUpperCase().contains("DELETE FROM"));
        assertTrue(generatedSql.toUpperCase().contains("WHERE"));
    }

    // ========== 边界情况和异常测试 ==========


    @Test
    @DisplayName("测试超长SQL")
    void testVeryLongSql() {
        StringBuilder longSql = new StringBuilder("SELECT ");
        for (int i = 0; i < 1000; i++) {
            if (i > 0) longSql.append(", ");
            longSql.append("col").append(i);
        }
        longSql.append(" FROM users");

        SqlInfo result = parser.parse(longSql.toString());
        assertNotNull(result);
        assertEquals(1000, result.getSelectColumns().size());
    }

    @Test
    @DisplayName("测试大小写不敏感")
    void testCaseInsensitive() {
        String[] sqls = {
                "select * from users",
                "SELECT * FROM USERS",
                "Select * From Users",
                "sElEcT * fRoM uSeRs"
        };

        for (String sql : sqls) {
            SqlInfo result = parser.parse(sql);
            assertNotNull(result);
            assertEquals(SqlInfo.SqlType.SELECT, result.getSqlType());
        }
    }

    @Test
    @DisplayName("测试带注释的SQL")
    void testSqlWithComments() {
        String sql = "SELECT * FROM users -- this is a comment\nWHERE id = 1";
        SqlInfo result = parser.parse(sql);

        assertNotNull(result);
        assertEquals(SqlInfo.SqlType.SELECT, result.getSqlType());
    }

    @Test
    @DisplayName("测试getCleaner方法")
    void testGetCleaner() {
        SqlCleaner cleaner1 = parser.getCleaner();
        SqlCleaner cleaner2 = parser.getCleaner();

        assertNotNull(cleaner1);
        assertNotNull(cleaner2);
        assertSame(cleaner1, cleaner2); // 应该返回同一个实例
        assertTrue(cleaner1 instanceof MySqlSqlCleaner);
    }

    @Test
    @DisplayName("测试getCompare方法")
    void testGetCompare() {
        SqlCompare compare1 = parser.getCompare();
        SqlCompare compare2 = parser.getCompare();

        assertNotNull(compare1);
        assertNotNull(compare2);
        assertSame(compare1, compare2); // 应该返回同一个实例
        assertTrue(compare1 instanceof MySqlSqlCompare);
    }

    // ========== 特殊字符和转义测试 ==========

    @Test
    @DisplayName("测试包含特殊字符的SQL")
    void testSqlWithSpecialCharacters() {
        String sql = "SELECT * FROM users WHERE name = 'O\\'Reilly' AND description LIKE '%100%'";
        SqlInfo result = parser.parse(sql);

        assertNotNull(result);
        assertEquals(2, result.getWhereConditions().size());
    }

    @Test
    @DisplayName("测试包含反引号的字段名")
    void testSqlWithBackticks() {
        String sql = "SELECT `user_id`, `user_name` FROM `user_table`";
        SqlInfo result = parser.parse(sql);

        assertNotNull(result);
        assertEquals(2, result.getSelectColumns().size());
    }

    @Test
    @DisplayName("测试无效的操作符")
    void testInvalidOperator() {
        String sql = "SELECT * FROM users WHERE id === 1";
        SqlInfo result = parser.parse(sql);
        // 应该能解析，但可能无法正确识别操作符
        assertNotNull(result);
    }
}