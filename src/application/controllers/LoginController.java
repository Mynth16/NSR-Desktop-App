package application.controllers;

import application.database.AccountDAO;
import application.models.Account;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class LoginController {
    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button signInButton;

    private final AccountDAO accountDAO = new AccountDAO();

    @FXML
    private void initialize() {
        signInButton.setOnAction(e -> handleSignIn());

        // Allow Enter key to submit
        passwordField.setOnAction(e -> handleSignIn());
    }

    private void handleSignIn() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both username and password.");
            return;
        }

        Account account = accountDAO.login(username, password);

        if (account == null) {
            showError("Invalid credentials. Please try again.");
            return;
        }

        // Successful login - navigate to dashboard
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/views/dashboard.fxml"));
            Parent root = loader.load();

            // Pass the account to the dashboard controller
            DashboardController dashboardController = loader.getController();
            dashboardController.setAccount(account);

            Scene scene = new Scene(root);
            Stage stage = (Stage) signInButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.centerOnScreen();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to load dashboard: " + e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}