package sqlTsinjo.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ClusterConfig {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private int configVersion;
    private LoadBalancerConfig loadBalancer;
    private List<InstanceConfig> instances = new ArrayList<>();
    private RoutingConfig routing;
    private ReplicationConfig replication;

    public static ClusterConfig load(String path) throws IOException {
        Objects.requireNonNull(path, "path");
        return MAPPER.readValue(new File(path), ClusterConfig.class);
    }

    public InstanceConfig getInstanceById(String id) {
        if (id == null) return null;
        for (InstanceConfig instance : instances) {
            if (id.equals(instance.getId())) return instance;
        }
        return null;
    }

    public List<InstanceConfig> getMasters() {
        List<InstanceConfig> out = new ArrayList<>();
        for (InstanceConfig instance : instances) {
            if (instance.getRole() == InstanceRole.MASTER) out.add(instance);
        }
        return out;
    }

    public List<InstanceConfig> getReadBackends() {
        if (routing == null || routing.getReads() == null) {
            return List.copyOf(instances);
        }
        return routing.getReads().filter(instances);
    }

    public int getConfigVersion() {
        return configVersion;
    }

    public void setConfigVersion(int configVersion) {
        this.configVersion = configVersion;
    }

    public LoadBalancerConfig getLoadBalancer() {
        return loadBalancer;
    }

    public void setLoadBalancer(LoadBalancerConfig loadBalancer) {
        this.loadBalancer = loadBalancer;
    }

    public List<InstanceConfig> getInstances() {
        return instances;
    }

    public void setInstances(List<InstanceConfig> instances) {
        this.instances = instances == null ? new ArrayList<>() : instances;
    }

    public RoutingConfig getRouting() {
        return routing;
    }

    public void setRouting(RoutingConfig routing) {
        this.routing = routing;
    }

    public ReplicationConfig getReplication() {
        return replication;
    }

    public void setReplication(ReplicationConfig replication) {
        this.replication = replication;
    }
}
