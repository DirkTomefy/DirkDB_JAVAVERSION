package sqlTsinjo.config;

import java.io.IOException;

public class ClusterRuntime {
    public static final String ENV_CONFIG_PATH = "DIRK_CONFIG_PATH";
    public static final String ENV_INSTANCE_ID = "DIRK_INSTANCE_ID";

    public static final String PROP_CONFIG_PATH = "dirk.configPath";
    public static final String PROP_INSTANCE_ID = "dirk.instanceId";

    private final String configPath;
    private final ClusterConfig cluster;

    public ClusterRuntime(String configPath) throws IOException {
        this.configPath = configPath;
        this.cluster = ClusterConfig.load(configPath);
    }

    public static ClusterRuntime loadFromEnv() throws IOException {
        String configPath = System.getProperty(PROP_CONFIG_PATH);
        if (configPath == null || configPath.isBlank()) {
            configPath = System.getenv(ENV_CONFIG_PATH);
        }
        if (configPath == null || configPath.isBlank()) {
            configPath = "config.json";
        }
        return new ClusterRuntime(configPath);
    }

    public InstanceConfig requireSelfInstanceFromEnv() {
        String instanceId = System.getProperty(PROP_INSTANCE_ID);
        if (instanceId == null || instanceId.isBlank()) {
            instanceId = System.getenv(ENV_INSTANCE_ID);
        }
        if (instanceId == null || instanceId.isBlank()) {
            throw new IllegalStateException(
                    "Missing instance id. Set -D" + PROP_INSTANCE_ID + " or env " + ENV_INSTANCE_ID);
        }
        InstanceConfig self = cluster.getInstanceById(instanceId);
        if (self == null) {
            throw new IllegalStateException("Instance not found in config.json: " + instanceId);
        }
        return self;
    }

    public String getConfigPath() {
        return configPath;
    }

    public ClusterConfig getCluster() {
        return cluster;
    }
}
