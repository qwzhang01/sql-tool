package io.github.qwzhang01.sql.tool.helper;

import io.github.qwzhang01.sql.tool.model.*;

import java.util.List;
import java.util.Map;

/**
 * SQL信息转换工具类
 * 将 SqlParseHelper 解析的 SqlInfo 对象转换为 SqlAnalysisInfo 对象
 *
 * @author avinzhang
 * @since 1.0.0
 */
public class SqlAnalysis {

    /**
     * 将 SqlInfo 对象转换为 SqlAnalysisInfo 对象
     *
     * @param sql 待转换的 sql 脚本
     * @return 转换后的 SqlAnalysisInfo 对象
     * @throws IllegalArgumentException 如果 sqlInfo 为 null
     */
    public static SqlAnalysisInfo analysis(String sql) {
        if (sql == null || sql.isEmpty()) {
            throw new IllegalArgumentException("SQL cannot be null");
        }

        SqlInfo sqlInfo = SqlParseHelper.parseSQL(sql);

        SqlAnalysisInfo analysisInfo = new SqlAnalysisInfo();

        // 设置 SQL 类型
        analysisInfo.setSqlType(convertSqlType(sqlInfo.getSqlType()));

        // 转换主表信息
        convertMainTable(sqlInfo, analysisInfo);

        // 转换 JOIN 表信息
        convertJoinTables(sqlInfo, analysisInfo);

        // 转换 SELECT 字段
        convertSelectFields(sqlInfo, analysisInfo);

        // 转换 WHERE 条件
        convertWhereConditions(sqlInfo, analysisInfo);

        // 转换 INSERT 字段
        convertInsertFields(sqlInfo, analysisInfo);

        // 转换 UPDATE SET 字段
        convertUpdateSetFields(sqlInfo, analysisInfo);

        // 转换参数映射
        convertParameterMappings(sqlInfo, analysisInfo);

        return analysisInfo;
    }

    /**
     * 转换 SQL 类型
     */
    private static SqlAnalysisInfo.SqlType convertSqlType(SqlInfo.SqlType sqlType) {
        if (sqlType == null) {
            return null;
        }

        switch (sqlType) {
            case SELECT:
                return SqlAnalysisInfo.SqlType.SELECT;
            case INSERT:
                return SqlAnalysisInfo.SqlType.INSERT;
            case UPDATE:
                return SqlAnalysisInfo.SqlType.UPDATE;
            case DELETE:
                return SqlAnalysisInfo.SqlType.DELETE;
            default:
                // 对于其他类型（CREATE, DROP, ALTER, TRUNCATE），默认返回 SELECT
                return SqlAnalysisInfo.SqlType.SELECT;
        }
    }

    /**
     * 转换主表信息
     */
    private static void convertMainTable(SqlInfo sqlInfo, SqlAnalysisInfo analysisInfo) {
        TableInfo mainTable = sqlInfo.getMainTable();
        if (mainTable != null) {
            SqlAnalysisInfo.TableInfo tableInfo = new SqlAnalysisInfo.TableInfo(
                    mainTable.getTableName(),
                    mainTable.getAlias(),
                    SqlAnalysisInfo.TableType.MAIN
            );
            analysisInfo.addTable(tableInfo);
        }
    }

    /**
     * 转换 JOIN 表信息
     */
    private static void convertJoinTables(SqlInfo sqlInfo, SqlAnalysisInfo analysisInfo) {
        List<JoinInfo> joinTables = sqlInfo.getJoinTables();
        if (joinTables != null) {
            for (JoinInfo joinInfo : joinTables) {
                SqlAnalysisInfo.TableInfo tableInfo = new SqlAnalysisInfo.TableInfo(
                        joinInfo.getTableName(),
                        joinInfo.getAlias(),
                        SqlAnalysisInfo.TableType.JOIN
                );
                analysisInfo.addTable(tableInfo);
            }
        }
    }

    /**
     * 转换 SELECT 字段
     */
    private static void convertSelectFields(SqlInfo sqlInfo, SqlAnalysisInfo analysisInfo) {
        List<ColumnInfo> selectColumns = sqlInfo.getSelectColumns();
        if (selectColumns != null) {
            for (ColumnInfo columnInfo : selectColumns) {
                // 跳过 * 通配符
                if ("*".equals(columnInfo.getColumnName())) {
                    continue;
                }

                String tableAlias = determineTableAlias(columnInfo, analysisInfo);
                SqlAnalysisInfo.FieldCondition fieldCondition = new SqlAnalysisInfo.FieldCondition(
                        tableAlias,
                        columnInfo.getColumnName(),
                        SqlAnalysisInfo.FieldType.SELECT
                );
                analysisInfo.addSelectField(fieldCondition);
            }
        }
    }

    /**
     * 转换 WHERE 条件
     */
    private static void convertWhereConditions(SqlInfo sqlInfo, SqlAnalysisInfo analysisInfo) {
        List<WhereCondition> whereConditions = sqlInfo.getWhereConditions();
        if (whereConditions != null) {
            for (WhereCondition whereCondition : whereConditions) {
                convertWhereCondition(whereCondition, analysisInfo);
            }
        }
    }

    /**
     * 递归转换单个 WHERE 条件
     */
    private static void convertWhereCondition(WhereCondition whereCondition, SqlAnalysisInfo analysisInfo) {
        if (whereCondition == null || whereCondition.isEmpty()) {
            return;
        }

        // 处理简单条件
        if (whereCondition.getLeftOperand() != null) {
            String fieldName = extractFieldName(whereCondition.getLeftOperand());
            String tableAlias = extractTableAlias(whereCondition.getLeftOperand(), analysisInfo);

            SqlAnalysisInfo.OperatorType operatorType = convertOperatorType(whereCondition.getOperator());
            int paramCount = calculateParamCount(whereCondition);

            SqlAnalysisInfo.FieldCondition fieldCondition = new SqlAnalysisInfo.FieldCondition(
                    tableAlias,
                    fieldName,
                    SqlAnalysisInfo.FieldType.CONDITION,
                    operatorType,
                    paramCount
            );
            analysisInfo.addCondition(fieldCondition);
        }

        // 递归处理子条件
        List<WhereCondition> subConditions = whereCondition.getSubConditions();
        if (subConditions != null) {
            for (WhereCondition subCondition : subConditions) {
                convertWhereCondition(subCondition, analysisInfo);
            }
        }
    }

    /**
     * 转换 INSERT 字段
     */
    private static void convertInsertFields(SqlInfo sqlInfo, SqlAnalysisInfo analysisInfo) {
        List<String> insertColumns = sqlInfo.getInsertColumns();
        if (insertColumns != null) {
            String mainTableAlias = getMainTableAlias(analysisInfo);
            for (String columnName : insertColumns) {
                SqlAnalysisInfo.FieldCondition fieldCondition = new SqlAnalysisInfo.FieldCondition(
                        mainTableAlias,
                        columnName,
                        SqlAnalysisInfo.FieldType.INSERT
                );
                analysisInfo.addInsertField(fieldCondition);
            }
        }
    }

    /**
     * 转换 UPDATE SET 字段
     */
    private static void convertUpdateSetFields(SqlInfo sqlInfo, SqlAnalysisInfo analysisInfo) {
        Map<String, Object> updateValues = sqlInfo.getUpdateValues();
        if (updateValues != null) {
            String mainTableAlias = getMainTableAlias(analysisInfo);
            for (String columnName : updateValues.keySet()) {
                SqlAnalysisInfo.FieldCondition fieldCondition = new SqlAnalysisInfo.FieldCondition(
                        mainTableAlias,
                        columnName,
                        SqlAnalysisInfo.FieldType.UPDATE_SET
                );
                analysisInfo.addSetField(fieldCondition);
            }
        }
    }

    /**
     * 转换参数映射
     */
    private static void convertParameterMappings(SqlInfo sqlInfo, SqlAnalysisInfo analysisInfo) {
        // 根据字段顺序生成参数映射
        List<SqlAnalysisInfo.FieldCondition> allFields = analysisInfo.getAllFields();
        int parameterIndex = 0;

        for (SqlAnalysisInfo.FieldCondition field : allFields) {
            int paramCount = field.getEffectiveParamCount();
            for (int i = 0; i < paramCount; i++) {
                String tableName = analysisInfo.getRealTableName(field.tableAlias());
                SqlAnalysisInfo.ParameterFieldMapping mapping = new SqlAnalysisInfo.ParameterFieldMapping(
                        parameterIndex++,
                        tableName,
                        field.columnName(),
                        field.tableAlias(),
                        field.fieldType()
                );
                analysisInfo.addParameterMapping(mapping);
            }
        }
    }

    /**
     * 转换操作符类型
     */
    private static SqlAnalysisInfo.OperatorType convertOperatorType(String operator) {
        if (operator == null) {
            return SqlAnalysisInfo.OperatorType.SINGLE_PARAM;
        }

        String op = operator.toUpperCase().trim();
        switch (op) {
            case "IN":
            case "NOT IN":
                return SqlAnalysisInfo.OperatorType.IN_OPERATOR;
            case "BETWEEN":
            case "NOT BETWEEN":
                return SqlAnalysisInfo.OperatorType.BETWEEN_OPERATOR;
            case "IS NULL":
            case "IS NOT NULL":
                return SqlAnalysisInfo.OperatorType.NO_PARAM;
            default:
                return SqlAnalysisInfo.OperatorType.SINGLE_PARAM;
        }
    }

    /**
     * 计算参数个数
     */
    private static int calculateParamCount(WhereCondition whereCondition) {
        Integer valueCount = whereCondition.getValueCount();
        if (valueCount != null) {
            return valueCount;
        }

        // 根据操作符类型推断参数个数
        String operator = whereCondition.getOperator();
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
                    Object rightOperand = whereCondition.getRightOperand();
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
    private static String determineTableAlias(ColumnInfo columnInfo, SqlAnalysisInfo analysisInfo) {
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
            return parts[parts.length - 1]; // 返回最后一部分作为字段名
        }

        return fieldExpression;
    }

    /**
     * 从字段表达式中提取表别名
     */
    private static String extractTableAlias(String fieldExpression, SqlAnalysisInfo analysisInfo) {
        if (fieldExpression == null) {
            return getMainTableAlias(analysisInfo);
        }

        // 处理 table.field 格式
        if (fieldExpression.contains(".")) {
            String[] parts = fieldExpression.split("\\.");
            if (parts.length >= 2) {
                return parts[0]; // 返回第一部分作为表别名
            }
        }

        return getMainTableAlias(analysisInfo);
    }

    /**
     * 获取主表别名
     */
    private static String getMainTableAlias(SqlAnalysisInfo analysisInfo) {
        List<SqlAnalysisInfo.TableInfo> tables = analysisInfo.getTables();
        for (SqlAnalysisInfo.TableInfo table : tables) {
            if (table.tableType() == SqlAnalysisInfo.TableType.MAIN) {
                return table.getEffectiveAlias();
            }
        }
        return null;
    }
}