package io.github.qwzhang01.sql.tool.exception;

/**
 * SQL解析异常
 */
public class UnSuportedException extends ParseException {

    public UnSuportedException(String message) {
        super(message);
    }

    public UnSuportedException(String message, Throwable cause) {
        super(message, cause);
    }
}