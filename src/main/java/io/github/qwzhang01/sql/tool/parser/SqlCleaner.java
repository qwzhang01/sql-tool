package io.github.qwzhang01.sql.tool.parser;

/**
 * SQL清理器 - 用于清理SQL语句中的注释和多余空白字符
 * <p>
 * 支持的注释类型:
 * 1. 单行注释: -- 注释内容
 * 2. 多行注释:
 * <p>
 * 功能:
 * - 正确处理字符串字面量中的注释符号(不会误删)
 * - 清理多余的空白字符和换行符
 * - 保持SQL语句的语义完整性
 *
 * @author avinzhang
 */
public interface SqlCleaner {
    /**
     * 清理SQL语句，移除注释和多余的空白字符
     *
     * @param sql 原始SQL语句
     * @return 清理后的SQL语句
     */
    String cleanSql(String sql);

    /**
     * 清理SQL并格式化为单行，在关键字周围添加适当空格
     *
     * @param sql 原始SQL语句
     * @return 清理并格式化后的SQL语句
     */
    String cleanAndFormatSql(String sql);

    /**
     * 检查SQL是否包含注释
     *
     * @param sql SQL语句
     * @return 如果包含注释返回true
     */
    boolean containsComments(String sql);

    /**
     * 移除SQL中的所有注释但保留原始格式
     *
     * @param sql 原始SQL语句
     * @return 移除注释后的SQL语句
     */
    String removeCommentsOnly(String sql);
}
