package io.github.qwzhang01.sql.tool.parser;

/**
 * SQL Cleaner interface for removing comments and normalizing SQL statements.
 * This interface provides methods to clean SQL statements by removing comments,
 * extra whitespace, and formatting while preserving SQL semantics.
 *
 * <p>Supported comment types:
 * <ul>
 * <li>Single-line comments: -- comment content</li>
 * <li>Multi-line comments: "\/**\/" </li>
 * </ul>
 *
 * <p>Features:
 * <ul>
 * <li>Correctly handles comment symbols within string literals (won't remove them)</li>
 * <li>Removes excessive whitespace and line breaks</li>
 * <li>Maintains SQL statement semantic integrity</li>
 * <li>Supports database-specific escape character handling</li>
 * </ul>
 *
 * @author Avin Zhang
 * @since 1.0.0
 */
public interface SqlCleaner {

    /**
     * Cleans SQL statement by removing comments and excessive whitespace
     *
     * @param sql the original SQL statement
     * @return the cleaned SQL statement
     */
    String cleanSql(String sql);

    /**
     * Cleans and formats SQL as a single line with proper spacing around keywords
     *
     * @param sql the original SQL statement
     * @return the cleaned and formatted SQL statement
     */
    String cleanAndFormatSql(String sql);

    /**
     * Checks if SQL contains any comments
     *
     * @param sql the SQL statement to check
     * @return true if the SQL contains comments
     */
    boolean containsComments(String sql);

    /**
     * Removes all comments from SQL while preserving original formatting
     *
     * @param sql the original SQL statement
     * @return the SQL statement with comments removed
     */
    String removeCommentsOnly(String sql);
}
