package io.github.qwzhang01.sql.tool.jsqlparser;

import io.github.qwzhang01.sql.tool.helper.ParserHelper;
import io.github.qwzhang01.sql.tool.jsqlparser.visitor.ParamFinder;
import io.github.qwzhang01.sql.tool.jsqlparser.visitor.TableFinder;
import io.github.qwzhang01.sql.tool.model.SqlParam;
import io.github.qwzhang01.sql.tool.model.SqlTable;
import io.github.qwzhang01.sql.tool.wrapper.SqlParser;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.ParseException;
import net.sf.jsqlparser.statement.Statement;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static io.github.qwzhang01.sql.tool.jsqlparser.param.ParamExtractor.preProcessSql;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ParserHelperTest {

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
        List<SqlTable> tablesOrOtherSources = ParserHelper.getTables(sql);
        System.out.println(tablesOrOtherSources);

    }

    @Test
    public void testParam() {
        List<SqlParam> param = ParserHelper.getParam(sql);
        assertTrue(param.get(0).getColumn().equals("order_id"));
    }

    @Test
    public void testComplete() {
        String join = "left join flow f on f.userId = users.id" +
                " left join users u1 on tmp.id = u1.user_id";
        String where = "WHERE f.id = ? AND exists (select 1 from `product` where product.userId = users.id)";
        String result = ParserHelper.addJoinAndWhere(sql, join, where);
        System.out.println(result);
    }

    @Test
    public void testComplex() throws ParseException {
        String sqlStr = "SELECT  e.id\n" +
                "        , e.code\n" +
                "        , e.review_type\n" +
                "        , e.review_object\n" +
                "        , e.review_first_datetime AS reviewfirsttime\n" +
                "        , e.review_latest_datetime AS reviewnewtime\n" +
                "        , e.risk_event\n" +
                "        , e.risk_detail\n" +
                "        , e.risk_grade\n" +
                "        , e.risk_status\n" +
                "        , If( e.deal_type IS NULL\n" +
                "            OR e.deal_type = '', '--', e.deal_type ) AS dealtype\n" +
                "        , e.deal_result\n" +
                "        , If( e.deal_remark IS NULL\n" +
                "            OR e.deal_remark = '', '--', e.deal_remark ) AS dealremark\n" +
                "        , e.is_deleted\n" +
                "        , e.review_object_id\n" +
                "        , e.archive_id\n" +
                "        , e.feature AS featurename\n" +
                "        , Ifnull( ( SELECT real_name\n" +
                "                    FROM bladex.blade_user\n" +
                "                    WHERE id = e.review_first_user ), ( SELECT DISTINCT\n" +
                "                                                            real_name\n" +
                "                                                        FROM app_sys.asys_uniapp_rn_auth\n"
                +
                "                                                        WHERE uniapp_user_id = e.review_first_user\n"
                +
                "                                                            AND is_disable = 0 ) ) AS reviewfirstuser\n"
                +
                "        , Ifnull( ( SELECT real_name\n" +
                "                    FROM bladex.blade_user\n" +
                "                    WHERE id = e.review_latest_user ), (    SELECT DISTINCT\n" +
                "                                                                real_name\n" +
                "                                                            FROM app_sys.asys_uniapp_rn_auth\n"
                +
                "                                                            WHERE uniapp_user_id = e.review_latest_user\n"
                +
                "                                                                AND is_disable = 0 ) ) AS reviewnewuser\n"
                +
                "        , If( ( SELECT real_name\n" +
                "                FROM bladex.blade_user\n" +
                "                WHERE id = e.deal_user ) IS NOT NULL\n" +
                "            AND e.deal_user != - 9999, (    SELECT real_name\n" +
                "                                            FROM bladex.blade_user\n" +
                "                                            WHERE id = e.deal_user ), '--' ) AS dealuser\n"
                +
                "        , CASE\n" +
                "                WHEN 'COMPANY'\n" +
                "                    THEN Concat( (  SELECT ar.customer_name\n" +
                "                                    FROM mtp_cs.mtp_rsk_cust_archive ar\n" +
                "                                    WHERE ar.is_deleted = 0\n" +
                "                                        AND ar.id = e.archive_id ), If( (   SELECT alias\n"
                +
                "                                                                            FROM web_crm.wcrm_customer\n"
                +
                "                                                                            WHERE id = e.customer_id ) = ''\n"
                +
                "                OR (    SELECT alias\n" +
                "                        FROM web_crm.wcrm_customer\n" +
                "                        WHERE id = e.customer_id ) IS NULL, ' ', Concat( '（', ( SELECT alias\n"
                +
                "                                                                                FROM web_crm.wcrm_customer\n"
                +
                "                                                                                WHERE id = e.customer_id ), '）' ) ) )\n"
                +
                "                WHEN 'EMPLOYEE'\n" +
                "                    THEN (  SELECT Concat( auth.real_name, ' ', auth.phone )\n" +
                "                            FROM app_sys.asys_uniapp_rn_auth auth\n" +
                "                            WHERE auth.is_disable = 0\n" +
                "                                AND auth.uniapp_user_id = e.uniapp_user_id )\n" +
                "                WHEN 'DEAL'\n" +
                "                    THEN (  SELECT DISTINCT\n" +
                "                                Concat( batch.code, '-', detail.line_seq\n" +
                "                                        , ' ', Ifnull( (    SELECT DISTINCT\n" +
                "                                                                auth.real_name\n" +
                "                                                            FROM app_sys.asys_uniapp_rn_auth auth\n"
                +
                "                                                            WHERE auth.uniapp_user_id = e.uniapp_user_id\n"
                +
                "                                                                AND auth.is_disable = 0 ), ' ' ) )\n"
                +
                "                            FROM web_pym.wpym_payment_batch_detail detail\n" +
                "                                LEFT JOIN web_pym.wpym_payment_batch batch\n" +
                "                                    ON detail.payment_batch_id = batch.id\n" +
                "                            WHERE detail.id = e.review_object_id )\n" +
                "                WHEN 'TASK'\n" +
                "                    THEN (  SELECT code\n" +
                "                            FROM web_tm.wtm_task task\n" +
                "                            WHERE e.review_object_id = task.id )\n" +
                "                ELSE NULL\n" +
                "            END AS reviewobjectname\n" +
                "        , CASE\n" +
                "                WHEN 4\n" +
                "                    THEN 'HIGH_LEVEL'\n" +
                "                WHEN 3\n" +
                "                    THEN 'MEDIUM_LEVEL'\n" +
                "                WHEN 2\n" +
                "                    THEN 'LOW_LEVEL'\n" +
                "                ELSE 'HEALTHY'\n" +
                "            END AS risklevel\n" +
                "FROM mtp_cs.mtp_rsk_event e\n" +
                "WHERE e.is_deleted = 0\n" +
                "ORDER BY e.review_latest_datetime DESC\n" +
                "LIMIT 30\n" +
                ";";


        Set<SqlTable> tablesOrOtherSources = TableFinder.findTablesOrOtherSources(sqlStr);

        /*long startMillis = System.currentTimeMillis();
        for (int i = 1; i < 1000; i++) {
            final CCJSqlParser parser = new CCJSqlParser(sqlStr)
                    .withSquareBracketQuotation(false)
                    .withAllowComplexParsing(true)
                    .withBackslashEscapeCharacter(false);
            parser.Statements();
            long endMillis = System.currentTimeMillis();
            System.out.println("Duration [ms]: " + (endMillis - startMillis) / i);
        }*/
    }


    public static void main(String[] args) {
        String sql = """
                select * from a where 
                (
                    `examVenueId` = #{ew.paramNameValuePairs.MPGENVAL1} 
                    AND (`phone` = #{ew.paramNameValuePairs.MPGENVAL2} OR `email` = #{ew.paramNameValuePairs.MPGENVAL3}) 
                    AND `createTime` >= #{ew.paramNameValuePairs.MPGENVAL4} 
                    AND `createTime` <= #{ew.paramNameValuePairs.MPGENVAL5}
                )
                """;

        sql = preProcessSql(sql);
        System.out.println(sql);
    }

    @Test
    public void test2() throws JSQLParserException {
        String sql = """
                INSERT INTO user_info 
                ( userId, userEngName, userName, userFullName, areaCode, mobile, email, userType, createBy, createTime, updateBy, updateTime, enableFlag ) 
                VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) , ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) , ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) , ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) , ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) , ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) , ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) , ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) , ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) , ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) , ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) , ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) , ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) , ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) , ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) , ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) , ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) , ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) , ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) , ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) , ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) , ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) , ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) , ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) , ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) , ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) , ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) , ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) , ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) , ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) , ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) , ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) , ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) , ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) , ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) , ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) , ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) , ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) , ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) , ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) , ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) , ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) , ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) , ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) , ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) , ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) , ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) , ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) , ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) , ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) , ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) , ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) , ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) , ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) , ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) , ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) , ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) , ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) , ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) , ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) , ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) , ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) , ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) , ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) , ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) , ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) , ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) , ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) , ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) , ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) , ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) , ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) , ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) , ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) , ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) , ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) , ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) , ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) , ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) , ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) , ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) , ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) , ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) , ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) , ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) , ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) , ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) , ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) , ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) , ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) , ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) , ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) , ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) , ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) , ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) , ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) , ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) , ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) , ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) , ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) 
                ON DUPLICATE KEY UPDATE 
                userEngName = VALUES(userEngName), userName = VALUES(userName), userFullName = VALUES(userFullName), areaCode = VALUES(areaCode), mobile = VALUES(mobile), email = VALUES(email), userType = VALUES(userType), createBy = VALUES(createBy), createTime = VALUES(createTime), updateBy = VALUES(updateBy), updateTime = VALUES(updateTime), enableFlag = VALUES(enableFlag)
                """;

        Statement parse = SqlParser.getInstance().parse(sql);
        // System.out.println(parse);

        Set<SqlParam> set = ParamFinder.find(sql);
        System.out.println(set);
    }
}
