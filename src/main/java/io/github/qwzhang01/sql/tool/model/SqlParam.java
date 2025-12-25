package io.github.qwzhang01.sql.tool.model;

import java.util.Objects;

/**
 * Represents a SQL parameter placeholder (?) and its associated metadata.
 * This class captures information about parameter placeholders in prepared statements,
 * including their position, the column they correspond to, and the table they belong to.
 *
 * @author Avin Zhang
 * @since 1.0.0
 */
public class SqlParam {
    /**
     * The column name associated with this parameter
     */
    private String column;
    
    /**
     * The table name associated with this parameter
     */
    private String table;
    
    /**
     * The index position of this placeholder in the SQL statement (0-based)
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