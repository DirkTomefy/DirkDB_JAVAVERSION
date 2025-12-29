package sqlTsinjo.cli;

import java.io.File;
import java.util.Vector;

public class AppContext {
    String databaseName;
    String userName;
    Vector<File> lokedFile;
    
    
    public AppContext(String databaseName, String userName) {
        this.databaseName = databaseName;
        this.userName = userName;
        this.lokedFile =new Vector<>();
    }
    public String getDatabaseName() {
        return databaseName;
    }
    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
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
}
