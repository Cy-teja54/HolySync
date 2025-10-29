package in.suratsaiteja.chatapp.dao;

import in.suratsaiteja.chatapp.config.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.stereotype.Repository;

@Repository
public class UserDAO {
    private static final String INSERT_USER_QUERY = 
        "INSERT INTO users (username, email, password) VALUES (?, ?, ?)";
    
    private static final String VALIDATE_USER_QUERY = 
        "SELECT username, password FROM users WHERE username = ?";

    public boolean registerUser(String username, String email, String password) {
        try (Connection conn = DatabaseConnection.gConnection();  // Fixed method name
             PreparedStatement stmt = conn.prepareStatement(INSERT_USER_QUERY)) {

            stmt.setString(1, username);
            stmt.setString(2, email);
            stmt.setString(3, password);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println("User registered successfully!");
                return true;
            }
            return false;

        } catch (SQLException e) {
            System.err.println("Error registering user: " + e.getMessage());
            throw new RuntimeException("Failed to register user", e);
        }
    }

    public boolean validateUser(String username, String password) {
        try (Connection conn = DatabaseConnection.gConnection();
             PreparedStatement stmt = conn.prepareStatement(VALIDATE_USER_QUERY)) {
            
            System.out.println("\n=== Validating User ===");
            System.out.println("Attempting login for username: '" + username + "'");
            
            // First, let's see what users exist in the database
            try (PreparedStatement debugStmt = conn.prepareStatement("SELECT username, password FROM users");
                 ResultSet debugRs = debugStmt.executeQuery()) {
                System.out.println("\nAll users in database:");
                while (debugRs.next()) {
                    System.out.println("DB User: '" + debugRs.getString("username") + 
                                     "', Password: '" + debugRs.getString("password") + "'");
                }
            }
            
            stmt.setString(1, username);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String storedUsername = rs.getString("username");
                    String storedPassword = rs.getString("password");
                    
                    System.out.println("\nLogin attempt details:");
                    System.out.println("Input username : '" + username + "'");
                    System.out.println("Input password : '" + password + "'");
                    System.out.println("Stored username: '" + storedUsername + "'");
                    System.out.println("Stored password: '" + storedPassword + "'");
                    
                    boolean matches = password.equals(storedPassword);
                    System.out.println("Password match: " + matches);
                    
                    return matches;
                }
            }
            System.out.println("No user found with username: '" + username + "'");
            return false;
            
        } catch (SQLException e) {
            System.err.println("Error validating user: " + e.getMessage());
            throw new RuntimeException("Failed to validate user", e);
        }
    }
}
