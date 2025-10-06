package io.github.qwzhang01.sql.tool.model;

/**
 * 参数 占位符
 *
 * @author Avin Zhang
 * @since 1.0.0
 */
public class SqlParam extends SqlField {
    /**
     * 占位符在SQL中的索引位置
     */
    private Integer index;

    /**
     * 占位符类型，表示占位符出现的SQL子句类型
     */

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }


    @Override
    public String toString() {
        return "SqlParam{" +
                "index=" + index +
                ", fieldName='" + getFieldName() + '\'' +
                ", tableName='" + getTableName() + '\'' +
                ", tableAlias='" + getTableAlias() + '\'' +
                ", alias='" + getAlias() + '\'' +
                '}';
    }
}