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


    }



    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}