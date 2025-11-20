package io.github.qwzhang01.sql.tool.exception;


/**
 * Exception for illegal SQL that cannot be parsed or converted
 *
 * @author avinzhang
 */
public class SqlIllegalException extends ParseException {
    private String sql;

    public SqlIllegalException(String message, Throwable cause, String sql) {
        super(message + ": " + sql, cause);
        this.sql = sql;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }
}