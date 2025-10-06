package io.github.qwzhang01.sql.tool.model;

import io.github.qwzhang01.sql.tool.enums.FieldType;
import io.github.qwzhang01.sql.tool.enums.OperatorType;
import io.github.qwzhang01.sql.tool.enums.SqlType;
import io.github.qwzhang01.sql.tool.enums.TableType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SQL analysis result containing parsed SQL information and field mappings.
 * This class provides structured access to SQL components including tables,
 * fields, conditions, and parameter mappings for different SQL statement types.
 *
 * @author avinzhang
 */
public class SqlGather {

    // Table information list
    private final List<TableInfo> tables = new ArrayList<>();
    // WHERE condition fields
    private final List<FieldCondition> conditions = new ArrayList<>();
    // UPDATE statement SET fields
    private final List<FieldCondition> setFields = new ArrayList<>();
    // INSERT statement fields
    private final List<FieldCondition> insertFields = new ArrayList<>();
    // SELECT statement fields
    private final List<FieldCondition> selectFields = new ArrayList<>();
    // Parameter placeholder to field mapping relationships (in order)
    private final List<ParameterFieldMapping> parameterMappings = new ArrayList<>();
    // Table alias mapping
    private final Map<String, String> aliasToTableMap = new HashMap<>();
    private SqlType sqlType;

    // ========== Basic Operation Methods ==========
    public SqlType getSqlType() {
        return sqlType;
    }

    public void setSqlType(SqlType sqlType) {
        this.sqlType = sqlType;
    }

    public void addTable(TableInfo tableInfo) {
        tables.add(tableInfo);
        // Maintain alias mapping
        if (tableInfo.alias() != null && !tableInfo.alias().trim().isEmpty()) {
            aliasToTableMap.put(tableInfo.alias(), tableInfo.tableName());
        }
    }

    public void addCondition(FieldCondition condition) {
        conditions.add(condition);
    }

    public void addSetField(FieldCondition setField) {
        setFields.add(setField);
    }

    public void addInsertField(FieldCondition insertField) {
        insertFields.add(insertField);
    }

    public void addSelectField(FieldCondition selectField) {
        selectFields.add(selectField);
    }

    public void addParameterMapping(ParameterFieldMapping mapping) {
        parameterMappings.add(mapping);
    }

    // ========== Getter Methods ==========

    public List<TableInfo> getTables() {
        return tables;
    }

    public List<FieldCondition> getConditions() {
        return conditions;
    }

    public List<FieldCondition> getSetFields() {
        return setFields;
    }

    public List<FieldCondition> getInsertFields() {
        return insertFields;
    }

    public List<FieldCondition> getSelectFields() {
        return selectFields;
    }

    public List<ParameterFieldMapping> getParameterMappings() {
        return parameterMappings;
    }

    public Map<String, String> getAliasToTableMap() {
        return aliasToTableMap;
    }

    /**
     * Gets all fields in the order they appear in SQL.
     * For different SQL types, field order varies:
     * - INSERT: insertFields
     * - UPDATE: setFields + conditions
     * - SELECT/DELETE: conditions
     */
    public List<FieldCondition> getAllFields() {
        List<FieldCondition> allFields = new ArrayList<>();

        switch (sqlType) {
            case INSERT:
                allFields.addAll(insertFields);
                break;
            case UPDATE:
                allFields.addAll(setFields);
                allFields.addAll(conditions);
                break;
            case SELECT:
            case DELETE:
                allFields.addAll(conditions);
                break;
        }

        return allFields;
    }

    /**
     * Gets the real table name by alias or table name.
     */
    public String getRealTableName(String aliasOrTableName) {
        return aliasToTableMap.getOrDefault(aliasOrTableName, aliasOrTableName);
    }

    /**
     * Table information record.
     */
    public record TableInfo(String tableName, String alias, TableType tableType) {

        public TableInfo(String tableName, String alias) {
            this(tableName, alias, TableType.MAIN);
        }

        public String getEffectiveAlias() {
            if (alias == null || alias.trim().isEmpty()) {
                return tableName;
            }
            return alias;
        }
    }

    /**
     * Field condition information class.
     */
    public static class FieldCondition {
        private final String tableAlias;
        private final String columnName;
        private final FieldType fieldType;
        private final OperatorType operatorType;
        private final int actualParamCount;

        public FieldCondition(String tableAlias, String columnName) {
            this(tableAlias, columnName, FieldType.CONDITION, OperatorType.SINGLE_PARAM, 1);
        }

        public FieldCondition(String tableAlias, String columnName, FieldType fieldType) {
            this(tableAlias, columnName, fieldType, OperatorType.SINGLE_PARAM, 1);
        }

        public FieldCondition(String tableAlias, String columnName, FieldType fieldType, OperatorType operatorType, int actualParamCount) {
            this.tableAlias = tableAlias;
            this.columnName = columnName;
            this.fieldType = fieldType;
            this.operatorType = operatorType;
            this.actualParamCount = actualParamCount;
        }

        public String tableAlias() {
            return tableAlias;
        }

        public String columnName() {
            return columnName;
        }

        public FieldType fieldType() {
            return fieldType;
        }

        public OperatorType operatorType() {
            return operatorType;
        }

        public int actualParamCount() {
            return actualParamCount;
        }

        /**
         * Gets the effective parameter count.
         */
        public int getEffectiveParamCount() {
            if (operatorType == OperatorType.IN_OPERATOR) {
                return actualParamCount; // For IN operator, use actual calculated parameter count
            }
            return operatorType.getParamCount();
        }
    }

    /**
     * Parameter placeholder to field mapping relationship record.
     */
    public record ParameterFieldMapping(
            int parameterIndex,     // Parameter index (starting from 0)
            String tableName,       // Table name
            String fieldName,       // Field name
            String tableAlias,      // Table alias
            FieldType fieldType     // Field type
    ) {
        /**
         * Gets the effective table identifier (prioritizes alias).
         */
        public String getEffectiveTableIdentifier() {
            return (tableAlias != null && !tableAlias.trim().isEmpty()) ? tableAlias : tableName;
        }
    }
}
