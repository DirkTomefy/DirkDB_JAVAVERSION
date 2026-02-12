package sqlTsinjo.cli;

import java.io.File;
import java.nio.file.Path;
import java.util.Vector;

import sqlTsinjo.query.err.eval.DatabaseNotExistErr;

public class AppContext {
    String databaseName;
    String userName;
    Vector<File> lokedFile;
    boolean debugMode;

    private final String dataDirectory;
    private final String instanceId;
    private final int replicationIntervalSeconds;

    public AppContext(String databaseName, String userName, boolean debugMode) {
        this.databaseName = databaseName;
        this.userName = userName;
        this.lokedFile = new Vector<>();
        this.debugMode = debugMode;

        this.dataDirectory = "databases";
        this.instanceId = "default";
        this.replicationIntervalSeconds = 2;
    }

    public AppContext(String databaseName, String userName, boolean debugMode, String dataDirectory, String instanceId,
            int replicationIntervalSeconds) {
        this.databaseName = databaseName;
        this.userName = userName;
        this.lokedFile = new Vector<>();
        this.debugMode = debugMode;
        this.dataDirectory = dataDirectory;
        this.instanceId = instanceId;
        this.replicationIntervalSeconds = replicationIntervalSeconds;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) throws DatabaseNotExistErr {
        if(databaseName==null) return;
        if (databaseExists(databaseName)) {
            this.databaseName = databaseName;
        }else{
            throw new DatabaseNotExistErr(databaseName);
        }
    }

    public boolean databaseExists(String databaseName) {
        File dbDir = Path.of(dataDirectory, databaseName).toFile();
        if (!dbDir.exists()) return false;
        return true;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Vector<File> getLokedFile() {
        return lokedFile;
    }

    public void setLokedFile(Vector<File> lokedFile) {
        this.lokedFile = lokedFile;
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    public String getDataDirectory() {
        return dataDirectory;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public int getReplicationIntervalSeconds() {
        return replicationIntervalSeconds;
    }
}
