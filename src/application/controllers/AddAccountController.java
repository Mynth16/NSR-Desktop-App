package application.controllers;

import application.database.AccountDAO;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class AddAccountController extends NavigationBaseController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordTextField;
    @FXML private Button showPasswordBtn;
    @FXML private Label passwordStrengthLabel;
    @FXML private ProgressBar passwordStrengthBar;
    @FXML private ComboBox<String> roleCombo;
    @FXML private Button cancelBtn;
    @FXML private Button createBtn;

    private AccountDAO accountDAO = new AccountDAO();

    @FXML
    public void initialize() {
        roleCombo.getItems().addAll("Admin", "Staff", "Viewer");
        passwordStrengthBar.setProgress(0);
        passwordStrengthLabel.setText("Password strength: -");
        passwordTextField.setManaged(false);
        passwordTextField.setVisible(false);
        passwordTextField.managedProperty().bind(passwordTextField.visibleProperty());
        passwordTextField.visibleProperty().addListener((obs, oldV, newV) -> {
            if (newV) passwordTextField.setText(passwordField.getText());
        });
        passwordField.textProperty().addListener((obs, oldV, newV) -> {
            updatePasswordStrength(newV);
            if (!passwordTextField.isVisible()) passwordTextField.setText(newV);
        });
        passwordTextField.textProperty().addListener((obs, oldV, newV) -> {
            if (passwordTextField.isVisible()) passwordField.setText(newV);
        });
        showPasswordBtn.setOnAction(e -> togglePasswordVisibility());
        cancelBtn.setOnAction(e -> closeWindow());
        createBtn.setOnAction(e -> handleCreateAccount());
    }

    private void updatePasswordStrength(String password) {
        int score = 0;
        if (password.length() >= 8) score++;
        if (password.matches(".*[A-Za-z].*")) score++;
        if (password.matches(".*[0-9].*")) score++;
        passwordStrengthBar.setProgress(score / 3.0);
        if (score == 3) {
            passwordStrengthLabel.setText("Password strength: Strong");
        } else if (score == 2) {
            passwordStrengthLabel.setText("Password strength: Medium");
        } else {
            passwordStrengthLabel.setText("Password strength: Weak");
        }
    }

    private void togglePasswordVisibility() {
        if (passwordTextField.isVisible()) {
            // Hide password
            passwordTextField.setVisible(false);
            passwordField.setVisible(true);
            showPasswordBtn.setText("\uD83D\uDC41"); // 👁
        } else {
            // Show password
            passwordTextField.setText(passwordField.getText());
            passwordTextField.setVisible(true);
            passwordField.setVisible(false);
            showPasswordBtn.setText("\uD83D\uDC41\u200D\uD83D\uDD12"); // 👁‍🔒
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) cancelBtn.getScene().getWindow();
        stage.close();
    }

    private void handleCreateAccount() {
        String username = usernameField.getText().trim();
        String password = passwordField.isVisible() ? passwordField.getText() : passwordTextField.getText();
        String role = roleCombo.getValue();
        if (!username.matches("^[A-Za-z0-9_]{3,30}$")) {
            showAlert("Invalid username. Use 3-30 letters, numbers, or underscores.");
            return;
        }
        if (password.length() < 8 || !password.matches(".*[A-Za-z].*") || !password.matches(".*[0-9].*")) {
            showAlert("Password must be at least 8 characters and contain a letter and a number.");
            return;
        }
        if (role == null || role.isEmpty()) {
            showAlert("Please select a role.");
            return;
        }

        String roleCode = "A";
        if (role.equals("Staff")) roleCode = "S";
        else if (role.equals("Viewer")) roleCode = "V";
        // Get current user role from NavigationBaseController
        String currentUserRole = (this.currentAccount != null && this.currentAccount.getRole() != null) ? this.currentAccount.getRole() : "Viewer";
        System.out.println("[DEBUG] currentAccount: " + (this.currentAccount != null ? this.currentAccount.getUsername() : "null") + ", role: " + currentUserRole);
        boolean success = accountDAO.addAccount(username, password, roleCode, currentUserRole);
        if (success) {
            if (this.currentAccount != null) {
                // Audit log
                String userId = this.currentAccount.getId();
                String newAccId = accountDAO.getAccountIdByUsername(username);
                application.database.AuditTrailDAO.logCreate(userId, "A", newAccId, "Created account: " + username + " (Role: " + roleCode + ")");
            }
            closeWindow();
        } else {
            showAlert("Failed to create account. Username may already exist or you lack permission.");
        }
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        alert.showAndWait();
    }
}
