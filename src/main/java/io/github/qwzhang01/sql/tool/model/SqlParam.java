package io.github.qwzhang01.sql.tool.model;

/**
 * 参数 占位符
 *
 * @author Avin Zhang
 * @since 1.0.0
 */
public class SqlParam extends SqlField {
    private Integer index;

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }
}