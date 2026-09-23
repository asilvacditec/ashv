package org.ash.activity;

import java.sql.SQLException;
import java.time.Instant;
import java.util.List;

/** Read-only boundary for engine-specific history repositories. */
public interface ActivityRepository {
    Report read(Instant fromInclusive, Instant toExclusive) throws SQLException;

    record Point(Instant time, long activeRequests) { }
    record Ranking(String name, long observations) { }
    record Request(Instant time, int session, String loginTime, int request,
                   String database, String login, String status, String waitType,
                   int blocker, long cpuMs, String sql) { }
    record Report(List<Point> points, List<Ranking> waits, List<Ranking> queries,
                  List<Ranking> sessions, List<Request> requests, boolean detailsTruncated) {
        public Report {
            points = List.copyOf(points);
            waits = List.copyOf(waits);
            queries = List.copyOf(queries);
            sessions = List.copyOf(sessions);
            requests = List.copyOf(requests);
        }
    }
}
