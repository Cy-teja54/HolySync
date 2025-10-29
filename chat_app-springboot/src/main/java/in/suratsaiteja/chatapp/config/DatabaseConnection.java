package in.suratsaiteja.chatapp.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL  = "jdbc:postgresql://localhost:5432/chat_app";
    private static final String USER = "chat_user";
    private static final String PASSWORD = "chat123";

    public static Connection gConnection() throws SQLException{
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
