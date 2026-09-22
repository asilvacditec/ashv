package org.ash.conn.settings;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class OracleJdbcUrlTest {
    @Test void readsHistoricalAndServiceProfiles() {
        String[] expected = {"host", "1521", "pdb"};
        assertArrayEquals(expected, OracleJdbcUrl.parts("jdbc:oracle:thin:@host:1521:pdb"));
        assertArrayEquals(expected, OracleJdbcUrl.parts(OracleJdbcUrl.service("host", "1521", "pdb")));
        assertArrayEquals(new String[] {"[::1]", "1521", "pdb"}, OracleJdbcUrl.parts("jdbc:oracle:thin:@//[::1]:1521/pdb"));
    }
}
