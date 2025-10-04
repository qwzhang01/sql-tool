package io.github.qwzhang01.sql.tool.model;

/**
 * LIMIT信息
 */
public class LimitInfo {

    /**
     * 偏移量
     */
    private long offset;

    /**
     * 限制数量
     */
    private long limit;

    public LimitInfo() {
    }

    public LimitInfo(long limit) {
        this.limit = limit;
        this.offset = 0;
    }

    public LimitInfo(long offset, long limit) {
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