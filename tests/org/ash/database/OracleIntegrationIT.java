package org.ash.database;

import org.ash.conn.model.Model;
import org.ash.conn.model.OracleVersion;
import org.ash.util.Options;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.sql.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

/** Explicitly opt-in: never runs during the default build or public CI. */
class OracleIntegrationIT {
    @TempDir Path directory;
    private String required(String name) {
        String value = System.getenv(name);
        assertNotNull(value, "Set " + name + " in the local environment");
        assertFalse(value.isBlank(), "Set " + name + " in the local environment");
        return value;
    }

    @Test void connectsAndChecksReadPrivileges() throws Exception {
        try (Connection connection = DriverManager.getConnection(required("ASHV_ORACLE_URL"), required("ASHV_ORACLE_USER"), required("ASHV_ORACLE_PASSWORD"))) {
            assertNotNull(OracleVersion.collector(connection.getMetaData().getDatabaseMajorVersion(), connection.getMetaData().getDatabaseMinorVersion()));
            for (String query : new String[] {
                    "SELECT SYSDATE FROM DUAL", "SELECT dbid FROM v$database",
                    "SELECT instance_number FROM v$instance", "SELECT value FROM v$parameter WHERE name='cpu_count'",
                    "SELECT user_id, username FROM dba_users WHERE rownum <= 1",
                    "SELECT sql_id, sql_text FROM v$sql WHERE rownum <= 1",
                    "SELECT sql_id FROM v$sql_plan WHERE rownum <= 1"}) {
                try (PreparedStatement statement = connection.prepareStatement(query)) {
                    statement.setQueryTimeout(30);
                    try (ResultSet rows = statement.executeQuery()) {
                        assertNotNull(rows.getMetaData());
                    }
                }
            }
        }
    }

    @Test void collectsAshIntoTemporaryHistory() throws Exception {
        assumeTrue("true".equalsIgnoreCase(System.getenv("ASHV_DIAGNOSTICS_PACK_AUTHORIZED")),
                "ASH integration requires confirmed Diagnostics Pack entitlement; connection test still runs");
        Model model = new Model();
        model.connectionPoolInit("oracle.jdbc.OracleDriver", required("ASHV_ORACLE_URL"), required("ASHV_ORACLE_USER"), required("ASHV_ORACLE_PASSWORD"));
        assertNull(model.getErrorMessage(), "Oracle connection failed; inspect the local error output");
        Options.getInstance().setEnvDir(directory.toString());
        Database11g1 database = null;
        try {
            database = new Database11g1(model);
            database.loadAshDataToLocal();
            assertTrue(database.getDao().activeSessionHistoryById.count() > 0,
                    "No ASH samples persisted: check workload, privileges and collector errors");
        } finally {
            if (database != null) database.close();
            model.closeConnectionPool();
        }
    }
}
