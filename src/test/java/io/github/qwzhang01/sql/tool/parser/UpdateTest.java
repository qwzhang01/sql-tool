package io.github.qwzhang01.sql.tool.parser;

import io.github.qwzhang01.sql.tool.enums.FieldType;
import io.github.qwzhang01.sql.tool.enums.SqlType;
import io.github.qwzhang01.sql.tool.helper.SqlGatherHelper;
import io.github.qwzhang01.sql.tool.model.SqlGather;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UpdateTest {
    @Test
    @DisplayName("测试 UPDATE 语句解析")
    public void testUpdateStatement() {
        String sql = "UPDATE user SET name = ?, phone = ?, status = ? WHERE id = ? AND dept_id = ?";

        SqlGather result = SqlGatherHelper.analysis(sql);

        assertEquals(SqlType.UPDATE, result.getSqlType());
        assertEquals(1, result.getTables().size());
        assertEquals("user", result.getTables().get(0).tableName());
        assertEquals(3, result.getSetFields().size());
        assertEquals(2, result.getConditions().size());
        assertEquals(5, result.getParameterMappings().size());

        // 验证 SET 字段
        assertEquals("name", result.getSetFields().get(0).columnName());
        assertEquals("phone", result.getSetFields().get(1).columnName());
        assertEquals("status", result.getSetFields().get(2).columnName());

        // 验证 WHERE 条件
        assertEquals("id", result.getConditions().get(0).columnName());
        assertEquals("dept_id", result.getConditions().get(1).columnName());

        // 验证参数映射顺序（SET 字段在前，WHERE 条件在后）
        assertEquals(FieldType.UPDATE_SET, result.getParameterMappings().get(0).fieldType());
        assertEquals(FieldType.UPDATE_SET, result.getParameterMappings().get(1).fieldType());
        assertEquals(FieldType.UPDATE_SET, result.getParameterMappings().get(2).fieldType());
        assertEquals(FieldType.CONDITION, result.getParameterMappings().get(3).fieldType());
        assertEquals(FieldType.CONDITION, result.getParameterMappings().get(4).fieldType());
    }
}
