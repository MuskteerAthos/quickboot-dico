package com.data2.salmon;

import com.data2.salmon.common.util.ArrUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;

@Component
public class BuildFactory {

    @Autowired
    public ConnectFactory connectFactory;

    @Autowired
    public Router router;

    public void build(String dbase, ExecuteSql currSql, Map<?, ?> params) throws SalmonException {
        // loading ruler.
        loadFromConfig(currSql);
        // calcu ruler for where data goes.
        TableConfig ruler = currSql.getRuler();
        String dbId = Parser.parse(ruler, params);
        // create connection && prepared sql param.
        connectFactory.setSql(currSql).makeConnect(new DataSourceLooker(dbase,dbId)).preparedStmt(params);

    }

    /**
     * decorate sql.
     *
     * @param currSql
     */
    private void loadFromConfig(ExecuteSql currSql) {
        currSql.setTableName(calcuTabnameFromSqlStr(currSql.getSql()));
        TableConfig tabRuler = router.config(currSql.getTableName());
        currSql.setRuler(tabRuler);
        currSql.setPartionKey(new Pair(tabRuler.getColumn(), null));
    }

    /**
     * search for tab name.
     *
     * @param sql
     * @return
     */
    private static String calcuTabnameFromSqlStr(String sql) {
        String[] res = PatternMatcherUnit.split(sql, "\\s+");
        boolean tag = false;
        for (String sub : res) {
            if (ArrUtils.inArray(sub.trim(), OperationKeys.INTO, OperationKeys.DELETE, OperationKeys.UPDATE, OperationKeys.FROM)) {
                tag = true;
                continue;
            }
            if (tag) {
                if (!StringUtils.isEmpty(sub.trim())) {
                    return sub;
                }
            }
        }

        return null;
    }

}
