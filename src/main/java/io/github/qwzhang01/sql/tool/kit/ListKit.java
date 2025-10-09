package io.github.qwzhang01.sql.tool.kit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 列表工具
 *
 * @author avinzhang
 */
public class ListKit {
    public static <T> List<T> merge(List<T> list1, List<T> list2) {
        if (list1 == null && list2 == null) {
            return Collections.emptyList();
        }
        if (list1 == null) {
            return list2;
        }
        if (list2 == null) {
            return list1;
        }
        List<T> result = new ArrayList<>();
        result.addAll(list1);
        result.addAll(list2);
        return result;
    }
}
