package io.github.qwzhang01.sql.tool.helper;

import io.github.qwzhang01.sql.tool.enums.FieldType;
import io.github.qwzhang01.sql.tool.enums.OperatorType;
import io.github.qwzhang01.sql.tool.enums.TableType;
import io.github.qwzhang01.sql.tool.model.*;

import java.util.Comparator;
import java.util.List;

import static io.github.qwzhang01.sql.tool.enums.OperatorType.SINGLE_PARAM;

public class SqlGatherHelper {
    /**
     * Extracts parameter placeholders from SQL statement and returns them as a list of SqlParam objects.
     * This method analyzes the SQL statement to identify parameter placeholders (?) and creates
     * corresponding SqlParam objects containing metadata about each parameter's position and context.
     *
     * <p>The method performs the following operations:
     * <ul>
     *   <li>Parses the SQL statement to extract parameter mappings</li>
     *   <li>Filters parameter mappings that contain placeholder values ("?")</li>
     *   <li>Creates SqlParam objects with field name, table name, table alias, and index information</li>
     *   <li>Re-indexes the parameters sequentially starting from 0</li>
     * </ul>
     *
     * <p>Example usage:
     * <pre>
     * String sql = "SELECT * FROM users WHERE id = ? AND name = ?";
     * List&lt;SqlParam&gt; params = SqlGatherHelper.param(sql);
     * // Returns 2 SqlParam objects with indexes 0 and 1
     * </pre>
     *
     * @param sql the SQL statement to analyze for parameter placeholders
     * @return a list of SqlParam objects representing the parameter placeholders in the SQL,
     * ordered by their appearance in the statement with sequential indexes starting from 0
     * @throws IllegalArgumentException if the SQL is null, empty, or contains only whitespace
     * @see SqlParam
     * @see #analysis(String)
     * @since 1.0.0
     */
    public static List<SqlParam> param(String sql) {
        SqlGather analysis = analysis(sql);
        List<SqlGather.ParameterFieldMapping> parameterMappings = analysis.getParameterMappings();

        List<SqlParam> list = parameterMappings.stream().filter(p -> {
            if (p.value() != null) {
                return String.valueOf(p.value()).contains("?");
            }
            return false;
        }).map(p -> {
            SqlParam sqlParam = new SqlParam();
            sqlParam.setFieldName(p.fieldName());
            sqlParam.setTableName(p.tableName());
            sqlParam.setTableAlias(p.tableAlias());
            sqlParam.setIndex(p.parameterIndex());
            return sqlParam;
        }).sorted(Comparator.comparing(SqlParam::getIndex)).toList();

        for (int i = 0; i < list.size(); i++) {
            list.get(i).setIndex(i);
        }
        return list;
    }

    /**
     * 将 SqlInfo 对象转换为 SqlAnalysisInfo 对象
     *
     * @param sql 待转换的 sql 脚本
     * @return 转换后的 SqlAnalysisInfo 对象
     * @throws IllegalArgumentException 如果 sqlInfo 为 null
     */
    public static SqlGather analysis(String sql) {
        if (sql == null || sql.isEmpty() || sql.trim().isEmpty()) {
            throw new IllegalArgumentException("SQL cannot be null");
        }

        SqlObj sqlObj = SqlParseHelper.parseSQL(sql);
        if (sqlObj == null) {
            throw new IllegalArgumentException("SQL cannot be null");
        }

        SqlGather analysisInfo = new SqlGather();
        // 设置 SQL 类型
        analysisInfo.setSqlType((sqlObj.getSqlType()));
        // 转换主表信息
        convertMainTable(sqlObj, analysisInfo);
        // 转换 JOIN 表信息
        convertJoinTables(sqlObj, analysisInfo);

        // 转换 SELECT 字段
        convertSelectFields(sqlObj, analysisInfo);

        // 转换 WHERE 条件
        convertWhereConditions(sqlObj, analysisInfo);

        // 转换 INSERT 字段
        convertInsertFields(sqlObj, analysisInfo);

        // 转换 UPDATE SET 字段
        convertUpdateSetFields(sqlObj, analysisInfo);

        // 转换参数映射
        convertParameterMappings(analysisInfo);

        return analysisInfo;
    }

    /**
     * 转换主表信息
     */
    private static void convertMainTable(SqlObj sqlObj, SqlGather analysisInfo) {
        SqlTable mainTable = sqlObj.getMainTable();
        if (mainTable != null) {
            SqlGather.TableInfo tableInfo = new SqlGather.TableInfo(mainTable.getTableName(), mainTable.getAlias(), TableType.MAIN);
            analysisInfo.addTable(tableInfo);
        }
    }

    /**
     * 转换 JOIN 表信息
     */
    private static void convertJoinTables(SqlObj sqlObj, SqlGather analysisInfo) {
        List<SqlJoin> joinTables = sqlObj.getJoinTables();
        if (joinTables != null) {
            for (SqlJoin sqlJoin : joinTables) {
                SqlGather.TableInfo tableInfo = new SqlGather.TableInfo(sqlJoin.getTableName(), sqlJoin.getAlias(), TableType.JOIN);
                analysisInfo.addTable(tableInfo);
            }
        }
    }

    /**
     * 转换 SELECT 字段
     */
    private static void convertSelectFields(SqlObj sqlObj, SqlGather analysisInfo) {
        List<SqlField> selectColumns = sqlObj.getSelectColumns();
        if (selectColumns != null) {
            for (SqlField columnInfo : selectColumns) {
                // 跳过 * 通配符
                if ("*".equals(columnInfo.getFieldName())) {
                    continue;
                }

                String tableAlias = determineTableAlias(columnInfo, analysisInfo);
                SqlGather.FieldCondition fieldCondition = new SqlGather.FieldCondition(tableAlias, columnInfo.getFieldName(), FieldType.SELECT);
                analysisInfo.addSelectField(fieldCondition);
            }
        }
    }

    /**
     * 转换 WHERE 条件
     */
    private static void convertWhereConditions(SqlObj sqlObj, SqlGather analysisInfo) {
        List<SqlCondition> sqlConditions = sqlObj.getWhereConditions();
        if (sqlConditions != null) {
            for (SqlCondition sqlCondition : sqlConditions) {
                convertWhereCondition(sqlCondition, analysisInfo);
            }
        }
    }

    /**
     * 递归转换单个 WHERE 条件
     */
    private static void convertWhereCondition(SqlCondition sqlCondition, SqlGather analysisInfo) {
        if (sqlCondition == null || sqlCondition.isEmpty()) {
            return;
        }

        // 处理简单条件
        if (sqlCondition.getLeftOperand() != null) {
            String fieldName = extractFieldName(sqlCondition.getLeftOperand());
            String tableAlias = extractTableAlias(sqlCondition.getLeftOperand(), analysisInfo);

            OperatorType operatorType = OperatorType.convertOperatorType(sqlCondition.getOperator());
            int paramCount = calculateParamCount(sqlCondition);

            SqlGather.FieldCondition fieldCondition = new SqlGather.FieldCondition(tableAlias, fieldName, FieldType.CONDITION, operatorType, paramCount, sqlCondition.getRightOperand());
            analysisInfo.addCondition(fieldCondition);
        }

        // 递归处理子条件
        List<SqlCondition> subConditions = sqlCondition.getSubConditions();
        if (subConditions != null) {
            for (SqlCondition subCondition : subConditions) {
                convertWhereCondition(subCondition, analysisInfo);
            }
        }
    }

    /**
     * 转换 INSERT 字段
     */
    private static void convertInsertFields(SqlObj sqlObj, SqlGather analysisInfo) {
        if (sqlObj.getInsertValues() == null || sqlObj.getInsertValues().isEmpty()) {
            return;
        }
        String mainTableAlias = getMainTableAlias(analysisInfo);
        for (int i = 0; i < sqlObj.getInsertValues().size(); i++) {
            SqlUpdateColumn column = sqlObj.getInsertValues().get(i);
            analysisInfo.addInsertField(new SqlGather.FieldCondition(mainTableAlias,
                    column.columnName(),
                    FieldType.INSERT, SINGLE_PARAM,
                    1,
                    column.value()));

        }
    }

    /**
     * 转换 UPDATE SET 字段
     */
    private static void convertUpdateSetFields(SqlObj sqlObj, SqlGather analysisInfo) {
        if (sqlObj.getUpdateValues() == null || sqlObj.getUpdateValues().isEmpty()) {
            return;
        }
        List<SqlUpdateColumn> updateValues = sqlObj.getUpdateValues();
        String mainTableAlias = getMainTableAlias(analysisInfo);
        for (SqlUpdateColumn column : updateValues) {
            analysisInfo.addSetField(new SqlGather.FieldCondition(mainTableAlias,
                    column.columnName(),
                    FieldType.UPDATE_SET,
                    SINGLE_PARAM,
                    1,
                    column.value()));
        }
    }

    /**
     * 转换参数映射
     */
    private static void convertParameterMappings(SqlGather analysisInfo) {
        // 根据字段顺序生成参数映射
        List<SqlGather.FieldCondition> allFields = analysisInfo.getAllFields();
        int parameterIndex = 0;

        for (SqlGather.FieldCondition field : allFields) {
            int paramCount = field.getEffectiveParamCount();
            for (int i = 0; i < paramCount; i++) {
                String tableName = analysisInfo.getRealTableName(field.tableAlias());
                SqlGather.ParameterFieldMapping mapping = new SqlGather.ParameterFieldMapping(parameterIndex++, tableName, field.columnName(), field.tableAlias(), field.fieldType(), field.getValue());
                analysisInfo.addParameterMapping(mapping);
            }
        }
    }

    /**
     * 计算参数个数
     */
    private static int calculateParamCount(SqlCondition sqlCondition) {
        Integer valueCount = sqlCondition.getValueCount();
        if (valueCount != null) {
            return valueCount;
        }

        // 根据操作符类型推断参数个数
        String operator = sqlCondition.getOperator();
        if (operator != null) {
            String op = operator.toUpperCase().trim();
            switch (op) {
                case "BETWEEN":
                case "NOT BETWEEN":
                    return 2;
                case "IS NULL":
                case "IS NOT NULL":
                    return 0;
                case "IN":
                case "NOT IN":
                    // 尝试从右操作数推断
                    Object rightOperand = sqlCondition.getRightOperand();
                    if (rightOperand instanceof List) {
                        return ((List<?>) rightOperand).size();
                    } else if (rightOperand instanceof String) {
                        String str = (String) rightOperand;
                        if (str.contains(",")) {
                            return str.split(",").length;
                        }
                    }
                    return 1;
                default:
                    return 1;
            }
        }
        return 1;
    }

    /**
     * 确定表别名
     */
    private static String determineTableAlias(SqlField columnInfo, SqlGather analysisInfo) {
        // 优先使用 ColumnInfo 中的表别名
        if (columnInfo.getTableAlias() != null && !columnInfo.getTableAlias().trim().isEmpty()) {
            return columnInfo.getTableAlias();
        }

        // 其次使用表名
        if (columnInfo.getTableName() != null && !columnInfo.getTableName().trim().isEmpty()) {
            return columnInfo.getTableName();
        }

        // 最后使用主表别名
        return getMainTableAlias(analysisInfo);
    }

    /**
     * 从字段表达式中提取字段名
     */
    private static String extractFieldName(String fieldExpression) {
        if (fieldExpression == null) {
            return null;
        }

        // 处理 table.field 格式
        if (fieldExpression.contains(".")) {
            String[] parts = fieldExpression.split("\\.");
            // 返回最后一部分作为字段名
            return parts[parts.length - 1];
        }

        return fieldExpression;
    }

    /**
     * 从字段表达式中提取表别名
     */
    private static String extractTableAlias(String fieldExpression, SqlGather analysisInfo) {
        if (fieldExpression == null) {
            return getMainTableAlias(analysisInfo);
        }

        // 处理 table.field 格式
        if (fieldExpression.contains(".")) {
            String[] parts = fieldExpression.split("\\.");
            if (parts.length >= 2) {
                // 返回第一部分作为表别名
                return parts[0];
            }
        }

        return getMainTableAlias(analysisInfo);
    }

    /**
     * 获取主表别名
     */
    private static String getMainTableAlias(SqlGather analysisInfo) {
        List<SqlGather.TableInfo> tables = analysisInfo.getTables();
        for (SqlGather.TableInfo table : tables) {
            if (table.tableType() == TableType.MAIN) {
                return table.getEffectiveAlias();
            }
        }
        return null;
    }
}
