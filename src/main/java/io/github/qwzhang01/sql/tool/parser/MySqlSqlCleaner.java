package io.github.qwzhang01.sql.tool.parser;

/**
 * MySQL SQL清理器
 *
 * @author avinzhang
 */
public class MySqlSqlCleaner implements SqlCleaner {

    /**
     * 清理SQL语句，移除注释和多余的空白字符
     *
     * @param sql 原始SQL语句
     * @return 清理后的SQL语句
     */
    @Override
    public String cleanSql(String sql) {
        if (sql == null) {
            return null;
        }
        sql = sql.trim();
        if (sql.isEmpty()) {
            return sql;
        }

        StringBuilder result = new StringBuilder();
        int length = sql.length();
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;
        boolean inSingleLineComment = false;
        boolean inMultiLineComment = false;
        boolean lastCharWasSpace = false;

        for (int i = 0; i < length; i++) {
            char current = sql.charAt(i);
            char next = (i + 1 < length) ? sql.charAt(i + 1) : '\0';

            // 处理字符串字面量
            if (!inSingleLineComment && !inMultiLineComment) {
                // 处理单引号字符串
                if (current == '\'' && !inDoubleQuote) {
                    // 检查是否是转义的单引号
                    boolean isEscaped = false;

                    // 检查反斜杠转义 \'
                    if (i > 0 && sql.charAt(i - 1) == '\\') {
                        // 需要检查反斜杠本身是否被转义
                        int backslashCount = 0;
                        for (int j = i - 1; j >= 0 && sql.charAt(j) == '\\'; j--) {
                            backslashCount++;
                        }
                        // 如果反斜杠数量是奇数，则当前引号被转义
                        isEscaped = (backslashCount % 2 == 1);
                    }

                    // 检查双单引号转义 ''
                    if (!isEscaped && inSingleQuote && next == '\'') {
                        // 这是双单引号转义，跳过这两个引号
                        result.append(current);
                        result.append(next);
                        lastCharWasSpace = false;
                        i++; // 跳过下一个引号
                        continue;
                    }

                    if (!isEscaped) {
                        inSingleQuote = !inSingleQuote;
                    }
                    result.append(current);
                    lastCharWasSpace = false;
                    continue;
                }

                // 处理双引号字符串
                if (current == '"' && !inSingleQuote) {
                    // 检查是否是转义的双引号
                    boolean isEscaped = false;

                    // 检查反斜杠转义 \"
                    if (i > 0 && sql.charAt(i - 1) == '\\') {
                        // 需要检查反斜杠本身是否被转义
                        int backslashCount = 0;
                        for (int j = i - 1; j >= 0 && sql.charAt(j) == '\\'; j--) {
                            backslashCount++;
                        }
                        // 如果反斜杠数量是奇数，则当前引号被转义
                        isEscaped = (backslashCount % 2 == 1);
                    }

                    // 检查双双引号转义 ""
                    if (!isEscaped && inDoubleQuote && next == '"') {
                        // 这是双双引号转义，跳过这两个引号
                        result.append(current);
                        result.append(next);
                        lastCharWasSpace = false;
                        i++; // 跳过下一个引号
                        continue;
                    }

                    if (!isEscaped) {
                        inDoubleQuote = !inDoubleQuote;
                    }
                    result.append(current);
                    lastCharWasSpace = false;
                    continue;
                }
            }

            // 如果在字符串内，直接添加字符
            if (inSingleQuote || inDoubleQuote) {
                result.append(current);
                lastCharWasSpace = false;
                continue;
            }

            // 处理单行注释 --
            if (!inMultiLineComment && current == '-' && next == '-') {
                inSingleLineComment = true;
                i++; // 跳过下一个 -
                continue;
            }

            // 处理多行注释开始 /* (只有在不在单行注释中时才处理)
            if (!inSingleLineComment && !inMultiLineComment && current == '/' && next == '*') {
                inMultiLineComment = true;
                i++; // 跳过下一个 *
                continue;
            }

            // 处理多行注释结束 */ (只有在多行注释中时才处理)
            if (inMultiLineComment && current == '*' && next == '/') {
                inMultiLineComment = false;
                i++; // 跳过下一个 /
                continue;
            }

            // 处理单行注释结束（换行符）
            if (inSingleLineComment && (current == '\n' || current == '\r')) {
                inSingleLineComment = false;
                // 保留一个空格代替换行
                if (!result.isEmpty() && !lastCharWasSpace) {
                    result.append(' ');
                    lastCharWasSpace = true;
                }
                continue;
            }

            // 跳过注释内容
            if (inSingleLineComment || inMultiLineComment) {
                continue;
            }

            // 处理空白字符
            if (Character.isWhitespace(current)) {
                // 将连续的空白字符替换为单个空格
                if (!result.isEmpty() && !lastCharWasSpace) {
                    result.append(' ');
                    lastCharWasSpace = true;
                }
            } else {
                result.append(current);
                lastCharWasSpace = false;
            }
        }

        String resultSql = result.toString().trim().replaceAll("`", "");
        if (resultSql.endsWith(";")) {
            resultSql = resultSql.substring(0, resultSql.length() - 1);
        }
        return resultSql;
    }

    /**
     * 清理SQL并格式化为单行，在关键字周围添加适当空格
     *
     * @param sql 原始SQL语句
     * @return 清理并格式化后的SQL语句
     */
    @Override
    public String cleanAndFormatSql(String sql) {
        String cleaned = cleanSql(sql);

        if (cleaned == null || cleaned.isEmpty()) {
            return cleaned;
        }

        // 在关键字前后添加适当的空格（不区分大小写）
        cleaned = cleaned.replaceAll("(?i)\\b(SELECT|FROM|WHERE|JOIN|INNER|LEFT|RIGHT|FULL|OUTER|ON|GROUP|ORDER|BY|HAVING|UNION|INSERT|UPDATE|DELETE|CREATE|ALTER|DROP|TABLE|INDEX|VIEW|INTO|VALUES|SET|AND|OR|NOT|IN|EXISTS|BETWEEN|LIKE|IS|NULL|ASC|DESC|DISTINCT|ALL|ANY|SOME|CASE|WHEN|THEN|ELSE|END)\\b", " $1 ");

        // 清理多余的空格
        cleaned = cleaned.replaceAll("\\s+", " ").trim();

        // 清理操作符周围的空格
        cleaned = cleaned.replaceAll("\\s*([=<>!]+)\\s*", " $1 ");
        cleaned = cleaned.replaceAll("\\s*([(),;])\\s*", "$1 ");

        // 最终清理
        cleaned = cleaned.replaceAll("\\s+", " ").trim();

        return cleaned;
    }

    /**
     * 检查SQL是否包含注释
     *
     * @param sql SQL语句
     * @return 如果包含注释返回true
     */
    @Override
    public boolean containsComments(String sql) {
        if (sql == null || sql.isEmpty()) {
            return false;
        }

        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;
        int length = sql.length();

        for (int i = 0; i < length - 1; i++) {
            char current = sql.charAt(i);
            char next = sql.charAt(i + 1);

            // 处理字符串字面量
            if (current == '\'' && !inDoubleQuote) {
                // 检查是否是转义的单引号
                boolean isEscaped = false;

                // 检查反斜杠转义
                if (i > 0 && sql.charAt(i - 1) == '\\') {
                    int backslashCount = 0;
                    for (int j = i - 1; j >= 0 && sql.charAt(j) == '\\'; j--) {
                        backslashCount++;
                    }
                    isEscaped = (backslashCount % 2 == 1);
                }

                // 检查双单引号转义
                if (!isEscaped && inSingleQuote && i + 1 < length && sql.charAt(i + 1) == '\'') {
                    i++; // 跳过下一个引号
                    continue;
                }

                if (!isEscaped) {
                    inSingleQuote = !inSingleQuote;
                }
                continue;
            }
            if (current == '"' && !inSingleQuote) {
                // 检查是否是转义的双引号
                boolean isEscaped = false;

                // 检查反斜杠转义
                if (i > 0 && sql.charAt(i - 1) == '\\') {
                    int backslashCount = 0;
                    for (int j = i - 1; j >= 0 && sql.charAt(j) == '\\'; j--) {
                        backslashCount++;
                    }
                    isEscaped = (backslashCount % 2 == 1);
                }

                // 检查双双引号转义
                if (!isEscaped && inDoubleQuote && i + 1 < length && sql.charAt(i + 1) == '"') {
                    i++; // 跳过下一个引号
                    continue;
                }

                if (!isEscaped) {
                    inDoubleQuote = !inDoubleQuote;
                }
                continue;
            }

            // 如果在字符串内，跳过
            if (inSingleQuote || inDoubleQuote) {
                continue;
            }

            // 检查注释
            if ((current == '-' && next == '-') || (current == '/' && next == '*')) {
                return true;
            }
        }

        return false;
    }

    /**
     * 移除SQL中的所有注释但保留原始格式
     *
     * @param sql 原始SQL语句
     * @return 移除注释后的SQL语句
     */
    @Override
    public String removeCommentsOnly(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            return sql;
        }

        StringBuilder result = new StringBuilder();
        int length = sql.length();
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;
        boolean inSingleLineComment = false;
        boolean inMultiLineComment = false;

        for (int i = 0; i < length; i++) {
            char current = sql.charAt(i);
            char next = (i + 1 < length) ? sql.charAt(i + 1) : '\0';

            // 处理字符串字面量
            if (!inSingleLineComment && !inMultiLineComment) {
                if (current == '\'' && !inDoubleQuote) {
                    // 检查是否是转义的单引号
                    boolean isEscaped = false;

                    // 检查反斜杠转义
                    if (i > 0 && sql.charAt(i - 1) == '\\') {
                        int backslashCount = 0;
                        for (int j = i - 1; j >= 0 && sql.charAt(j) == '\\'; j--) {
                            backslashCount++;
                        }
                        isEscaped = (backslashCount % 2 == 1);
                    }

                    // 检查双单引号转义
                    if (!isEscaped && inSingleQuote && next == '\'') {
                        result.append(current);
                        result.append(next);
                        i++; // 跳过下一个引号
                        continue;
                    }

                    if (!isEscaped) {
                        inSingleQuote = !inSingleQuote;
                    }
                    result.append(current);
                    continue;
                }
                if (current == '"' && !inSingleQuote) {
                    // 检查是否是转义的双引号
                    boolean isEscaped = false;

                    // 检查反斜杠转义
                    if (i > 0 && sql.charAt(i - 1) == '\\') {
                        int backslashCount = 0;
                        for (int j = i - 1; j >= 0 && sql.charAt(j) == '\\'; j--) {
                            backslashCount++;
                        }
                        isEscaped = (backslashCount % 2 == 1);
                    }

                    // 检查双双引号转义
                    if (!isEscaped && inDoubleQuote && next == '"') {
                        result.append(current);
                        result.append(next);
                        i++; // 跳过下一个引号
                        continue;
                    }

                    if (!isEscaped) {
                        inDoubleQuote = !inDoubleQuote;
                    }
                    result.append(current);
                    continue;
                }
            }

            // 如果在字符串内，直接添加字符
            if (inSingleQuote || inDoubleQuote) {
                result.append(current);
                continue;
            }

            // 处理单行注释 --
            if (!inMultiLineComment && current == '-' && next == '-') {
                inSingleLineComment = true;
                i++; // 跳过下一个 -
                continue;
            }

            // 处理多行注释开始 /* (只有在不在单行注释中时才处理)
            if (!inSingleLineComment && !inMultiLineComment && current == '/' && next == '*') {
                inMultiLineComment = true;
                i++; // 跳过下一个 *
                continue;
            }

            // 处理多行注释结束 */ (只有在多行注释中时才处理)
            if (inMultiLineComment && current == '*' && next == '/') {
                inMultiLineComment = false;
                i++; // 跳过下一个 /
                continue;
            }

            // 处理单行注释结束（换行符）
            if (inSingleLineComment && (current == '\n' || current == '\r')) {
                inSingleLineComment = false;
                result.append(current); // 保留换行符
                continue;
            }

            // 跳过注释内容
            if (inSingleLineComment || inMultiLineComment) {
                continue;
            }

            // 添加非注释字符
            result.append(current);
        }

        return result.toString();
    }
}
