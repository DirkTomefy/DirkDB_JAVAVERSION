package sqlTsinjo.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ReadsRoutingConfig {
    private String strategy = "ROUND_ROBIN";
    private List<InstanceRole> includeRoles = new ArrayList<>();

    public String getStrategy() {
        return strategy;
    }

    public void setStrategy(String strategy) {
        this.strategy = strategy;
    }

    public List<InstanceRole> getIncludeRoles() {
        return includeRoles;
    }

    public void setIncludeRoles(List<InstanceRole> includeRoles) {
        this.includeRoles = includeRoles == null ? new ArrayList<>() : includeRoles;
    }

    public List<InstanceConfig> filter(List<InstanceConfig> instances) {
        if (instances == null) return List.of();
        if (includeRoles == null || includeRoles.isEmpty()) return List.copyOf(instances);
        Set<InstanceRole> set = new HashSet<>(includeRoles);
        List<InstanceConfig> out = new ArrayList<>();
        for (InstanceConfig i : instances) {
            if (set.contains(i.getRole())) out.add(i);
        }
        return out;
    }
}
