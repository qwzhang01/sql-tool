package io.github.qwzhang01.sql.tool.parser;

import io.github.qwzhang01.sql.tool.model.JoinInfo;
import io.github.qwzhang01.sql.tool.model.SqlInfo;
import io.github.qwzhang01.sql.tool.model.WhereCondition;

import java.util.List;
import java.util.Map;

/**
 * SQL Parser interface providing comprehensive SQL parsing capabilities.
 * This interface defines the contract for parsing SQL statements into structured
 * objects and converting them back to SQL strings.
 *
 * <p>Currently implemented for MySQL syntax. Future versions will support
 * SQL Server, Oracle, PostgreSQL, and other major database systems.
 *
 * @author Avin Zhang
 * @since 1.0.0
 */
public interface SqlParser {

    /**
     * Parses a SQL statement into a structured SqlInfo object
     *
     * @param sql the SQL statement to parse
     * @return SqlInfo object containing parsed SQL components
     * @throws io.github.qwzhang01.sql.tool.exception.ParseException if parsing fails
     */
    SqlInfo parse(String sql);

    /**
     * Parses a SQL statement with parameter mapping
     *
     * @param sql        the SQL statement to parse
     * @param parameters parameter mapping for prepared statements
     * @return SqlInfo object containing parsed SQL components and parameters
     * @throws io.github.qwzhang01.sql.tool.exception.ParseException if parsing fails
     */
    SqlInfo parse(String sql, Map<String, Object> parameters);

    /**
     * Parses JOIN operations from a SQL statement or fragment
     *
     * @param sql the SQL statement containing JOIN operations
     * @return list of JoinInfo objects with detailed join analysis
     * @throws io.github.qwzhang01.sql.tool.exception.ParseException if parsing fails
     */
    List<JoinInfo> parseJoin(String sql);

    /**
     * Parses WHERE conditions from a SQL statement or fragment
     *
     * @param sql the SQL statement containing WHERE conditions
     * @return list of WhereCondition objects with detailed field analysis
     * @throws io.github.qwzhang01.sql.tool.exception.ParseException if parsing fails
     */
    List<WhereCondition> parseWhere(String sql);

    /**
     * Converts a SqlInfo object back to a SQL statement string
     *
     * @param sqlInfo the SqlInfo object to convert
     * @return the generated SQL statement
     * @throws io.github.qwzhang01.sql.tool.exception.UnSuportedException if conversion is not supported
     */
    String toSql(SqlInfo sqlInfo);

    /**
     * Gets the SQL cleaner instance for this parser
     *
     * @return the SqlCleaner implementation
     */
    SqlCleaner getCleaner();

    /**
     * Gets the SQL comparison utility for this parser
     *
     * @return the SqlCompare implementation
     */
    SqlCompare getCompare();
}