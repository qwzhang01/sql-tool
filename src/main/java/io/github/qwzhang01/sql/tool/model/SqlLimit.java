package io.github.qwzhang01.sql.tool.model;

/**
 * LIMIT clause information for result set pagination.
 * This class represents the LIMIT and OFFSET values used to control
 * the number of rows returned and the starting position.
 *
 * @author Avin Zhang
 * @since 1.0.0
 */
public class SqlLimit {

    /**
     * The number of rows to skip from the beginning (OFFSET value)
     */
    private long offset;

    /**
     * The maximum number of rows to return (LIMIT value)
     */
    private long limit;

    /**
     * Default constructor
     */
    public SqlLimit() {
    }

    /**
     * Constructor with limit only, offset defaults to 0
     *
     * @param limit the maximum number of rows to return
     */
    public SqlLimit(long limit) {
        this.limit = limit;
        this.offset = 0;
    }

    /**
     * Constructor with both offset and limit
     *
     * @param offset the number of rows to skip
     * @param limit  the maximum number of rows to return
     */
    public SqlLimit(long offset, long limit) {
        this.offset = offset;
        this.limit = limit;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public long getLimit() {
        return limit;
    }

    public void setLimit(long limit) {
        this.limit = limit;
    }

    @Override
    public String toString() {
        return "LimitInfo{" +
                "offset=" + offset +
                ", limit=" + limit +
                '}';
    }
}