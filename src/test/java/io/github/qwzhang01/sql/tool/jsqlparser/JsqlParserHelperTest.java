package io.github.qwzhang01.sql.tool.jsqlparser;

import io.github.qwzhang01.sql.tool.helper.JsqlParserHelper;
import io.github.qwzhang01.sql.tool.jsqlparser.visitor.TableFinder;
import io.github.qwzhang01.sql.tool.model.SqlParam;
import io.github.qwzhang01.sql.tool.model.SqlTable;
import net.sf.jsqlparser.JSQLParserException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class JsqlParserHelperTest {

    private static final String sql = """
                            select 
                                tmp.*,
                                (select count(*) from dept where dept.order_id = tmp.id) as c
                            from (
                                select * from users
                                join orders on users.id = orders.user_id
                                join order_items on orders.id = order_items.order_id
                            ) as tmp
                            left join dept on dept.id = tmp.dept_id and dept.order_id = ?
                            left join users u on tmp.id = u.user_id
                            where name = 'abc' and age = 123 and c = 8 
                            and tmp.d in (1, 2, 3)
                            AND c between 1 and 10
                            AND e is not null
                            AND exists (select 1 from goods where goods.order_id = tmp.id)
                            and f like 'abc%'
                            or g = 1
                            and tmp.k = ?
            """;


    @Test
    public void tableTest() throws JSQLParserException {
        // Set<SqlTable> tables = TableFinder.findTables(sql);
        Set<SqlTable> tablesOrOtherSources = TableFinder.findTablesOrOtherSources(sql);
        System.out.println(tablesOrOtherSources);

    }

    @Test
    public void testTable() {
        List<SqlTable> tables = JsqlParserHelper.getTables(sql);
        System.out.println("");
    }

    @Test
    public void getTableDeep() {
        List<SqlTable> tables = JsqlParserHelper.getTables(sql);
        System.out.println("");
    }

    @Test
    public void testParam() {
        List<SqlParam> param = JsqlParserHelper.getParam(sql);
        assertTrue(param.get(0).getFieldName().equals("order_id"));
    }

    @Test
    public void testComplex() {
        String join = "left join flow f on f.userId = users.id";
        String where = "WHERE flow.id = ? AND exists (select 1 from `product` where product.userId = users.id)";
        String result = JsqlParserHelper.addJoinAndWhere(sql, join, where);
        System.out.println(result);
    }
}
