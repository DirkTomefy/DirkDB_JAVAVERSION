package cli;

public class AppContext {
    String databaseName;
    String userName;
    public AppContext(String databaseName, String userName) {
        this.databaseName = databaseName;
        this.userName = userName;
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
}
