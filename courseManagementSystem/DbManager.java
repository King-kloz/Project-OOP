package courseManagementSystem;

import java.sql.*;

public class DbManager {
    private static DbManager instance;
    private Connection conn;
    
    private DbManager() throws SQLException {
        conn = DriverManager.getConnection("jdbc:mysql://weka host db", "weka username", "weka password");
    }
    
    public static DbManager getInstance() throws SQLException {
        if (instance == null) instance = new DbManager();
        return instance;
    }
    
    public Connection getConnection() {
        return conn;
    }
}
