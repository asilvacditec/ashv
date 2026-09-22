/* ASH Viewer modernization contribution: Aparecido Silva. GPL-3.0-or-later. */
package org.ash.conn.model;

import java.sql.SQLException;

/** Selects the existing collector using JDBC metadata rather than banner offsets. */
public final class OracleVersion {
    private OracleVersion() { }

    public static String collector(int major, int minor) throws SQLException {
        if (major == 8) return "8i";
        if (major == 9) return "9i";
        if (major == 10) return minor <= 1 ? "10g1" : "10g2";
        // The 11g collector uses columns retained in later releases.
        // Live validation is required per database release and deployment.
        if (major >= 11) return "11g";
        throw new SQLException("Unsupported Oracle database major version: " + major);
    }
}
