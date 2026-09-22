package org.ash.conn.model;

import org.junit.jupiter.api.Test;
import java.sql.*;
import java.util.Properties;
import java.util.logging.Logger;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConnectionPoolTest {
    public static class TestDriver implements Driver {
        static Connection supplied;
        public Connection connect(String url, Properties info) { return acceptsURL(url) ? supplied : null; }
        public boolean acceptsURL(String url) { return url.startsWith("jdbc:ash-test:"); }
        public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) { return new DriverPropertyInfo[0]; }
        public int getMajorVersion() { return 1; }
        public int getMinorVersion() { return 0; }
        public boolean jdbcCompliant() { return false; }
        public Logger getParentLogger() { return Logger.getGlobal(); }
    }
    @Test void reusesConnectionsAndEnforcesCapacity() throws Exception {
        TestDriver driver = new TestDriver();
        Connection connection = mock(Connection.class);
        when(connection.prepareCall(anyString())).thenReturn(mock(CallableStatement.class));
        TestDriver.supplied = connection;
        DriverManager.registerDriver(driver);
        ConnectionPool pool = null;
        try {
            pool = new ConnectionPool(TestDriver.class.getName(), "jdbc:ash-test:db", "reader", "secret", 1, 1, false);
            assertSame(connection, pool.getConnection());
            ConnectionPool fullPool = pool;
            assertThrows(SQLException.class, fullPool::getConnection);
            pool.free(connection);
            assertSame(connection, pool.getConnection());
            assertEquals(1, pool.totalConnections());
        } finally {
            if (pool != null) pool.closeAllConnections();
            DriverManager.deregisterDriver(driver);
        }
        verify(connection).close();
    }
}
