package sqlTsinjo.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ArchiveConfig {
    private boolean enabled = true;
    private String directoryName = ".tombstone_archive";
    private long retentionSeconds = 7L * 24L * 60L * 60L;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getDirectoryName() {
        return directoryName;
    }

    public void setDirectoryName(String directoryName) {
        this.directoryName = directoryName;
    }

    public long getRetentionSeconds() {
        return retentionSeconds;
    }

    public void setRetentionSeconds(long retentionSeconds) {
        this.retentionSeconds = retentionSeconds;
    }
}
