package io.github.qwzhang01.sql.tool.model;

/**
 * insert and update column ,value
 *
 * @author avinzhang
 */
public record SqlUpdateColumn(String columnName, Object value) {

    @Override
    public String toString() {
        return "SqlUpdateColumn{"
                + "columnName='" + columnName + '\''
                + ", value=" + value
                + '}';
    }
}