package sqlTsinjo.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RoutingConfig {
    private ReadsRoutingConfig reads;
    private WritesRoutingConfig writes;

    public ReadsRoutingConfig getReads() {
        return reads;
    }

    public void setReads(ReadsRoutingConfig reads) {
        this.reads = reads;
    }

    public WritesRoutingConfig getWrites() {
        return writes;
    }

    public void setWrites(WritesRoutingConfig writes) {
        this.writes = writes;
    }
}
