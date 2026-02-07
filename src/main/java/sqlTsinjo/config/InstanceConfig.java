package sqlTsinjo.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InstanceConfig {
    private String id;
    private InstanceRole role;
    private String host;
    private int port;
    private String dataDirectory;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public InstanceRole getRole() {
        return role;
    }

    public void setRole(InstanceRole role) {
        this.role = role;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getDataDirectory() {
        return dataDirectory;
    }

    public void setDataDirectory(String dataDirectory) {
        this.dataDirectory = dataDirectory;
    }

    @Override
    public String toString() {
        return id + "(" + role + ")@" + host + ":" + port;
    }
}
