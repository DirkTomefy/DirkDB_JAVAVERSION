package sqlTsinjo.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TombstoneConfig {
    private String suffix = ".tombstone";
    private String databaseMarkerFile = ".db.tombstone";
    private long ttlSeconds = 24L * 60L * 60L;
    private int gcSafetyMultiplier = 10;
    private ArchiveConfig archive = new ArchiveConfig();

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public String getDatabaseMarkerFile() {
        return databaseMarkerFile;
    }

    public void setDatabaseMarkerFile(String databaseMarkerFile) {
        this.databaseMarkerFile = databaseMarkerFile;
    }

    public long getTtlSeconds() {
        return ttlSeconds;
    }

    public void setTtlSeconds(long ttlSeconds) {
        this.ttlSeconds = ttlSeconds;
    }

    public int getGcSafetyMultiplier() {
        return gcSafetyMultiplier;
    }

    public void setGcSafetyMultiplier(int gcSafetyMultiplier) {
        this.gcSafetyMultiplier = gcSafetyMultiplier;
    }

    public ArchiveConfig getArchive() {
        return archive;
    }

    public void setArchive(ArchiveConfig archive) {
        this.archive = archive;
    }
}
