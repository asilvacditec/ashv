package org.ash.conn.model;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class OracleVersionTest {
    @ParameterizedTest
    @CsvSource({"8,1,8i", "9,2,9i", "10,1,10g1", "10,2,10g2", "11,1,11g", "11,2,11g", "12,1,11g", "12,2,11g", "19,0,11g", "21,0,11g", "23,0,11g"})
    void selectsCollector(int major, int minor, String expected) throws Exception {
        assertEquals(expected, OracleVersion.collector(major, minor));
    }
    @Test void rejectsInvalidMetadata() {
        assertThrows(java.sql.SQLException.class, () -> OracleVersion.collector(0, 0));
    }
}
