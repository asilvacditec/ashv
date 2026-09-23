package org.ash.sqlserver;

import com.microsoft.sqlserver.jdbc.SQLServerDataSource;

/** Structured JDBC properties avoid connection-string injection. */
public final class SqlServerConnections {
    private SqlServerConnections() { }
    public static SQLServerDataSource dataSource(String host, int port, String database,
                                                 String user, String password, boolean trustCertificate) {
        if (host == null || host.isBlank() || database == null || database.isBlank()
                || user == null || user.isBlank() || port < 1 || port > 65535)
            throw new IllegalArgumentException("Informe host, porta valida, banco de monitoramento e usuario.");
        SQLServerDataSource ds = new SQLServerDataSource();
        ds.setServerName(host.trim());
        ds.setPortNumber(port);
        ds.setDatabaseName(database.trim());
        ds.setUser(user);
        ds.setPassword(password);
        ds.setEncrypt("true");
        ds.setTrustServerCertificate(trustCertificate);
        ds.setApplicationName("ASH Viewer SQL Server preview");
        ds.setLoginTimeout(15);
        ds.setSocketTimeout(45000);
        return ds;
    }
}
