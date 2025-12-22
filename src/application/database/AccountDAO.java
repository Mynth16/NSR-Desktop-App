package application.database;

import application.models.Account;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

public class AccountDAO {
    public Account login(String username, String password) {
        String sql = "SELECT id, username, password, role FROM account WHERE username = ? AND password = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Account(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("role")
                );
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
                        rs.getInt("id"),
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

            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, role);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Failed to add Account");
            e.printStackTrace();
        }

        return false;
    }

    public boolean updateAccount(int id, String username, String role, String newPasswordOrNull) {
        boolean updatePassword = newPasswordOrNull != null;

        String sql = updatePassword
                ? "UPDATE account SET username=?, role=?, password=? WHERE id=?"
                : "UPDATE account SET username=?, role=? WHERE id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, role);

            if (updatePassword) {
                stmt.setString(3, newPasswordOrNull);
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
}
