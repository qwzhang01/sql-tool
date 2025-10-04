package io.github.qwzhang01.sql.tool.parser;

/**
 * SQL 字段相等判断
 * <p>
 * todo 目前只实现了 MySQL 的，未来需要实现 SQL Server、Oracle等主流数据库
 *
 * @author avinzhang
 */
public interface SqlCompare {
    boolean equal(String mark1, String mark2);
}