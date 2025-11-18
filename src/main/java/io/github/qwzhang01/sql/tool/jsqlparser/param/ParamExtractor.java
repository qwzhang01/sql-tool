package io.github.qwzhang01.sql.tool.jsqlparser.param;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParamExtractor {
    private static final Pattern HASH_PARAM_PATTERN = Pattern.compile("#\\{([^}]+)\\}");

    public static String preProcessSql(String sql) {
        return preProcessSql(sql, HASH_PARAM_PATTERN);
    }

    public static String preProcessSql(String sql, Pattern pattern) {
        Matcher matcher = pattern.matcher(sql);

        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            matcher.appendReplacement(result, "?");
        }
        matcher.appendTail(result);

        return result.toString();
    }
}