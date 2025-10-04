package io.github.qwzhang01.sql.tool.parser;

import io.github.qwzhang01.sql.tool.model.SqlInfo;

import java.util.Map;

/**
 * SQL解析器接口
 * <p>
 * todo 目前只实现了 MySQL 的，未来需要实现 SQL Server、Oracle等主流数据库
 *
 * @author avinzhang
 */
public interface SqlParser {

    /**
     * 解析SQL语句
     *
     * @param sql SQL语句
     * @return SQL信息对象
     */
    SqlInfo parse(String sql);

    /**
     * 解析SQL语句（带参数）
     *
     * @param sql        SQL语句
     * @param parameters 参数映射
     * @return SQL信息对象
     */
    SqlInfo parse(String sql, Map<String, Object> parameters);

    /**
     * 将SqlInfo对象转换为SQL语句
     *
     * @param sqlInfo SQL信息对象
     * @return SQL语句
     */
    String toSql(SqlInfo sqlInfo);
}