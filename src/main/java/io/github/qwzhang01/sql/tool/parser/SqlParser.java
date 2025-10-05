package io.github.qwzhang01.sql.tool.parser;

import io.github.qwzhang01.sql.tool.exception.UnSupportedException;
import io.github.qwzhang01.sql.tool.model.SqlCondition;
import io.github.qwzhang01.sql.tool.model.SqlJoin;
import io.github.qwzhang01.sql.tool.model.SqlObj;

import java.util.List;

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
    SqlObj parse(String sql);

    /**
     * Parses JOIN operations from a SQL statement or fragment
     *
     * @param sql the SQL statement containing JOIN operations
     * @return list of JoinInfo objects with detailed join analysis
     * @throws io.github.qwzhang01.sql.tool.exception.ParseException if parsing fails
     */
    List<SqlJoin> parseJoin(String sql);

    /**
     * Parses WHERE conditions from a SQL statement or fragment
     *
     * @param sql the SQL statement containing WHERE conditions
     * @return list of WhereCondition objects with detailed field analysis
     * @throws io.github.qwzhang01.sql.tool.exception.ParseException if parsing fails
     */
    List<SqlCondition> parseWhere(String sql);

    /**
     * Converts a SqlInfo object back to a SQL statement string
     *
     * @param sqlObj the SqlInfo object to convert
     * @return the generated SQL statement
     * @throws UnSupportedException if conversion is not supported
     */
    String toSql(SqlObj sqlObj);

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