package org.ash.sqlserver;

import org.junit.jupiter.api.Test;
import java.sql.*;
import java.time.Instant;
import java.util.Calendar;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SqlServerRepositoryTest {
    private final Instant from = Instant.parse("2026-09-01T00:00:00Z");
    private final Instant to = Instant.parse("2026-09-02T00:00:00Z");

    private Connection connection() throws Exception {
        Connection c = mock(Connection.class);
        DatabaseMetaData md = mock(DatabaseMetaData.class);
        when(c.getMetaData()).thenReturn(md);
        when(md.getDatabaseProductName()).thenReturn("Microsoft SQL Server");
        when(md.getDatabaseMajorVersion()).thenReturn(11);
        Statement st = mock(Statement.class);
        ResultSet version = mock(ResultSet.class);
        when(c.createStatement()).thenReturn(st);
        when(st.executeQuery(anyString())).thenReturn(version);
        when(version.next()).thenReturn(true, false);
        when(version.getInt(1)).thenReturn(1);
        return c;
    }

    @Test void emptySamplesAreDistinctFromZeroActivityAndResourcesClose() throws Exception {
        Connection c = connection();
        PreparedStatement st = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);
        when(c.prepareStatement(anyString())).thenReturn(st);
        when(st.executeQuery()).thenReturn(rs);
        var report = new SqlServerRepository(() -> c).read(from, to);
        assertTrue(report.points().isEmpty());
        assertTrue(report.requests().isEmpty());
        assertFalse(report.detailsTruncated());
        verify(st, times(5)).setTimestamp(eq(1), eq(Timestamp.from(from)), argThat(cal -> cal.getTimeZone().getID().equals("UTC")));
        verify(st, times(5)).setTimestamp(eq(2), eq(Timestamp.from(to)), any(Calendar.class));
        verify(st, times(5)).setQueryTimeout(30);
        verify(st, times(5)).close();
        verify(rs, times(5)).close();
        verify(c).close();
    }

    @Test void failedQueryClosesConnection() throws Exception {
        Connection c = connection();
        PreparedStatement st = mock(PreparedStatement.class);
        when(c.prepareStatement(anyString())).thenReturn(st);
        when(st.executeQuery()).thenThrow(new SQLException("timeout"));
        assertThrows(SQLException.class, () -> new SqlServerRepository(() -> c).read(from, to));
        verify(st).close(); verify(c).close();
    }

    @Test void rejectsOracleInsteadOfSelectingCollectorByMajorVersion() throws Exception {
        Connection c = connection();
        when(c.getMetaData().getDatabaseProductName()).thenReturn("Oracle");
        assertThrows(SQLException.class, () -> new SqlServerRepository(() -> c).read(from, to));
        verify(c, never()).createStatement(); verify(c).close();
    }

    @Test void boundsRangeBeforeConnecting() {
        SqlServerRepository repository = new SqlServerRepository(() -> { fail("Must not connect"); return null; });
        assertThrows(IllegalArgumentException.class, () -> repository.read(to, from));
        assertThrows(IllegalArgumentException.class, () -> repository.read(from, from.plusSeconds(32*86400L)));
    }

    @Test void preservesUtcEmptySampleAndMarksDetailTruncation() throws Exception {
        Connection c = connection();
        when(c.prepareStatement(anyString())).thenAnswer(call -> {
            String sql = call.getArgument(0);
            PreparedStatement st = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);
            when(st.executeQuery()).thenReturn(rs);
            if (sql.contains("TOP (50001)")) {
                when(rs.next()).thenReturn(true, false);
                when(rs.getTimestamp(eq(1), any(Calendar.class))).thenReturn(Timestamp.from(from));
                when(rs.getLong(2)).thenReturn(0L);
            } else if (sql.contains("TOP (1001)")) {
                int[] count = {0};
                when(rs.next()).thenAnswer(ignored -> ++count[0] <= 1001);
                when(rs.getTimestamp(eq(1), any(Calendar.class))).thenReturn(Timestamp.from(from));
                when(rs.getInt(2)).thenReturn(52);
            }
            return st;
        });
        var report = new SqlServerRepository(() -> c).read(from, to);
        assertEquals(1, report.points().size());
        assertEquals(from, report.points().get(0).time());
        assertEquals(0, report.points().get(0).activeRequests());
        assertEquals(1000, report.requests().size());
        assertTrue(report.detailsTruncated());
        assertThrows(UnsupportedOperationException.class, () -> report.points().clear());
    }

    @Test void rejectsUnknownSchema() throws Exception {
        Connection c = connection();
        ResultSet version = c.createStatement().executeQuery("");
        when(version.getInt(1)).thenReturn(2);
        assertThrows(SQLException.class, () -> new SqlServerRepository(() -> c).read(from, to));
        verify(c, never()).prepareStatement(anyString());
    }

    @Test void connectionPropertiesKeepCredentialsSeparateAndTlsEnabled() {
        var ds = SqlServerConnections.dataSource("localhost", 1433, "AshViewer;encrypt=false", "reader", "secret", false);
        assertEquals("AshViewer;encrypt=false", ds.getDatabaseName());
        assertEquals("true", ds.getEncrypt());
        assertFalse(ds.getTrustServerCertificate());
        assertEquals(15, ds.getLoginTimeout());
        assertThrows(IllegalArgumentException.class, () -> SqlServerConnections.dataSource("host", 0, "db", "user", "", false));
    }
}
