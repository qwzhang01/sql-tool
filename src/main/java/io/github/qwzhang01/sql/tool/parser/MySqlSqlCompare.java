package io.github.qwzhang01.sql.tool.parser;

/**
 * @author avinzhang
 */
public class MySqlSqlCompare implements SqlCompare {
    @Override
    public boolean equal(String mark1, String mark2) {
        if (mark1 == null && mark2 == null) {
            return true;
        }
        if (mark1 == null) {
            return false;
        }
        if (mark2 == null) {
            return false;
        }
        mark1 = mark1.trim();
        mark2 = mark2.trim();

        mark1 = mark1.replaceAll("`", "").replaceAll("\"", "").replaceAll("'", "");
        mark2 = mark2.replaceAll("`", "").replaceAll("\"", "").replaceAll("'", "");

        mark1 = mark1.trim();
        mark2 = mark2.trim();

        mark1 = mark1.toUpperCase();
        mark2 = mark2.toUpperCase();
        return mark1.equals(mark2);
    }
}
