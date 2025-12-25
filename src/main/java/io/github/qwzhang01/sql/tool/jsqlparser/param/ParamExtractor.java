package io.github.qwzhang01.sql.tool.jsqlparser.param;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parameter extractor utility for pre-processing SQL statements.
 * This class converts custom parameter placeholders (like MyBatis-style #{param})
 * to standard JDBC placeholders (?).
 *
 * @author Avin Zhang
 * @since 1.0.0
 */
public class ParamExtractor {
    /**
     * Default pattern for matching MyBatis-style parameter placeholders: #{paramName}
     */
    private static final Pattern HASH_PARAM_PATTERN = Pattern.compile("#\\{([^}]+)\\}");

    /**
     * Pre-processes SQL by converting #{param} style placeholders to ? placeholders
     *
     * @param sql the SQL statement with custom placeholders
     * @return the SQL with standard JDBC ? placeholders
     */
    public static String preProcessSql(String sql) {
        return preProcessSql(sql, HASH_PARAM_PATTERN);
    }

    /**
     * Pre-processes SQL by converting custom parameter placeholders to ? placeholders
     * using the provided regex pattern
     *
     * @param sql     the SQL statement with custom placeholders
     * @param pattern the regex pattern to match custom parameter placeholders
     * @return the SQL with standard JDBC ? placeholders
     */
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