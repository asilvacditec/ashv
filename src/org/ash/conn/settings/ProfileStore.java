/* ASH Viewer modernization contribution: Aparecido Silva. GPL-3.0-or-later. */
package org.ash.conn.settings;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;

/** Persists only connection metadata. Passwords never cross this boundary. */
public final class ProfileStore {
    private ProfileStore() { }

    public static DbConnection read(Path file) throws IOException {
        List<String> fields = Files.readAllLines(file, StandardCharsets.UTF_8);
        if (fields.size() < 5) throw new IOException("Incomplete connection profile");
        DbConnection connection = new DbConnection(fields.get(0), fields.get(1), fields.get(2),
                fields.get(3), "", fields.get(4));
        validate(connection);
        return connection;
    }

    public static void write(Path directory, DbConnection connection) throws IOException {
        validate(connection);
        Files.createDirectories(directory);
        String basename = connection.getName().replace(' ', '_');
        Path target = directory.resolve(basename + ".ini");
        Path temporary = Files.createTempFile(directory, "profile-", ".tmp");
        try {
            Files.write(temporary, List.of(connection.getName(), connection.getClassName(),
                    connection.getUrl(), connection.getUsername(), connection.getEdition(), ""), StandardCharsets.UTF_8);
            try {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
            }
            Files.deleteIfExists(directory.resolve(basename + ".pwd"));
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private static void validate(DbConnection connection) throws IOException {
        String name = connection.getName();
        if (name == null || !name.matches("[\\p{L}\\p{N}_ .-]+") || name.equals(".") || name.equals("..")) {
            throw new IOException("Invalid profile name");
        }
        for (String field : new String[] {name, connection.getClassName(), connection.getUrl(),
                connection.getUsername(), connection.getEdition()}) {
            if (field == null || field.contains("\n") || field.contains("\r")) {
                throw new IOException("Profile fields must contain one line");
            }
        }
        if (!connection.getUrl().startsWith("jdbc:oracle:thin:@")) {
            throw new IOException("Use a JDBC URL without credentials: jdbc:oracle:thin:@//host:1521/service");
        }
    }
}
