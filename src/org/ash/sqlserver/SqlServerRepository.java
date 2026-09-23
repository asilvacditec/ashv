package org.ash.sqlserver;

import org.ash.activity.ActivityRepository;
import java.sql.*;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

/** Reads installed ashv v1 tables only; never provisions or collects via the UI. */
public final class SqlServerRepository implements ActivityRepository {
    @FunctionalInterface
    public interface Connections { Connection open() throws SQLException; }
    private final Connections connections;
    private static final String RANGE = " FROM ashv.Sample s JOIN ashv.RequestSample r ON r.sample_id=s.sample_id"
            + " WHERE s.sampled_at_utc >= ? AND s.sampled_at_utc < ? ";

    public SqlServerRepository(Connections connections) { this.connections = connections; }

    @Override public Report read(Instant from, Instant to) throws SQLException {
        if (from == null || to == null || !from.isBefore(to)
                || Duration.between(from, to).compareTo(Duration.ofDays(31)) > 0)
            throw new IllegalArgumentException("Escolha um intervalo UTC de ate 31 dias.");
        try (Connection c = connections.open()) {
            DatabaseMetaData md = c.getMetaData();
            if (!"Microsoft SQL Server".equals(md.getDatabaseProductName()) || md.getDatabaseMajorVersion() < 11)
                throw new SQLException("Esta visualizacao exige SQL Server 2012 ou posterior.");
            try (Statement st = c.createStatement()) {
                st.setQueryTimeout(30);
                try (ResultSet rs = st.executeQuery("SELECT schema_version FROM ashv.Configuration WHERE id=1")) {
                    if (!rs.next() || rs.getInt(1) != 1) throw new SQLException("Repositorio ashv v1 ausente ou incompativel.");
                }
            }
            List<Point> points = new ArrayList<>();
            try (PreparedStatement st = range(c, "SELECT TOP (50001) sampled_at_utc, active_requests FROM ashv.Sample"
                    + " WHERE sampled_at_utc >= ? AND sampled_at_utc < ? ORDER BY sampled_at_utc, sample_id", from, to);
                 ResultSet rs = st.executeQuery()) {
                while (rs.next()) points.add(new Point(rs.getTimestamp(1, utc()).toInstant(), rs.getLong(2)));
            }
            if (points.size() > 50000) throw new SQLException("Mais de 50000 coletas; reduza o intervalo.");
            List<Ranking> waits = ranking(c, "COALESCE(r.wait_type, N'Sem espera registrada / ' + r.status)", from, to);
            List<Ranking> queries = ranking(c, "COALESCE(r.database_name,N'?') + N' / ' + COALESCE(CONVERT(varchar(18),r.query_hash,1),'sem hash')", from, to);
            List<Ranking> sessions = ranking(c, "CONVERT(varchar(6),r.session_id) + ' / ' + CONVERT(varchar(23),r.login_time,121)", from, to);
            List<Request> requests = new ArrayList<>();
            try (PreparedStatement st = range(c, "SELECT TOP (1001) s.sampled_at_utc,r.session_id,r.login_time,r.request_id,"
                    + "r.database_name,r.login_name,r.status,r.wait_type,r.blocking_session_id,r.cpu_time_ms,r.sql_text"
                    + RANGE + "ORDER BY s.sampled_at_utc DESC,s.sample_id DESC,r.session_id,r.request_id", from, to);
                 ResultSet rs = st.executeQuery()) {
                while (rs.next()) requests.add(new Request(rs.getTimestamp(1, utc()).toInstant(), rs.getInt(2),
                        rs.getString(3), rs.getInt(4), rs.getString(5), rs.getString(6), rs.getString(7),
                        rs.getString(8), rs.getInt(9), rs.getLong(10), rs.getString(11)));
            }
            boolean truncated = requests.size() > 1000;
            if (truncated) requests.remove(1000);
            return new Report(points, waits, queries, sessions, requests, truncated);
        }
    }

    private static Calendar utc() { return Calendar.getInstance(TimeZone.getTimeZone("UTC")); }

    private static PreparedStatement range(Connection c, String sql, Instant from, Instant to) throws SQLException {
        PreparedStatement st = c.prepareStatement(sql);
        try {
            st.setQueryTimeout(30);
            st.setTimestamp(1, Timestamp.from(from), utc());
            st.setTimestamp(2, Timestamp.from(to), utc());
            return st;
        } catch (SQLException ex) { st.close(); throw ex; }
    }

    private static List<Ranking> ranking(Connection c, String expression, Instant from, Instant to) throws SQLException {
        List<Ranking> result = new ArrayList<>();
        // Expressions are internal constants, never user input.
        try (PreparedStatement st = range(c, "SELECT TOP (20) " + expression + ",COUNT_BIG(*)" + RANGE
                + "GROUP BY " + expression + " ORDER BY COUNT_BIG(*) DESC,1", from, to);
             ResultSet rs = st.executeQuery()) {
            while (rs.next()) result.add(new Ranking(rs.getString(1), rs.getLong(2)));
        }
        return result;
    }
}
