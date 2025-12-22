package application.database;

import application.models.Account;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;

public class AccountDAO {
    public Account login(String username, String password) {
        String sql = "SELECT username, password, role FROM account WHERE username = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String hashedPassword = rs.getString("password");

                // Check if password is null or empty
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
                                rs.getString("username"),
                                hashedPassword,
                                rs.getString("role")
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
        String sql = "SELECT * FROM account ORDER BY id ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(new Account(
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("role")
                ));
            }

        } catch (SQLException e) {
            System.err.println("Failed to load Accounts");
            e.printStackTrace();
        }

        return list;
    }

    public boolean addAccount(String username, String password, String role) {
        String sql = "INSERT INTO account (username, password, role) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Hash the password before storing
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

    public boolean updateAccount(int id, String username, String role, String newPasswordOrNull) {
        boolean updatePassword = newPasswordOrNull != null && !newPasswordOrNull.isEmpty();

        String sql = updatePassword
                ? "UPDATE account SET username=?, role=?, password=? WHERE id=?"
                : "UPDATE account SET username=?, role=? WHERE id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, role);

            if (updatePassword) {
                // Hash the new password before storing
                String hashedPassword = BCrypt.hashpw(newPasswordOrNull, BCrypt.gensalt(12));
                stmt.setString(3, hashedPassword);
                stmt.setInt(4, id);
            } else {
                stmt.setInt(3, id);
            }

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Failed to update Account");
            e.printStackTrace();
        }

        return false;
    }

    public boolean deleteAccount(int id) {
        String sql = "DELETE FROM account WHERE id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Failed to delete account");
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Utility method to hash a password for manual database insertion
     * Usage: String hashed = AccountDAO.hashPassword("mypassword");
     */
    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(12));
    }
}