package org.ash.datatemp;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SqlsTempTest {
    @Test void aggregatesCpuAndIoPerSqlAndResets() {
        SqlsTemp sqls = new SqlsTemp();
        sqls.setSqlId("cpu");
        sqls.setSqlId("io");
        sqls.setTimeOfGroupEvent("cpu", 0, 1, 0, 2);
        sqls.setTimeOfGroupEvent("io", 200, 0, 1740759767.0, 3);
        sqls.set_sum();
        assertEquals(2, sqls.get_cpu_sum());
        assertEquals(3, sqls.get_userIO8_sum());
        assertEquals(5, sqls.get_sum());
        assertEquals(2.0, sqls.getMainSqls().get("cpu").get("COUNT"));
        sqls.clear();
        assertTrue(sqls.getMainSqls().isEmpty());
        assertEquals(0, sqls.get_cpu_sum());
    }
    @Test void keepsUniqueNonzeroPlans() {
        SqlsTemp sqls = new SqlsTemp();
        sqls.setSqlId("sql");
        sqls.saveSqlPlanHashValue("sql", 0);
        sqls.saveSqlPlanHashValue("sql", 123456);
        sqls.saveSqlPlanHashValue("sql", 123456);
        assertEquals(java.util.List.of(123456.0), sqls.getSqlPlanHashValue("sql"));
    }
}
