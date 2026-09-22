/* ASH Viewer modernization contribution: Aparecido Silva. GPL-3.0-or-later. */
package org.ash.conn.settings;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Reads service URLs and the historical host:port:SID profile syntax. */
public final class OracleJdbcUrl {
    private static final Pattern URL = Pattern.compile("jdbc:oracle:thin:@(?://)?(\\[[^]]+]|[^:/]+):(\\d+)[/:](.+)");
    private OracleJdbcUrl() { }
    public static String[] parts(String url) {
        if (url == null || url.isEmpty()) return new String[] {"", "1521", ""};
        Matcher match = URL.matcher(url);
        if (!match.matches()) throw new IllegalArgumentException("Expected host, port and service in the Oracle JDBC URL");
        return new String[] {match.group(1), match.group(2), match.group(3)};
    }
    public static String service(String host, String port, String service) {
        return "jdbc:oracle:thin:@//" + host + ":" + port + "/" + service;
    }
}
