package sqlTsinjo.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import sqlTsinjo.config.TombstoneConfig;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class TombstoneManager {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public record Tombstone(long deletedAt, String byInstance) {}

    public static File tombstoneFor(File dataFile, TombstoneConfig cfg) {
        return new File(dataFile.getPath() + cfg.getSuffix());
    }

    public static Tombstone readTombstone(File tombstoneFile) throws IOException {
        return MAPPER.readValue(tombstoneFile, Tombstone.class);
    }

    public static void writeTombstone(File tombstoneFile, Tombstone ts) throws IOException {
        File parent = tombstoneFile.getParentFile();
        if (parent != null) parent.mkdirs();
        MAPPER.writerWithDefaultPrettyPrinter().writeValue(tombstoneFile, ts);
    }

    public static boolean isDeleted(File dataFile, TombstoneConfig cfg) {
        File t = tombstoneFor(dataFile, cfg);
        if (!t.exists()) return false;
        try {
            Tombstone ts = readTombstone(t);
            long deletedAt = ts.deletedAt();
            if (!dataFile.exists()) return true;
            return deletedAt >= dataFile.lastModified();
        } catch (Exception e) {
            return true;
        }
    }

    public static void markDeleted(File dataFile, TombstoneConfig cfg, String byInstance) throws IOException {
        File t = tombstoneFor(dataFile, cfg);
        writeTombstone(t, new Tombstone(System.currentTimeMillis(), byInstance));
    }

    public static void clearDeletedMarker(File dataFile, TombstoneConfig cfg) {
        File t = tombstoneFor(dataFile, cfg);
        if (t.exists()) {
            try {
                Files.deleteIfExists(t.toPath());
            } catch (IOException ignore) {
            }
        }
    }

    public static boolean isDatabaseDeleted(File dbDir, TombstoneConfig cfg) {
        File marker = new File(dbDir, cfg.getDatabaseMarkerFile());
        return marker.exists();
    }

    public static void markDatabaseDeleted(File dbDir, TombstoneConfig cfg, String byInstance) throws IOException {
        File marker = new File(dbDir, cfg.getDatabaseMarkerFile());
        writeTombstone(marker, new Tombstone(System.currentTimeMillis(), byInstance));
    }

    public static void clearDatabaseDeletedMarker(File dbDir, TombstoneConfig cfg) {
        File marker = new File(dbDir, cfg.getDatabaseMarkerFile());
        if (marker.exists()) {
            try {
                Files.deleteIfExists(marker.toPath());
            } catch (IOException ignore) {
            }
        }
    }

    public static void gcTombstones(Path dataRoot, TombstoneConfig cfg, int replicationIntervalSeconds) {
        long now = System.currentTimeMillis();
        long ttlMillis = cfg.getTtlSeconds() * 1000L;
        long safetyMillis = (long) cfg.getGcSafetyMultiplier() * replicationIntervalSeconds * 1000L;

        try {
            if (!Files.exists(dataRoot)) return;
            Files.walk(dataRoot)
                    .filter(Files::isRegularFile)
                    .forEach(p -> {
                        String name = p.getFileName().toString();
                        boolean isTombstone = name.endsWith(cfg.getSuffix()) || name.equals(cfg.getDatabaseMarkerFile());
                        if (!isTombstone) return;

                        try {
                            Tombstone ts = readTombstone(p.toFile());
                            long age = now - ts.deletedAt();
                            if (age <= ttlMillis + safetyMillis) return;

                            if (cfg.getArchive() == null || !cfg.getArchive().isEnabled()) return;

                            Path archiveRoot = dataRoot.resolve(cfg.getArchive().getDirectoryName());
                            Path rel = dataRoot.relativize(p);
                            Path target = archiveRoot.resolve(rel);
                            Files.createDirectories(target.getParent());
                            Files.move(p, target, StandardCopyOption.REPLACE_EXISTING);
                        } catch (Exception ignore) {
                        }
                    });

            if (cfg.getArchive() != null && cfg.getArchive().isEnabled()) {
                purgeArchive(dataRoot, cfg);
            }
        } catch (IOException ignore) {
        }
    }

    private static void purgeArchive(Path dataRoot, TombstoneConfig cfg) {
        Path archiveRoot = dataRoot.resolve(cfg.getArchive().getDirectoryName());
        if (!Files.exists(archiveRoot)) return;

        long now = System.currentTimeMillis();
        long keepMillis = cfg.getArchive().getRetentionSeconds() * 1000L;
        try {
            Files.walk(archiveRoot)
                    .filter(Files::isRegularFile)
                    .forEach(p -> {
                        try {
                            Tombstone ts = readTombstone(p.toFile());
                            if (now - ts.deletedAt() > (cfg.getTtlSeconds() * 1000L) + keepMillis) {
                                Files.deleteIfExists(p);
                            }
                        } catch (Exception ignore) {
                        }
                    });
        } catch (IOException ignore) {
        }
    }
}
