package io.github.qwzhang01.sql.tool.model;

/**
 * ORDER BY信息
 */
public class OrderByInfo {

    /**
     * 排序字段名
     */
    private String columnName;

    /**
     * 表名
     */
    private String tableName;

    /**
     * 表别名
     */
    private String tableAlias;

    /**
     * 排序方向
     */
    private Direction direction;

    /**
     * 排序位置（在ORDER BY子句中的位置）
     */
    private int position;

    // 构造函数
    public OrderByInfo() {
        this.direction = Direction.ASC; // 默认升序
    }

    public OrderByInfo(String columnName) {
        this.columnName = columnName;
        this.direction = Direction.ASC;
    }

    public OrderByInfo(String columnName, Direction direction) {
        this.columnName = columnName;
        this.direction = direction;
    }

    public OrderByInfo(String columnName, String tableName, Direction direction) {
        this.columnName = columnName;
        this.tableName = tableName;
        this.direction = direction;
    }

    // Getter和Setter方法
    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getTableAlias() {
        return tableAlias;
    }

    public void setTableAlias(String tableAlias) {
        this.tableAlias = tableAlias;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    @Override
    public String toString() {
        return "OrderByInfo{" +
                "columnName='" + columnName + '\'' +
                ", tableName='" + tableName + '\'' +
                ", tableAlias='" + tableAlias + '\'' +
                ", direction=" + direction +
                ", position=" + position +
                '}';
    }

    /**
     * 排序方向枚举
     */
    public enum Direction {
        ASC("ASC"),
        DESC("DESC");

        private final String sqlKeyword;

        Direction(String sqlKeyword) {
            this.sqlKeyword = sqlKeyword;
        }

        public static Direction fromString(String directionStr) {
            if (directionStr == null) return ASC;

            String normalized = directionStr.toUpperCase().trim();
            for (Direction dir : values()) {
                if (dir.getSqlKeyword().equals(normalized) || dir.name().equals(normalized)) {
                    return dir;
                }
            }
            return ASC; // 默认升序
        }

        public String getSqlKeyword() {
            return sqlKeyword;
        }
    }
}