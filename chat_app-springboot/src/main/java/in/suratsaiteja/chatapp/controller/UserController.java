package in.suratsaiteja.chatapp.controller;

import in.suratsaiteja.chatapp.dao.UserDAO;
import in.suratsaiteja.chatapp.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserDAO userDAO;

    @Autowired
    public UserController(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody User user) {
        try {
            boolean success = userDAO.registerUser(
                user.getUsername(), 
                user.getEmail(), 
                user.getPassword()
            );
            if (success) {
                return ResponseEntity.ok("User registered successfully!");
            }
            return ResponseEntity.badRequest().body("Registration failed");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user) {
        try {
            System.out.println("==== Login Attempt ====");
            System.out.println("Username: " + user.getUsername());
            System.out.println("Password length: " + (user.getPassword() != null ? user.getPassword().length() : 0));
            
            if (user.getUsername() == null || user.getPassword() == null) {
                System.out.println("Login failed: Missing credentials");
                return ResponseEntity.badRequest().body("Username and password are required");
            }
            
            boolean isValid = userDAO.validateUser(user.getUsername(), user.getPassword());
            System.out.println("Login validation result: " + isValid);
            
            if (isValid) {
                System.out.println("Login successful for user: " + user.getUsername());
                return ResponseEntity.ok("Login successful!");
            }
            
            System.out.println("Login failed: Invalid credentials");
            return ResponseEntity.badRequest().body("Invalid username or password!");
        } catch (Exception e) {
            System.err.println("Login error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }
}
