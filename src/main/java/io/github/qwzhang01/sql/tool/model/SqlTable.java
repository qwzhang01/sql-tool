package io.github.qwzhang01.sql.tool.model;

import io.github.qwzhang01.sql.tool.exception.UnSupportedException;

import java.util.Set;

/**
 * SQL table information with name, alias and nested table support
 *
 * @author Avin Zhang
 * @since 1.0.0
 */
public class SqlTable {

    private String name;
    private String alias;
    private boolean isVirtual;
    /**
     * Child tables for nested queries
     */
    private Set<SqlTable> children;

    public SqlTable() {
    }

    public SqlTable(String name, String alias, boolean isVirtual) {
        this.name = name;
        this.alias = alias;
        this.isVirtual = isVirtual;
    }

    /**
     * Get table alias by table name
     */
    public String getAlias(String table) {
        if (table == null || table.isEmpty()) {
            throw new UnSupportedException("Empty table name cannot get alias");
        }
        if (table.equalsIgnoreCase(name)) {
            return alias == null || alias.isEmpty() ? table : alias;
        }

        String name = this.name;
        if (name.contains("`")) {
            name = name.replace("`", "");
        }
        if (table.contains("`")) {
            table = table.replace("`", "");
        }
        if (table.equalsIgnoreCase(name)) {
            return alias == null || alias.isEmpty() ? table : alias;
        }

        if (children == null || children.isEmpty()) {
            return table;
        }
        for (SqlTable child : children) {
            return child.getAlias(table);
        }
        return table;
    }

    public boolean isVirtual() {
        return isVirtual;
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