package application.database;

import application.models.Account;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;

public class AccountDAO {
    public Account login(String username, String password) {
        String sql = "SELECT acc_id, username, password, role, created_at FROM account WHERE username = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String hashedPassword = rs.getString("password");

                if (hashedPassword == null || hashedPassword.isEmpty()) {
                    System.err.println("Stored password is null or empty");
                    return null;
                }

                try {
                    // Convert $2y$ to $2a$ for compatibility with jBCrypt 0.4
                    // $2y$ and $2a$ are functionally equivalent
                    String compatibleHash = hashedPassword;
                    if (hashedPassword.startsWith("$2y$")) {
                        compatibleHash = "$2a$" + hashedPassword.substring(4);
                    }

                    // Verify password using BCrypt
                    if (BCrypt.checkpw(password, compatibleHash)) {
                        return new Account(
                                rs.getString("acc_id"),
                                rs.getString("username"),
                                hashedPassword,
                                rs.getString("role"),
                                rs.getString("created_at")
                        );
                    }
                } catch (IllegalArgumentException e) {
                    System.err.println("Invalid BCrypt hash format in database for user: " + username);
                    System.err.println("Hash value: " + hashedPassword);
                    System.err.println("Hash should start with $2a$, $2b$, or $2y$");
                    e.printStackTrace();
                    return null;
                }
            }

        } catch (SQLException e) {
            System.err.println("Login query failed");
            e.printStackTrace();
        }

        return null;
    }

    public ObservableList<Account> getAllAccounts() {
        ObservableList<Account> list = FXCollections.observableArrayList();
        String sql = "SELECT acc_id, username, password, role, created_at FROM account ORDER BY username ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(new Account(
                        rs.getString("acc_id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("role"),
                        rs.getString("created_at")
                ));
            }

        } catch (SQLException e) {
            System.err.println("Failed to load Accounts");
            e.printStackTrace();
        }

        return list;
    }

    // Only Admins can add accounts
    public boolean addAccount(String username, String password, String role, String currentUserRole) {
        if (!"Admin".equalsIgnoreCase(currentUserRole) && !"A".equalsIgnoreCase(currentUserRole)) {
            System.err.println("Permission denied: Only Admins can add accounts.");
            return false;
        }
        String sql = "INSERT INTO account (username, password, role) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt(12));
            stmt.setString(1, username);
            stmt.setString(2, hashedPassword);
            stmt.setString(3, role);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Failed to add Account");
            e.printStackTrace();
        }
        return false;
    }

    // Only Admins can delete accounts
    public boolean deleteAccount(String id, String currentUserRole) {
        if (!"Admin".equalsIgnoreCase(currentUserRole) && !"A".equalsIgnoreCase(currentUserRole)) {
            System.err.println("Permission denied: Only Admins can delete accounts.");
            return false;
        }
        String sql = "DELETE FROM account WHERE acc_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Failed to delete account");
            e.printStackTrace();
        }
        return false;
    }

    // Get account id by username (for delete)
    public String getAccountIdByUsername(String username) {
        String sql = "SELECT acc_id FROM account WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("acc_id");
            }
        } catch (SQLException e) {
            System.err.println("Failed to get account id by username");
            e.printStackTrace();
        }
        return null;
    }

    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(12));
    }
}