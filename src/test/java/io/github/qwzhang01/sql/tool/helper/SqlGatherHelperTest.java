package io.github.qwzhang01.sql.tool.helper;

import io.github.qwzhang01.sql.tool.enums.FieldType;
import io.github.qwzhang01.sql.tool.enums.OperatorType;
import io.github.qwzhang01.sql.tool.enums.SqlType;
import io.github.qwzhang01.sql.tool.enums.TableType;
import io.github.qwzhang01.sql.tool.model.SqlGather;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

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
}