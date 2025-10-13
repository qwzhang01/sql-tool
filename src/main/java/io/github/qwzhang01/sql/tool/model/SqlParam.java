package io.github.qwzhang01.sql.tool.model;

import java.util.Objects;

/**
 * 参数 占位符
 *
 * @author Avin Zhang
 * @since 1.0.0
 */
public class SqlParam {
    private String column;
    private String table;
    /**
     * 占位符在SQL中的索引位置
     */
    private Integer index;

    public SqlParam() {
    }

    public SqlParam(String column, String table, Integer index) {
        this.column = column;
        this.table = table;
        this.index = index;
    }

    public String getColumn() {
        return column;
    }

    public String getTable() {
        return table;
    }

    public Integer getIndex() {
        return index;
    }


    @Override
    public int hashCode() {
        return Objects.hash(index);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return false;
        }
        if (obj instanceof SqlParam param) {
            return Objects.equals(this.index, param.getIndex());
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return "SqlParam{" +
                "index=" + index +
                ", column='" + getColumn() + '\'' +
                ", table='" + getTable() + '\'' +
                '}';
    }
}