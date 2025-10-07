package io.github.qwzhang01.sql.tool.parser;

import io.github.qwzhang01.sql.tool.enums.SqlType;
import io.github.qwzhang01.sql.tool.helper.SqlGatherHelper;
import io.github.qwzhang01.sql.tool.model.SqlGather;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ExistsTest {

    @Test
    @DisplayName("测试复杂嵌套场景")
    public void testComplexNestedScenario() {
        String sql = "SELECT u.name, p.title FROM user u "
                + "LEFT JOIN profile p ON u.id = p.user_id AND p.type = ? "
                + "WHERE u.status IN (?, ?) AND u.created_at BETWEEN ? AND ? "
                + "AND EXISTS (SELECT 1 FROM orders o WHERE o.user_id = u.id AND o.status = ?)";

        SqlGather result = SqlGatherHelper.analysis(sql);

        assertEquals(SqlType.SELECT, result.getSqlType());
        assertEquals(2, result.getTables().size()); // user 和 profile，EXISTS 子查询中的表不会被主解析器捕获
        assertTrue(result.getConditions().size() >= 3); // 至少包含主要的 WHERE 条件
        assertTrue(result.getParameterMappings().size() >= 5); // 至少5个参数
    }
}
