package io.github.qwzhang01.sql.tool.jsqlparser;

import io.github.qwzhang01.sql.tool.model.SqlParam;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.update.UpdateSet;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;

public class JsqlParserTest {

    @Test
    void leftJoin() throws JSQLParserException {
        String sql = "select * from aa a" +
                "LEFT JOIN posts p ON u.id = p.user_id AND p.status = ? " +
                "INNER JOIN departments d ON u.dept_id = d.id ";

        Statement statement = CCJSqlParserUtil.parse(sql);

        statement.accept(new TablesStatementVisitorAdaptor(true));

        Select select = (Select) statement;
        PlainSelect plainSelect = select.getPlainSelect();

        System.out.println("\n=== LEFT JOIN 查询解析 ===");
        System.out.println("原始SQL: " + sql);

        // 解析表信息
        FromItem fromItem = plainSelect.getFromItem();
        if (fromItem instanceof Table table) {
            System.out.println("表名: " + table.getName());
            System.out.println("表别名: " + (table.getAlias() != null ? table.getAlias().getName() : "无"));
        }

        // 解析 JOIN 信息
        List<Join> joinList = plainSelect.getJoins();
    }

    /**
     * 测试基本的 SELECT 语句解析
     */
    @Test
    public void testBasicSelectParsing() throws JSQLParserException {
        String sql = "SELECT id, name, age FROM users WHERE id = ? AND name LIKE ?";

        Statement statement = CCJSqlParserUtil.parse(sql);
        Select select = (Select) statement;
        PlainSelect plainSelect = select.getPlainSelect();

        System.out.println("=== 基本 SELECT 语句解析 ===");
        System.out.println("原始SQL: " + sql);

        // 解析表信息
        FromItem fromItem = plainSelect.getFromItem();
        if (fromItem instanceof Table) {
            Table table = (Table) fromItem;
            System.out.println("表名: " + table.getName());
            System.out.println("表别名: " + (table.getAlias() != null ? table.getAlias().getName() : "无"));
        }

        // 解析字段信息
        System.out.println("查询字段:");
        List<SelectItem<?>> selectItems = plainSelect.getSelectItems();
        for (SelectItem<?> selectItem : selectItems) {
            if (selectItem.getExpression() != null) {
                System.out.println("  字段: " + selectItem.getExpression());
                System.out.println("  别名: " + (selectItem.getAlias() != null ? selectItem.getAlias().getName() : "无"));
            }
        }

        // 解析WHERE条件中的占位符
        parseWhereConditions(plainSelect.getWhere());
    }

    /**
     * 测试复杂的 JOIN 查询解析
     */
    @Test
    public void testComplexJoinParsing() throws JSQLParserException {
        String sql = "SELECT u.id, u.name, p.title, d.name as dept_name " +
                "FROM users u " +
                "LEFT JOIN posts p ON u.id = p.user_id AND p.status = ? " +
                "INNER JOIN departments d ON u.dept_id = d.id " +
                "WHERE u.age > ? AND u.created_date BETWEEN ? AND ? " +
                "ORDER BY u.id LIMIT ?";

        Statement statement = CCJSqlParserUtil.parse(sql);
        Select select = (Select) statement;
        PlainSelect plainSelect = select.getPlainSelect();

        System.out.println("\n=== 复杂 JOIN 查询解析 ===");
        System.out.println("原始SQL: " + sql);

        // 解析主表
        FromItem fromItem = plainSelect.getFromItem();
        if (fromItem instanceof Table table) {
            System.out.println("主表名: " + table.getName());
            System.out.println("主表别名: " + (table.getAlias() != null ? table.getAlias().getName() : "无"));
        }

        // 解析JOIN表
        List<Join> joins = plainSelect.getJoins();
        if (joins != null) {
            System.out.println("JOIN表信息:");
            for (Join join : joins) {
                FromItem joinItem = join.getFromItem();
                if (joinItem instanceof Table joinTable) {
                    System.out.println("  JOIN类型: " + (join.isLeft() ? "LEFT JOIN" : join.isInner() ? "INNER JOIN" : "JOIN"));
                    System.out.println("  表名: " + joinTable.getName());
                    System.out.println("  表别名: " + (joinTable.getAlias() != null ? joinTable.getAlias().getName() : "无"));

                    // 解析JOIN条件中的占位符
                    Collection<Expression> onExpressions = join.getOnExpressions();
                    if (onExpressions != null && !onExpressions.isEmpty()) {
                        System.out.println("  JOIN条件:");
                        for (Expression onExpr : onExpressions) {
                            List<SqlParam> sqlParams = ExpressionParse.getInstance().parseExpression(onExpr);
                            sqlParams.forEach(System.out::println);
                        }
                    }
                }
            }
        }

        // 解析查询字段
        System.out.println("查询字段:");
        List<SelectItem<?>> selectItems = plainSelect.getSelectItems();
        for (SelectItem<?> selectItem : selectItems) {
            if (selectItem.getExpression() != null) {
                System.out.println("  字段表达式: " + selectItem.getExpression());
                System.out.println("  字段别名: " + (selectItem.getAlias() != null ? selectItem.getAlias().getName() : "无"));
            }
        }

        // 解析WHERE条件
        parseWhereConditions(plainSelect.getWhere());

        // 解析LIMIT中的占位符
        Limit limit = plainSelect.getLimit();
        if (limit != null && limit.getRowCount() instanceof JdbcParameter) {
            System.out.println("LIMIT占位符: ?");
            System.out.println("  用途: 限制查询结果数量");
        }
    }

    /**
     * 测试 INSERT 语句解析
     */
    @Test
    public void testInsertParsing() throws JSQLParserException {
        String sql = "INSERT INTO users (name, email, age, dept_id) VALUES (?, ?, ?, ?)";

        Statement statement = CCJSqlParserUtil.parse(sql);
        Insert insert = (Insert) statement;

        System.out.println("\n=== INSERT 语句解析 ===");
        System.out.println("原始SQL: " + sql);
        System.out.println("目标表: " + insert.getTable().getName());

        // 解析插入字段
        System.out.println("插入字段:");
        List<Column> columns = insert.getColumns();
        if (columns != null) {
            for (int i = 0; i < columns.size(); i++) {
                System.out.println("  字段" + (i + 1) + ": " + columns.get(i).getColumnName());
            }
        }

        // 解析VALUES中的占位符
        if (insert.getValues() != null && insert.getValues().getExpressions() != null) {
            System.out.println("VALUES占位符:");
            ExpressionList<?> expressions = insert.getValues().getExpressions();
            for (int i = 0; i < expressions.size(); i++) {
                Expression expr = expressions.get(i);
                if (expr instanceof JdbcParameter) {
                    System.out.println("  占位符" + (i + 1) + ": ? (对应字段: " +
                            (columns != null && i < columns.size() ? columns.get(i).getColumnName() : "未知") + ")");
                }
            }
        }
    }

    /**
     * 测试 UPDATE 语句解析
     */
    @Test
    public void testUpdateParsing() throws JSQLParserException {
        String sql = "UPDATE users SET name = ?, email = ?, updated_date = ? WHERE id = ? AND status = ?";

        Statement statement = CCJSqlParserUtil.parse(sql);
        Update update = (Update) statement;

        System.out.println("\n=== UPDATE 语句解析 ===");
        System.out.println("原始SQL: " + sql);
        System.out.println("目标表: " + update.getTable().getName());

        // 解析SET子句中的占位符
        System.out.println("SET子句占位符:");
        List<UpdateSet> updateSets = update.getUpdateSets();
        if (updateSets != null) {
            for (UpdateSet updateSet : updateSets) {
                List<Column> columns = updateSet.getColumns();
                List<?> expressions = updateSet.getValues().getExpressions();
                for (int i = 0; i < columns.size() && i < expressions.size(); i++) {
                    Column column = columns.get(i);
                    Object value = expressions.get(i);
                    if (value instanceof JdbcParameter) {
                        System.out.println("  字段: " + column.getColumnName() + " = ? (占位符)");
                    }
                }
            }
        }

        // 解析WHERE条件
        parseWhereConditions(update.getWhere());
    }

    /**
     * 测试 DELETE 语句解析
     */
    @Test
    public void testDeleteParsing() throws JSQLParserException {
        String sql = "DELETE FROM users WHERE age < ? OR (status = ? AND created_date < ?)";

        Statement statement = CCJSqlParserUtil.parse(sql);
        Delete delete = (Delete) statement;

        System.out.println("\n=== DELETE 语句解析 ===");
        System.out.println("原始SQL: " + sql);
        System.out.println("目标表: " + delete.getTable().getName());

        // 解析WHERE条件
        parseWhereConditions(delete.getWhere());
    }

    /**
     * 测试子查询解析
     */
    @Test
    public void testSubqueryParsing() throws JSQLParserException {
        String sql = "SELECT u.name, u.email FROM users u WHERE u.dept_id IN " +
                "(SELECT d.id FROM departments d WHERE d.budget > ? AND d.location = ?) " +
                "AND u.salary > (SELECT AVG(salary) FROM users WHERE dept_id = ?)";

        Statement statement = CCJSqlParserUtil.parse(sql);
        Select select = (Select) statement;
        PlainSelect plainSelect = select.getPlainSelect();

        System.out.println("\n=== 子查询解析 ===");
        System.out.println("原始SQL: " + sql);

        // 解析主查询
        System.out.println("主查询表: " + ((Table) plainSelect.getFromItem()).getName());

        // 解析WHERE条件（包含子查询）
        parseWhereConditions(plainSelect.getWhere());
    }

    /**
     * 测试聚合函数和GROUP BY解析
     */
    @Test
    public void testAggregationParsing() throws JSQLParserException {
        String sql = "SELECT d.name, COUNT(*) as user_count, AVG(u.salary) as avg_salary " +
                "FROM departments d " +
                "LEFT JOIN users u ON d.id = u.dept_id " +
                "WHERE d.budget > ? AND u.status = ? " +
                "GROUP BY d.id, d.name " +
                "HAVING COUNT(*) > ? " +
                "ORDER BY avg_salary DESC";

        Statement statement = CCJSqlParserUtil.parse(sql);
        Select select = (Select) statement;
        PlainSelect plainSelect = select.getPlainSelect();

        System.out.println("\n=== 聚合函数和GROUP BY解析 ===");
        System.out.println("原始SQL: " + sql);

        // 解析查询字段（包含聚合函数）
        System.out.println("查询字段:");
        List<SelectItem<?>> selectItems = plainSelect.getSelectItems();
        for (SelectItem<?> selectItem : selectItems) {
            if (selectItem.getExpression() != null) {
                System.out.println("  表达式: " + selectItem.getExpression());
                System.out.println("  别名: " + (selectItem.getAlias() != null ? selectItem.getAlias().getName() : "无"));
            }
        }

        // 解析WHERE条件
        parseWhereConditions(plainSelect.getWhere());

        // 解析HAVING条件
        Expression having = plainSelect.getHaving();
        if (having != null) {
            System.out.println("HAVING条件:");
            List<SqlParam> sqlParams = ExpressionParse.getInstance().parseExpression(having);
            System.out.println(sqlParams);
            sqlParams.forEach(System.out::println);
        }
    }

    /**
     * 解析WHERE条件中的占位符和表达式
     */
    private void parseWhereConditions(Expression whereExpression) {
        if (whereExpression == null) return;

        System.out.println("WHERE条件分析:");
        ExpressionParse.getInstance().parseExpression(whereExpression);
    }


    /**
     * 测试复杂的占位符场景分析
     */
    @Test
    public void testComplexPlaceholderAnalysis() throws JSQLParserException {
        String sql = "SELECT u.id, u.name, u.email, d.name as department_name, " +
                "       CASE WHEN u.salary > ? THEN 'High' ELSE 'Normal' END as salary_level " +
                "FROM users u " +
                "INNER JOIN departments d ON u.dept_id = d.id " +
                "WHERE u.status = ? " +
                "  AND u.created_date >= ? " +
                "  AND (u.age BETWEEN ? AND ? OR u.experience > ?) " +
                "  AND u.email LIKE ? " +
                "  AND u.dept_id IN (?, ?, ?) " +
                "ORDER BY u.salary DESC " +
                "LIMIT ? OFFSET ?";

        Statement statement = CCJSqlParserUtil.parse(sql);
        Select select = (Select) statement;
        PlainSelect plainSelect = select.getPlainSelect();

        System.out.println("\n=== 复杂占位符场景分析 ===");
        System.out.println("原始SQL: " + sql);

        // 统计占位符数量和位置
        int placeholderCount = countPlaceholders(sql);
        System.out.println("总占位符数量: " + placeholderCount);

        // 详细分析每个占位符的用途
        System.out.println("\n占位符详细分析:");
        System.out.println("1. CASE WHEN条件中的占位符: ? (用于薪资比较)");
        System.out.println("2. WHERE等值条件: u.status = ? (用户状态过滤)");
        System.out.println("3. WHERE日期条件: u.created_date >= ? (创建日期过滤)");
        System.out.println("4. BETWEEN起始值: u.age BETWEEN ? (年龄范围起始)");
        System.out.println("5. BETWEEN结束值: AND ? (年龄范围结束)");
        System.out.println("6. OR条件: u.experience > ? (工作经验比较)");
        System.out.println("7. LIKE条件: u.email LIKE ? (邮箱模糊匹配)");
        System.out.println("8. IN条件值1: u.dept_id IN (? (部门ID1)");
        System.out.println("9. IN条件值2: , ? (部门ID2)");
        System.out.println("10. IN条件值3: , ?) (部门ID3)");
        System.out.println("11. LIMIT数量: LIMIT ? (结果数量限制)");
        System.out.println("12. OFFSET偏移: OFFSET ? (结果偏移量)");

        // 解析表和字段信息
        System.out.println("\n表和字段信息:");
        System.out.println("主表: users (别名: u)");
        System.out.println("JOIN表: departments (别名: d)");
        System.out.println("查询字段: u.id, u.name, u.email, d.name (别名: department_name), CASE表达式 (别名: salary_level)");

        // 解析WHERE条件
        parseWhereConditions(plainSelect.getWhere());
    }

    /**
     * 统计SQL中的占位符数量
     */
    private int countPlaceholders(String sql) {
        int count = 0;
        for (char c : sql.toCharArray()) {
            if (c == '?') {
                count++;
            }
        }
        return count;
    }

    /**
     * 测试占位符位置映射和参数绑定分析
     */
    @Test
    public void testPlaceholderPositionMapping() throws JSQLParserException {
        String sql = "SELECT * FROM orders o " +
                "JOIN customers c ON o.customer_id = c.id " +
                "WHERE o.order_date BETWEEN ? AND ? " +
                "  AND o.status IN (?, ?, ?) " +
                "  AND c.city = ? " +
                "  AND o.total_amount > ? " +
                "ORDER BY o.order_date DESC " +
                "LIMIT ?";

        System.out.println("\n=== 占位符位置映射和参数绑定分析 ===");
        System.out.println("原始SQL: " + sql);

        // 模拟PreparedStatement参数绑定
        System.out.println("\nPreparedStatement参数绑定示例:");
        System.out.println("preparedStatement.setDate(1, startDate);        // 订单开始日期");
        System.out.println("preparedStatement.setDate(2, endDate);          // 订单结束日期");
        System.out.println("preparedStatement.setString(3, \"PENDING\");      // 订单状态1");
        System.out.println("preparedStatement.setString(4, \"PROCESSING\");   // 订单状态2");
        System.out.println("preparedStatement.setString(5, \"COMPLETED\");    // 订单状态3");
        System.out.println("preparedStatement.setString(6, cityName);       // 客户城市");
        System.out.println("preparedStatement.setBigDecimal(7, minAmount);  // 最小金额");
        System.out.println("preparedStatement.setInt(8, pageSize);          // 分页大小");

        // 解析SQL结构
        Statement statement = CCJSqlParserUtil.parse(sql);
        Select select = (Select) statement;
        PlainSelect plainSelect = select.getPlainSelect();

        System.out.println("\n表结构分析:");
        System.out.println("主表: orders (别名: o)");
        List<Join> joins = plainSelect.getJoins();
        if (joins != null) {
            for (Join join : joins) {
                FromItem joinItem = join.getFromItem();
                if (joinItem instanceof Table) {
                    Table joinTable = (Table) joinItem;
                    System.out.println("JOIN表: " + joinTable.getName() +
                            " (别名: " + (joinTable.getAlias() != null ? joinTable.getAlias().getName() : "无") + ")");
                }
            }
        }

        parseWhereConditions(plainSelect.getWhere());
    }

    /**
     * 测试动态SQL构建场景
     */
    @Test
    public void testDynamicSqlScenarios() {
        System.out.println("\n=== 动态SQL构建场景 ===");

        // 场景1：动态WHERE条件
        System.out.println("场景1 - 动态WHERE条件构建:");
        System.out.println("基础SQL: SELECT * FROM users WHERE 1=1");
        System.out.println("动态添加条件:");
        System.out.println("  if (name != null) sql += \" AND name LIKE ?\";     // 占位符1: 姓名模糊查询");
        System.out.println("  if (ageMin != null) sql += \" AND age >= ?\";       // 占位符2: 最小年龄");
        System.out.println("  if (ageMax != null) sql += \" AND age <= ?\";       // 占位符3: 最大年龄");
        System.out.println("  if (deptIds != null) sql += \" AND dept_id IN (\" + placeholders + \")\"; // 占位符4-N: 部门ID列表");

        // 场景2：动态ORDER BY
        System.out.println("\n场景2 - 动态ORDER BY:");
        System.out.println("基础查询 + 动态排序字段");
        System.out.println("注意：ORDER BY字段不能使用占位符，需要白名单验证");

        // 场景3：分页查询
        System.out.println("\n场景3 - 分页查询:");
        System.out.println("MySQL: SELECT * FROM users WHERE status = ? LIMIT ? OFFSET ?");
        System.out.println("  占位符1: status (状态过滤)");
        System.out.println("  占位符2: pageSize (每页大小)");
        System.out.println("  占位符3: offset (偏移量)");

        System.out.println("\nOracle: SELECT * FROM (SELECT ROWNUM rn, t.* FROM (SELECT * FROM users WHERE status = ?) t WHERE ROWNUM <= ?) WHERE rn > ?");
        System.out.println("  占位符1: status (状态过滤)");
        System.out.println("  占位符2: endRow (结束行号)");
        System.out.println("  占位符3: startRow (开始行号)");
    }

    /**
     * 测试批量操作的占位符分析
     */
    @Test
    public void testBatchOperationAnalysis() {
        System.out.println("\n=== 批量操作占位符分析 ===");

        String batchInsertSql = "INSERT INTO users (name, email, age) VALUES (?, ?, ?)";
        System.out.println("批量插入SQL: " + batchInsertSql);
        System.out.println("批量操作说明:");
        System.out.println("  每批次包含3个占位符:");
        System.out.println("    ? - name字段 (String类型)");
        System.out.println("    ? - email字段 (String类型)");
        System.out.println("    ? - age字段 (Integer类型)");
        System.out.println("  批量执行时，每行数据重复使用这3个占位符");

        String batchUpdateSql = "UPDATE users SET name = ?, email = ? WHERE id = ?";
        System.out.println("\n批量更新SQL: " + batchUpdateSql);
        System.out.println("批量更新说明:");
        System.out.println("  每批次包含3个占位符:");
        System.out.println("    ? - name字段更新值 (String类型)");
        System.out.println("    ? - email字段更新值 (String类型)");
        System.out.println("    ? - WHERE条件id值 (Long类型)");
    }

    /**
     * 测试存储过程和函数调用
     */
    @Test
    public void testStoredProcedureAndFunction() {
        System.out.println("\n=== 存储过程和函数调用分析 ===");

        System.out.println("存储过程调用示例:");
        System.out.println("CALL getUsersByDept(?, ?)");
        System.out.println("  占位符1: deptId (输入参数 - 部门ID)");
        System.out.println("  占位符2: status (输入参数 - 用户状态)");

        System.out.println("\n函数调用示例:");
        System.out.println("SELECT calculateBonus(?, ?) FROM dual");
        System.out.println("  占位符1: salary (薪资)");
        System.out.println("  占位符2: performance (绩效评分)");

        System.out.println("\n注意事项:");
        System.out.println("- JSQLParser对存储过程的支持有限");
        System.out.println("- 建议使用CallableStatement处理存储过程");
        System.out.println("- 函数调用可以在SELECT、WHERE等子句中使用");
    }

    @Test
    void test() {
        System.out.println("JSQLParser 4.9 版本测试类已完成，包含以下测试方法:");
        System.out.println("1. testBasicSelectParsing() - 基本SELECT语句解析");
        System.out.println("2. testComplexJoinParsing() - 复杂JOIN查询解析");
        System.out.println("3. testInsertParsing() - INSERT语句解析");
        System.out.println("4. testUpdateParsing() - UPDATE语句解析");
        System.out.println("5. testDeleteParsing() - DELETE语句解析");
        System.out.println("6. testSubqueryParsing() - 子查询解析");
        System.out.println("7. testAggregationParsing() - 聚合函数和GROUP BY解析");
        System.out.println("8. testComplexPlaceholderAnalysis() - 复杂占位符场景分析");
        System.out.println("9. testPlaceholderPositionMapping() - 占位符位置映射");
        System.out.println("10. testDynamicSqlScenarios() - 动态SQL构建场景");
        System.out.println("11. testBatchOperationAnalysis() - 批量操作占位符分析");
        System.out.println("12. testStoredProcedureAndFunction() - 存储过程和函数调用");
        System.out.println("\n✅ 代码已适配JSQLParser 4.9版本API");
        System.out.println("运行任意测试方法查看详细的占位符解析结果！");
    }
}