package io.github.qwzhang01.sql.tool.model;

import java.util.Set;

/**
 * Table information class representing database table metadata and references.
 * This class stores comprehensive information about tables including names,
 * aliases, and hierarchical database/schema structure.
 *
 * @author Avin Zhang
 * @since 1.0.0
 */
public class SqlTable {

    /**
     * The table name as it appears in the database
     */
    private String name;

    /**
     * The table alias used in SQL statements
     */
    private String alias;


    /**
     * 嵌套查询，只有别名，没有名字，包含的子表数组
     */
    private Set<SqlTable> children;

    /**
     * Default constructor
     */
    public SqlTable() {
    }

    /**
     * Constructor with table name only
     *
     * @param name the name of the table
     */
    public SqlTable(String name) {
        this.name = name;
    }

    /**
     * Constructor with table name and alias
     *
     * @param name  the name of the table
     * @param alias the table alias
     */
    public SqlTable(String name, String alias) {
        this.name = name;
        this.alias = alias;
    }

    public Set<SqlTable> getChildren() {
        return children;
    }

    public void setChildren(Set<SqlTable> children) {
        this.children = children;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }


    @Override
    public int hashCode() {
        String key = name;
        if (alias != null && !alias.isEmpty()) {
            key += alias;
        }
        return key.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof SqlTable table) {
            if (!table.getName().equals(name)) {
                return false;
            }
            if (alias == null && table.getAlias() == null) {
                return true;
            }
            if (alias == null) {
                return false;
            }
            if (table.getAlias() == null) {
                return false;
            }
            return alias.equals(table.getAlias());
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return "SqlTable{" +
                "name='" + name + '\'' +
                ", alias='" + alias + '\'' +
                ", children=" + children +
                '}';
    }
}