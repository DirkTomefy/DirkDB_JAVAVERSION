package sqlTsinjo.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ReplicationConfig {
    private String type = "FILE";
    private int intervalSeconds = 2;
    private TombstoneConfig tombstone = new TombstoneConfig();

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getIntervalSeconds() {
        return intervalSeconds;
    }

    public void setIntervalSeconds(int intervalSeconds) {
        this.intervalSeconds = intervalSeconds;
    }

    public TombstoneConfig getTombstone() {
        return tombstone;
    }

    public void setTombstone(TombstoneConfig tombstone) {
        this.tombstone = tombstone;
    }
}
