package org.ash.database;

import org.ash.conn.model.*;
import org.ash.datamodel.ActiveSessionHistory;
import org.ash.util.Options;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.sql.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CollectorTest {
    @TempDir Path directory;

    @Test void persistsJdbcSampleAndUsesIncrementalWatermark() throws Exception {
        Options.getInstance().setEnvDir(directory.toString());
        Model model = mock(Model.class);
        ConnectionPool pool = mock(ConnectionPool.class);
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        ResultSet rows = mock(ResultSet.class);
        when(model.getConnectionPool()).thenReturn(pool);
        when(pool.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(rows);
        when(rows.next()).thenReturn(true, false);
        when(rows.getTimestamp("SAMPLE_TIME")).thenReturn(new Timestamp(1700000000123L));
        when(rows.getLong("SAMPLE_ID")).thenReturn(42L);
        when(rows.getLong("SESSION_ID")).thenReturn(123L);
        when(rows.getString("SQL_ID")).thenReturn("test_sql");
        Database11g1 database = new Database11g1(model);
        try {
            database.loadAshDataToLocal();
            assertEquals(1700000000123.0, database.getDao().ashById.get(42L).getsampleTime());
            assertEquals(1, database.getDao().activeSessionHistoryById.count());
            com.sleepycat.persist.EntityCursor<ActiveSessionHistory> cursor = database.getDao().activeSessionHistoryById.entities();
            try {
                ActiveSessionHistory sample = cursor.first();
                assertEquals("test_sql", sample.getSqlId());
                assertEquals(123, sample.getSessionId());
            } finally {
                cursor.close();
            }
            verify(rows).close();
            verify(statement).close();
            verify(pool).free(connection);
            database.setSampleId(42);
            when(rows.next()).thenReturn(false);
            database.loadAshDataToLocal();
            verify(connection).prepareStatement("SELECT * FROM V$ACTIVE_SESSION_HISTORY WHERE SAMPLE_ID > ?");
            verify(statement).setLong(1, 42L);
            assertEquals(1, database.getDao().activeSessionHistoryById.count());
        } finally {
            database.close();
        }
    }
}
