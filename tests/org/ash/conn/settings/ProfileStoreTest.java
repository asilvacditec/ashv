package org.ash.conn.settings;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import static org.junit.jupiter.api.Assertions.*;

class ProfileStoreTest {
    @TempDir Path directory;
    private DbConnection profile(String name, String url) {
        return new DbConnection(name, "oracle.jdbc.OracleDriver", url, "ash_reader", "secret-that-must-not-be-written", "EE");
    }

    @Test void roundTripOmitsPasswordAndRemovesLegacyCredential() throws Exception {
        Files.writeString(directory.resolve("Teste_ação.pwd"), "legacy-password");
        ProfileStore.write(directory, profile("Teste ação", "jdbc:oracle:thin:@//localhost:1521/service"));
        Path file = directory.resolve("Teste_ação.ini");
        assertFalse(Files.readString(file).contains("secret-that-must-not-be-written"));
        assertFalse(Files.exists(directory.resolve("Teste_ação.pwd")));
        DbConnection loaded = ProfileStore.read(file);
        assertEquals("Teste ação", loaded.getName());
        assertEquals("ash_reader", loaded.getUsername());
        assertEquals("", loaded.getPassword());
    }

    @Test void legacyPasswordIsNeverRead() throws Exception {
        Files.writeString(directory.resolve("old.ini"), "old\noracle.jdbc.OracleDriver\njdbc:oracle:thin:@host:1521:SID\nreader\nEE\n");
        Files.write(directory.resolve("old.pwd"), new byte[] {1, 2, 3});
        assertEquals("", ProfileStore.read(directory.resolve("old.ini")).getPassword());
    }

    @Test void rejectsPathsMultilineAndEmbeddedCredentials() {
        assertThrows(java.io.IOException.class, () -> ProfileStore.write(directory, profile("../escape", "jdbc:oracle:thin:@host")));
        assertThrows(java.io.IOException.class, () -> ProfileStore.write(directory, profile("ok", "jdbc:oracle:thin:user/password@host")));
        assertThrows(java.io.IOException.class, () -> ProfileStore.write(directory, profile("ok", "jdbc:oracle:thin:@host\npassword")));
    }

    @Test void rejectsIncompleteProfile() throws Exception {
        Path file = directory.resolve("broken.ini");
        Files.writeString(file, "one line");
        assertThrows(java.io.IOException.class, () -> ProfileStore.read(file));
    }
}
