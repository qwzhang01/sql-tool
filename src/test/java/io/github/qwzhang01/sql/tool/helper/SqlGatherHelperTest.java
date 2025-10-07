package io.github.qwzhang01.sql.tool.helper;

import io.github.qwzhang01.sql.tool.enums.FieldType;
import io.github.qwzhang01.sql.tool.enums.OperatorType;
import io.github.qwzhang01.sql.tool.enums.SqlType;
import io.github.qwzhang01.sql.tool.enums.TableType;
import io.github.qwzhang01.sql.tool.model.SqlGather;
import io.github.qwzhang01.sql.tool.model.SqlParam;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for SqlGatherHelper functionality.
 * Tests SQL parsing and analysis capabilities including various SQL statement types,
 * complex WHERE conditions, JOIN operations, and parameter mapping.
 */
@DisplayName("SqlGatherHelper Tests")
class SqlGatherHelperTest {

    // ========== Exception Cases Tests ==========

    @Test
    @DisplayName("Test null SQL")
    void testNullSql() {
        assertThrows(IllegalArgumentException.class, () -> SqlGatherHelper.analysis(null));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    @DisplayName("Test empty SQL and whitespace characters")
    void testEmptyAndWhitespaceSql(String sql) {
        assertThrows(IllegalArgumentException.class, () -> SqlGatherHelper.analysis(sql));
    }

    // ========== SELECT Statement Tests ==========

    @Test
    @DisplayName("Test simple SELECT statement")
    void testSimpleSelect() {
        String sql = "SELECT id, name FROM users WHERE id = ?";
        SqlGather result = SqlGatherHelper.analysis(sql);

        // Verify SQL type
        assertEquals(SqlType.SELECT, result.getSqlType());

        // Verify table information
        assertEquals(1, result.getTables().size());
        SqlGather.TableInfo table = result.getTables().get(0);
        assertEquals("users", table.tableName());
        assertEquals("users", table.getEffectiveAlias());
        assertEquals(TableType.MAIN, table.tableType());

        // Verify SELECT fields
        assertEquals(2, result.getSelectFields().size());
        assertEquals("id", result.getSelectFields().get(0).columnName());
        assertEquals("name", result.getSelectFields().get(1).columnName());

        // Verify WHERE conditions
        assertEquals(1, result.getConditions().size());
        SqlGather.FieldCondition condition = result.getConditions().get(0);
        assertEquals("id", condition.columnName());
        assertEquals(FieldType.CONDITION, condition.fieldType());
        assertEquals(OperatorType.SINGLE_PARAM, condition.operatorType());

        // Verify parameter mappings
        assertEquals(1, result.getParameterMappings().size());
        SqlGather.ParameterFieldMapping mapping = result.getParameterMappings().get(0);
        assertEquals(0, mapping.parameterIndex());
        assertEquals("users", mapping.tableName());
        assertEquals("id", mapping.fieldName());
    }

    @Test
    @DisplayName("Test SELECT * statement")
    void testSelectAll() {
        String sql = "SELECT * FROM products WHERE price > ?";
        SqlGather result = SqlGatherHelper.analysis(sql);

        assertEquals(SqlType.SELECT, result.getSqlType());
        assertEquals(1, result.getTables().size());
        assertEquals("products", result.getTables().get(0).tableName());

        // SELECT * should be skipped, not added to selectFields
        assertEquals(0, result.getSelectFields().size());

        // Verify WHERE conditions
        assertEquals(1, result.getConditions().size());
        assertEquals("price", result.getConditions().get(0).columnName());
    }

    @Test
    @DisplayName("Test SELECT with table alias")
    void testSelectWithTableAlias() {
        String sql = "SELECT u.id, u.name, u.email FROM users u WHERE u.status = ?";
        SqlGather result = SqlGatherHelper.analysis(sql);

        assertEquals(SqlType.SELECT, result.getSqlType());

        // Verify table information
        SqlGather.TableInfo table = result.getTables().get(0);
        assertEquals("users", table.tableName());
        assertEquals("u", table.alias());
        assertEquals("u", table.getEffectiveAlias());

        // Verify SELECT fields
        assertEquals(3, result.getSelectFields().size());
        for (SqlGather.FieldCondition field : result.getSelectFields()) {
            assertEquals("u", field.tableAlias());
        }

        // Verify WHERE conditions
        assertEquals(1, result.getConditions().size());
        assertEquals("u", result.getConditions().get(0).tableAlias());
        assertEquals("status", result.getConditions().get(0).columnName());
    }

    @Test
    @DisplayName("Test JOIN query")
    void testSelectWithJoin() {
        String sql = "SELECT u.name, d.department_name FROM users u " +
                "LEFT JOIN departments d ON u.dept_id = d.id " +
                "WHERE u.active = ? AND d.status = ?";
        SqlGather result = SqlGatherHelper.analysis(sql);

        assertEquals(SqlType.SELECT, result.getSqlType());

        // Verify table information
        assertEquals(2, result.getTables().size());

        SqlGather.TableInfo mainTable = result.getTables().get(0);
        assertEquals("users", mainTable.tableName());
        assertEquals("u", mainTable.alias());
        assertEquals(TableType.MAIN, mainTable.tableType());

        SqlGather.TableInfo joinTable = result.getTables().get(1);
        assertEquals("departments", joinTable.tableName());
        assertEquals("d", joinTable.alias());
        assertEquals(TableType.JOIN, joinTable.tableType());

        // Verify SELECT fields
        assertEquals(2, result.getSelectFields().size());
        assertEquals("u", result.getSelectFields().get(0).tableAlias());
        assertEquals("name", result.getSelectFields().get(0).columnName());
        assertEquals("d", result.getSelectFields().get(1).tableAlias());
        assertEquals("department_name", result.getSelectFields().get(1).columnName());

        // Verify WHERE conditions
        assertEquals(2, result.getConditions().size());

        // Verify parameter mappings
        assertEquals(2, result.getParameterMappings().size());
    }

    @Test
    @DisplayName("Test complex WHERE conditions")
    void testComplexWhereConditions() {
        String sql = "SELECT * FROM users WHERE age BETWEEN ? AND ? " +
                "AND status IN (?, ?, ?) AND name IS NOT NULL " +
                "AND email LIKE ?";
        SqlGather result = SqlGatherHelper.analysis(sql);

        assertEquals(4, result.getConditions().size());

        // Verify BETWEEN condition
        SqlGather.FieldCondition betweenCondition = result.getConditions().get(0);
        assertEquals("age", betweenCondition.columnName());
        assertEquals(OperatorType.BETWEEN_OPERATOR, betweenCondition.operatorType());
        assertEquals(2, betweenCondition.getEffectiveParamCount());

        // Verify IN condition
        SqlGather.FieldCondition inCondition = result.getConditions().get(1);
        assertEquals("status", inCondition.columnName());
        assertEquals(OperatorType.IN_OPERATOR, inCondition.operatorType());
        assertEquals(3, inCondition.actualParamCount());

        // Verify IS NOT NULL condition
        SqlGather.FieldCondition nullCondition = result.getConditions().get(2);
        assertEquals("name", nullCondition.columnName());
        assertEquals(OperatorType.NO_PARAM, nullCondition.operatorType());
        assertEquals(0, nullCondition.getEffectiveParamCount());

        // Verify LIKE condition
        SqlGather.FieldCondition likeCondition = result.getConditions().get(3);
        assertEquals("email", likeCondition.columnName());
        assertEquals(OperatorType.SINGLE_PARAM, likeCondition.operatorType());
        assertEquals(1, likeCondition.getEffectiveParamCount());

        // Verify total parameter mappings: 2(BETWEEN) + 3(IN) + 0(IS NOT NULL) + 1(LIKE) = 6
        assertEquals(6, result.getParameterMappings().size());
    }

    // ========== INSERT Statement Tests ==========

    @Test
    @DisplayName("Test simple INSERT statement")
    void testSimpleInsert() {
        String sql = "INSERT INTO users (name, email, age) VALUES (?, ?, ?)";
        SqlGather result = SqlGatherHelper.analysis(sql);

        assertEquals(SqlType.INSERT, result.getSqlType());

        // Verify table information
        assertEquals(1, result.getTables().size());
        assertEquals("users", result.getTables().get(0).tableName());

        // Verify INSERT fields
        assertEquals(3, result.getInsertFields().size());
        assertEquals("name", result.getInsertFields().get(0).columnName());
        assertEquals("email", result.getInsertFields().get(1).columnName());
        assertEquals("age", result.getInsertFields().get(2).columnName());

        // Verify field types
        for (SqlGather.FieldCondition field : result.getInsertFields()) {
            assertEquals(FieldType.INSERT, field.fieldType());
        }

        // Verify parameter mappings
        assertEquals(3, result.getParameterMappings().size());
        for (int i = 0; i < 3; i++) {
            assertEquals(i, result.getParameterMappings().get(i).parameterIndex());
            assertEquals(FieldType.INSERT, result.getParameterMappings().get(i).fieldType());
        }
    }

    @Test
    @DisplayName("Test INSERT with table alias")
    void testInsertWithTableAlias() {
        String sql = "INSERT INTO users (name, email) VALUES (?, ?)";
        SqlGather result = SqlGatherHelper.analysis(sql);

        assertEquals(SqlType.INSERT, result.getSqlType());

        // Verify table information
        SqlGather.TableInfo table = result.getTables().get(0);
        assertEquals("users", table.tableName());

        // Verify INSERT fields
        assertEquals(2, result.getInsertFields().size());
    }

    // ========== UPDATE Statement Tests ==========

    @Test
    @DisplayName("Test simple UPDATE statement")
    void testSimpleUpdate() {
        String sql = "UPDATE users SET name = '999', email = ? WHERE id = ? and status between ? and ? and age in (?,?,?,?)";
        SqlGather result = SqlGatherHelper.analysis(sql);

        assertEquals(SqlType.UPDATE, result.getSqlType());

        // Verify table information
        assertEquals(1, result.getTables().size());
        assertEquals("users", result.getTables().get(0).tableName());

        // Verify SET fields
        assertEquals(2, result.getSetFields().size());
        assertEquals("name", result.getSetFields().get(0).columnName());
        assertEquals("email", result.getSetFields().get(1).columnName());

        // Verify field types
        for (SqlGather.FieldCondition field : result.getSetFields()) {
            assertEquals(FieldType.UPDATE_SET, field.fieldType());
        }

        // Verify WHERE conditions
        assertEquals(3, result.getConditions().size());
        assertEquals("id", result.getConditions().get(0).columnName());
        assertEquals(FieldType.CONDITION, result.getConditions().get(0).fieldType());

        // Verify parameter mapping order: SET fields first, WHERE conditions second
        assertEquals(9, result.getParameterMappings().size());
        assertEquals(FieldType.UPDATE_SET, result.getParameterMappings().get(0).fieldType());
        assertEquals(FieldType.UPDATE_SET, result.getParameterMappings().get(1).fieldType());
        assertEquals(FieldType.CONDITION, result.getParameterMappings().get(2).fieldType());
    }

    @Test
    @DisplayName("Test UPDATE with complex WHERE conditions")
    void testUpdateWithComplexWhere() {
        String sql = "UPDATE users SET status = ?, updated_at = ? " +
                "WHERE age > ? AND department IN (?, ?) AND active = ?";
        SqlGather result = SqlGatherHelper.analysis(sql);

        assertEquals(SqlType.UPDATE, result.getSqlType());

        // Verify SET fields
        assertEquals(2, result.getSetFields().size());

        // Verify WHERE conditions
        assertEquals(3, result.getConditions().size());

        // Verify parameter mappings: 2 SET + 1 age + 2 IN + 1 active = 6 parameters
        assertEquals(6, result.getParameterMappings().size());

        // Verify parameter order
        assertEquals(FieldType.UPDATE_SET, result.getParameterMappings().get(0).fieldType());
        assertEquals(FieldType.UPDATE_SET, result.getParameterMappings().get(1).fieldType());
        assertEquals(FieldType.CONDITION, result.getParameterMappings().get(2).fieldType());
        assertEquals(FieldType.CONDITION, result.getParameterMappings().get(3).fieldType());
        assertEquals(FieldType.CONDITION, result.getParameterMappings().get(4).fieldType());
        assertEquals(FieldType.CONDITION, result.getParameterMappings().get(5).fieldType());
    }

    // ========== DELETE Statement Tests ==========

    @Test
    @DisplayName("Test simple DELETE statement")
    void testSimpleDelete() {
        String sql = "DELETE FROM users WHERE id = ?";
        SqlGather result = SqlGatherHelper.analysis(sql);

        assertEquals(SqlType.DELETE, result.getSqlType());

        // Verify table information
        assertEquals(1, result.getTables().size());
        assertEquals("users", result.getTables().get(0).tableName());

        // DELETE statements have no SELECT, INSERT, SET fields
        assertEquals(0, result.getSelectFields().size());
        assertEquals(0, result.getInsertFields().size());
        assertEquals(0, result.getSetFields().size());

        // Verify WHERE conditions
        assertEquals(1, result.getConditions().size());
        assertEquals("id", result.getConditions().get(0).columnName());

        // Verify parameter mappings
        assertEquals(1, result.getParameterMappings().size());
        assertEquals(FieldType.CONDITION, result.getParameterMappings().get(0).fieldType());
    }

    @Test
    @DisplayName("Test DELETE with complex WHERE conditions")
    void testDeleteWithComplexWhere() {
        String sql = "DELETE FROM users WHERE status = ? AND created_at < ? " +
                "AND department_id IN (?, ?, ?)";
        SqlGather result = SqlGatherHelper.analysis(sql);

        assertEquals(SqlType.DELETE, result.getSqlType());

        // Verify WHERE conditions
        assertEquals(3, result.getConditions().size());

        // Verify parameter mappings: 1 status + 1 created_at + 3 IN = 5 parameters
        assertEquals(5, result.getParameterMappings().size());
        for (SqlGather.ParameterFieldMapping mapping : result.getParameterMappings()) {
            assertEquals(FieldType.CONDITION, mapping.fieldType());
        }
    }

    // ========== Special Operator Tests ==========

    @Test
    @DisplayName("Test various operator types")
    void testVariousOperatorTypes() {
        String sql = "SELECT * FROM users WHERE " +
                "id = ? AND " +                    // SINGLE_PARAM
                "age BETWEEN ? AND ? AND " +       // BETWEEN_OPERATOR
                "status IN (?, ?) AND " +          // IN_OPERATOR
                "email IS NOT NULL AND " +         // NO_PARAM
                "name LIKE ?";                     // SINGLE_PARAM

        SqlGather result = SqlGatherHelper.analysis(sql);

        assertEquals(5, result.getConditions().size());

        // Verify operator types
        assertEquals(OperatorType.SINGLE_PARAM, result.getConditions().get(0).operatorType());
        assertEquals(OperatorType.BETWEEN_OPERATOR, result.getConditions().get(1).operatorType());
        assertEquals(OperatorType.IN_OPERATOR, result.getConditions().get(2).operatorType());
        assertEquals(OperatorType.NO_PARAM, result.getConditions().get(3).operatorType());
        assertEquals(OperatorType.SINGLE_PARAM, result.getConditions().get(4).operatorType());

        // Verify parameter counts
        assertEquals(1, result.getConditions().get(0).getEffectiveParamCount());
        assertEquals(2, result.getConditions().get(1).getEffectiveParamCount());
        assertEquals(2, result.getConditions().get(2).actualParamCount());
        assertEquals(0, result.getConditions().get(3).getEffectiveParamCount());
        assertEquals(1, result.getConditions().get(4).getEffectiveParamCount());
    }

    // ========== Table Alias Mapping Tests ==========

    @Test
    @DisplayName("Test table alias mapping functionality")
    void testTableAliasMapping() {
        String sql = "SELECT u.name, d.dept_name FROM users u " +
                "JOIN departments d ON u.dept_id = d.id " +
                "WHERE u.active = ?";
        SqlGather result = SqlGatherHelper.analysis(sql);

        // Verify alias mapping
        assertEquals("users", result.getRealTableName("u"));
        assertEquals("departments", result.getRealTableName("d"));
        assertEquals("unknown_table", result.getRealTableName("unknown_table"));

        // Verify table names in parameter mappings
        SqlGather.ParameterFieldMapping mapping = result.getParameterMappings().get(0);
        assertEquals("users", mapping.tableName());
        assertEquals("u", mapping.tableAlias());
        assertEquals("u", mapping.getEffectiveTableIdentifier());
    }

    // ========== Field Order Tests ==========

    @Test
    @DisplayName("Test field order of getAllFields method")
    void testGetAllFieldsOrder() {
        // INSERT statement: only returns insertFields
        String insertSql = "INSERT INTO users (name, email) VALUES (?, ?)";
        SqlGather insertResult = SqlGatherHelper.analysis(insertSql);
        assertEquals(2, insertResult.getAllFields().size());
        assertEquals(FieldType.INSERT, insertResult.getAllFields().get(0).fieldType());

        // UPDATE statement: returns setFields first, then conditions
        String updateSql = "UPDATE users SET name = ?, email = ? WHERE id = ?";
        SqlGather updateResult = SqlGatherHelper.analysis(updateSql);
        assertEquals(3, updateResult.getAllFields().size());
        assertEquals(FieldType.UPDATE_SET, updateResult.getAllFields().get(0).fieldType());
        assertEquals(FieldType.UPDATE_SET, updateResult.getAllFields().get(1).fieldType());
        assertEquals(FieldType.CONDITION, updateResult.getAllFields().get(2).fieldType());

        // SELECT statement: only returns conditions
        String selectSql = "SELECT * FROM users WHERE id = ? AND name = ?";
        SqlGather selectResult = SqlGatherHelper.analysis(selectSql);
        assertEquals(2, selectResult.getAllFields().size());
        assertEquals(FieldType.CONDITION, selectResult.getAllFields().get(0).fieldType());
        assertEquals(FieldType.CONDITION, selectResult.getAllFields().get(1).fieldType());

        // DELETE statement: only returns conditions
        String deleteSql = "DELETE FROM users WHERE id = ?";
        SqlGather deleteResult = SqlGatherHelper.analysis(deleteSql);
        assertEquals(1, deleteResult.getAllFields().size());
        assertEquals(FieldType.CONDITION, deleteResult.getAllFields().get(0).fieldType());
    }

    // ========== Edge Cases Tests ==========

    @Test
    @DisplayName("Test statements without WHERE conditions")
    void testStatementsWithoutWhere() {
        // SELECT without WHERE
        String selectSql = "SELECT id, name FROM users";
        SqlGather selectResult = SqlGatherHelper.analysis(selectSql);
        assertEquals(0, selectResult.getConditions().size());
        assertEquals(0, selectResult.getParameterMappings().size());

        // UPDATE without WHERE
        String updateSql = "UPDATE users SET status = ?";
        SqlGather updateResult = SqlGatherHelper.analysis(updateSql);
        assertEquals(0, updateResult.getConditions().size());
        assertEquals(1, updateResult.getParameterMappings().size());

        // DELETE without WHERE
        String deleteSql = "DELETE FROM users";
        SqlGather deleteResult = SqlGatherHelper.analysis(deleteSql);
        assertEquals(0, deleteResult.getConditions().size());
        assertEquals(0, deleteResult.getParameterMappings().size());
    }

    @Test
    @DisplayName("Test table names containing dots")
    void testTableNameWithDots() {
        String sql = "SELECT u.id FROM schema.users u WHERE u.name = ?";
        SqlGather result = SqlGatherHelper.analysis(sql);

        // Verify table information
        SqlGather.TableInfo table = result.getTables().get(0);
        assertTrue(table.tableName().contains("schema") || table.tableName().contains("users"));
        assertEquals("u", table.alias());

        // Verify field parsing
        assertEquals(1, result.getSelectFields().size());
        assertEquals("u", result.getSelectFields().get(0).tableAlias());
        assertEquals("id", result.getSelectFields().get(0).columnName());
    }

    @Test
    @DisplayName("Test parsing field names containing dots")
    void testFieldNameWithDots() {
        String sql = "SELECT table1.field1, table2.field2 FROM table1, table2 " +
                "WHERE table1.id = ? AND table2.status = ?";
        SqlGather result = SqlGatherHelper.analysis(sql);

        // Verify SELECT field parsing
        assertEquals(2, result.getSelectFields().size());
        assertEquals("table1", result.getSelectFields().get(0).tableAlias());
        assertEquals("field1", result.getSelectFields().get(0).columnName());
        assertEquals("table2", result.getSelectFields().get(1).tableAlias());
        assertEquals("field2", result.getSelectFields().get(1).columnName());

        // Verify WHERE condition parsing
        assertEquals(2, result.getConditions().size());
        assertEquals("table1", result.getConditions().get(0).tableAlias());
        assertEquals("id", result.getConditions().get(0).columnName());
        assertEquals("table2", result.getConditions().get(1).tableAlias());
        assertEquals("status", result.getConditions().get(1).columnName());
    }

    @Test
    @DisplayName("Test parameter mapping index order")
    void testParameterMappingIndexOrder() {
        String sql = "UPDATE users SET name = ?, email = ?, status = ? " +
                "WHERE id = ? AND age > ? AND department IN (?, ?)";
        SqlGather result = SqlGatherHelper.analysis(sql);

        // Verify parameter mapping index continuity
        assertEquals(7, result.getParameterMappings().size());
        for (int i = 0; i < result.getParameterMappings().size(); i++) {
            assertEquals(i, result.getParameterMappings().get(i).parameterIndex());
        }

        // Verify parameter type order: SET fields first, WHERE conditions second
        assertEquals(FieldType.UPDATE_SET, result.getParameterMappings().get(0).fieldType());
        assertEquals(FieldType.UPDATE_SET, result.getParameterMappings().get(1).fieldType());
        assertEquals(FieldType.UPDATE_SET, result.getParameterMappings().get(2).fieldType());
        assertEquals(FieldType.CONDITION, result.getParameterMappings().get(3).fieldType());
        assertEquals(FieldType.CONDITION, result.getParameterMappings().get(4).fieldType());
        assertEquals(FieldType.CONDITION, result.getParameterMappings().get(5).fieldType());
        assertEquals(FieldType.CONDITION, result.getParameterMappings().get(6).fieldType());
    }

    // ========== SqlParam Method Tests ==========

    @Test
    @DisplayName("Test param method with null SQL")
    void testParamMethodWithNullSql() {
        assertThrows(IllegalArgumentException.class, () -> SqlGatherHelper.param(null));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    @DisplayName("Test param method with empty SQL and whitespace characters")
    void testParamMethodWithEmptyAndWhitespaceSql(String sql) {
        assertThrows(IllegalArgumentException.class, () -> SqlGatherHelper.param(sql));
    }

    @Test
    @DisplayName("Test param method with simple SELECT statement")
    void testParamMethodWithSimpleSelect() {
        String sql = "SELECT id, name FROM users WHERE id = ? AND status = ?";
        List<SqlParam> params = SqlGatherHelper.param(sql);

        assertEquals(2, params.size());

        // Verify first parameter
        SqlParam firstParam = params.get(0);
        assertEquals(0, firstParam.getIndex());
        assertEquals("id", firstParam.getFieldName());
        assertEquals("users", firstParam.getTableName());
        assertEquals("users", firstParam.getTableAlias());

        // Verify second parameter
        SqlParam secondParam = params.get(1);
        assertEquals(1, secondParam.getIndex());
        assertEquals("status", secondParam.getFieldName());
        assertEquals("users", secondParam.getTableName());
        assertEquals("users", secondParam.getTableAlias());
    }

    @Test
    @DisplayName("Test param method with INSERT statement")
    void testParamMethodWithInsert() {
        String sql = "INSERT INTO users (name, email, age) VALUES (?, ?, ?)";
        List<SqlParam> params = SqlGatherHelper.param(sql);

        assertEquals(3, params.size());

        // Verify parameter order and properties
        String[] expectedFields = {"name", "email", "age"};
        for (int i = 0; i < params.size(); i++) {
            SqlParam param = params.get(i);
            assertEquals(i, param.getIndex());
            assertEquals(expectedFields[i], param.getFieldName());
            assertEquals("users", param.getTableName());
            assertEquals("users", param.getTableAlias());
        }
    }

    @Test
    @DisplayName("Test param method with UPDATE statement")
    void testParamMethodWithUpdate() {
        String sql = "UPDATE users SET name = ?, email = ? WHERE id = ? AND status = ?";
        List<SqlParam> params = SqlGatherHelper.param(sql);

        assertEquals(4, params.size());

        // Verify SET field parameters (should come first)
        assertEquals(0, params.get(0).getIndex());
        assertEquals("name", params.get(0).getFieldName());
        assertEquals(1, params.get(1).getIndex());
        assertEquals("email", params.get(1).getFieldName());

        // Verify WHERE condition parameters (should come after SET fields)
        assertEquals(2, params.get(2).getIndex());
        assertEquals("id", params.get(2).getFieldName());
        assertEquals(3, params.get(3).getIndex());
        assertEquals("status", params.get(3).getFieldName());

        // Verify all parameters belong to the same table
        for (SqlParam param : params) {
            assertEquals("users", param.getTableName());
            assertEquals("users", param.getTableAlias());
        }
    }

    @Test
    @DisplayName("Test param method with DELETE statement")
    void testParamMethodWithDelete() {
        String sql = "DELETE FROM users WHERE id = ? AND created_at < ?";
        List<SqlParam> params = SqlGatherHelper.param(sql);

        assertEquals(2, params.size());

        // Verify first parameter
        assertEquals(0, params.get(0).getIndex());
        assertEquals("id", params.get(0).getFieldName());
        assertEquals("users", params.get(0).getTableName());

        // Verify second parameter
        assertEquals(1, params.get(1).getIndex());
        assertEquals("created_at", params.get(1).getFieldName());
        assertEquals("users", params.get(1).getTableName());
    }

    @Test
    @DisplayName("Test param method with table aliases")
    void testParamMethodWithTableAliases() {
        String sql = "SELECT u.name, d.dept_name FROM users u " +
                "JOIN departments d ON u.dept_id = d.id " +
                "WHERE u.active = ? AND d.status = ?";
        List<SqlParam> params = SqlGatherHelper.param(sql);

        assertEquals(2, params.size());

        // Verify first parameter (users table)
        SqlParam firstParam = params.get(0);
        assertEquals(0, firstParam.getIndex());
        assertEquals("active", firstParam.getFieldName());
        assertEquals("users", firstParam.getTableName());
        assertEquals("u", firstParam.getTableAlias());

        // Verify second parameter (departments table)
        SqlParam secondParam = params.get(1);
        assertEquals(1, secondParam.getIndex());
        assertEquals("status", secondParam.getFieldName());
        assertEquals("departments", secondParam.getTableName());
        assertEquals("d", secondParam.getTableAlias());
    }

    @Test
    @DisplayName("Test param method with complex WHERE conditions")
    void testParamMethodWithComplexWhereConditions() {
        String sql = "SELECT * FROM users WHERE age BETWEEN ? AND ? " +
                "AND status IN (?, ?, ?) AND name LIKE ?";
        List<SqlParam> params = SqlGatherHelper.param(sql);

        assertEquals(6, params.size());

        // Verify BETWEEN parameters
        assertEquals("age", params.get(0).getFieldName());
        assertEquals("age", params.get(1).getFieldName());

        // Verify IN parameters
        assertEquals("status", params.get(2).getFieldName());
        assertEquals("status", params.get(3).getFieldName());
        assertEquals("status", params.get(4).getFieldName());

        // Verify LIKE parameter
        assertEquals("name", params.get(5).getFieldName());

        // Verify sequential indexing
        for (int i = 0; i < params.size(); i++) {
            assertEquals(i, params.get(i).getIndex());
        }
    }

    @Test
    @DisplayName("Test param method with no parameters")
    void testParamMethodWithNoParameters() {
        String sql = "SELECT id, name FROM users WHERE status = 'active'";
        List<SqlParam> params = SqlGatherHelper.param(sql);

        assertEquals(0, params.size());
    }

    @Test
    @DisplayName("Test param method with mixed parameter types")
    void testParamMethodWithMixedParameterTypes() {
        String sql = "UPDATE users SET name = ?, status = 'active', email = ? " +
                "WHERE id = ? AND created_at > '2023-01-01'";
        List<SqlParam> params = SqlGatherHelper.param(sql);

        assertEquals(3, params.size());

        // Verify SET field parameters
        assertEquals(0, params.get(0).getIndex());
        assertEquals("name", params.get(0).getFieldName());
        assertEquals(1, params.get(1).getIndex());
        assertEquals("email", params.get(1).getFieldName());

        // Verify WHERE condition parameter
        assertEquals(2, params.get(2).getIndex());
        assertEquals("id", params.get(2).getFieldName());
    }

    @Test
    @DisplayName("Test param method parameter index reordering")
    void testParamMethodParameterIndexReordering() {
        String sql = "INSERT INTO products (name, price, category_id) VALUES (?, ?, ?)";
        List<SqlParam> params = SqlGatherHelper.param(sql);

        assertEquals(3, params.size());

        // Verify that indexes are sequential starting from 0
        for (int i = 0; i < params.size(); i++) {
            assertEquals(i, params.get(i).getIndex());
        }

        // Verify field order is preserved
        assertEquals("name", params.get(0).getFieldName());
        assertEquals("price", params.get(1).getFieldName());
        assertEquals("category_id", params.get(2).getFieldName());
    }

    @Test
    @DisplayName("Test param method with subquery parameters")
    void testParamMethodWithSubqueryParameters() {
        String sql = "SELECT * FROM users WHERE department_id IN " +
                "(SELECT id FROM departments WHERE status = ?) AND age > ?";
        List<SqlParam> params = SqlGatherHelper.param(sql);

        assertEquals(2, params.size());

        // Verify parameters are extracted from both main query and subquery
        assertEquals(0, params.get(0).getIndex());
        assertEquals(1, params.get(1).getIndex());
    }

    @Test
    @DisplayName("测试用户报告的复杂 SQL 问题")
    public void testUserReportedComplexSql() {
        String sql = "SELECT * FROM user u WHERE u.isDel = 1 AND u.phone like ? AND u.status IN(?, ?) AND u.age > ? AND u.name IS NOT NULL AND u.id BETWEEN ? AND ?";
        SqlGather result = SqlGatherHelper.analysis(sql);

        assertEquals(SqlType.SELECT, result.getSqlType());
        assertEquals(1, result.getTables().size());
        assertEquals("user", result.getTables().get(0).tableName());
        assertEquals("u", result.getTables().get(0).alias());

        // 验证条件字段 - 只有包含参数的条件会被解析
        List<SqlGather.FieldCondition> conditions = result.getConditions();
        assertEquals(6, conditions.size());

        // u.phone like ?
        SqlGather.FieldCondition phoneCondition = conditions.get(1);
        assertEquals("u", phoneCondition.tableAlias());
        assertEquals("phone", phoneCondition.columnName());
        assertEquals(OperatorType.SINGLE_PARAM, phoneCondition.operatorType());
        assertEquals(1, phoneCondition.actualParamCount());

        // u.status IN(?, ?)
        SqlGather.FieldCondition statusCondition = conditions.get(2);
        assertEquals("u", statusCondition.tableAlias());
        assertEquals("status", statusCondition.columnName());
        assertEquals(OperatorType.IN_OPERATOR, statusCondition.operatorType());
        assertEquals(2, statusCondition.actualParamCount());

        // u.age > ?
        SqlGather.FieldCondition ageCondition = conditions.get(3);
        assertEquals("u", ageCondition.tableAlias());
        assertEquals("age", ageCondition.columnName());
        assertEquals(OperatorType.SINGLE_PARAM, ageCondition.operatorType());
        assertEquals(1, ageCondition.actualParamCount());

        // u.id BETWEEN(?, ?) - 这是关键测试点
        SqlGather.FieldCondition idCondition = conditions.get(5);
        assertEquals("u", idCondition.tableAlias());
        assertEquals("id", idCondition.columnName());
        assertEquals(OperatorType.BETWEEN_OPERATOR, idCondition.operatorType());
        assertEquals(2, idCondition.actualParamCount());

        // 验证参数映射总数
        List<SqlGather.ParameterFieldMapping> mappings = result.getParameterMappings();
        assertEquals(7, mappings.size()); // 1 + 2 + 1 + 2 = 6 个参数

        // 验证具体的参数映射
        assertEquals("isDel", mappings.get(0).fieldName());
        assertEquals("phone", mappings.get(1).fieldName());
        assertEquals("status", mappings.get(2).fieldName());
        assertEquals("status", mappings.get(3).fieldName());
        assertEquals("age", mappings.get(4).fieldName());
        assertEquals("id", mappings.get(5).fieldName());
        assertEquals("id", mappings.get(6).fieldName());
    }

    @Test
    @DisplayName("测试无别名时的表名解析")
    public void testParameterMappingWithoutAlias() {
        String sql = "SELECT name FROM user WHERE phone = ?";

        SqlGather result = SqlGatherHelper.analysis(sql);

        assertEquals(1, result.getParameterMappings().size());
        SqlGather.ParameterFieldMapping mapping = result.getParameterMappings().get(0);
        assertEquals("user", mapping.tableName());
        assertEquals("phone", mapping.fieldName());
        assertEquals("user", mapping.tableAlias());
    }
}