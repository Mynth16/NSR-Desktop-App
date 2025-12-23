package application.controllers;

import application.database.AccountDAO;
import application.models.Account;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import application.controllers.NavigationBaseController;

public class EditAccountController extends NavigationBaseController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label passwordStrengthLabel;
    @FXML private ProgressBar passwordStrengthBar;
    @FXML private ComboBox<String> roleCombo;
    @FXML private Button cancelBtn;
    @FXML private Button saveBtn;

    private AccountDAO accountDAO = new AccountDAO();
    private String accountId = null;

    @FXML
    public void initialize() {
        roleCombo.getItems().addAll("Admin", "Staff", "Viewer");
        passwordStrengthBar.setProgress(0);
        passwordStrengthLabel.setText("Password strength: -");
        passwordField.textProperty().addListener((obs, oldV, newV) -> updatePasswordStrength(newV));
        cancelBtn.setOnAction(e -> closeWindow());
        saveBtn.setOnAction(e -> handleSave());
    }

    public void setAccount(Account account, String id) {
        this.accountId = id;
        usernameField.setText(account.getUsername());
        // Password left blank for security
        String role = "Admin";
        if ("S".equals(account.getRole())) role = "Staff";
        else if ("V".equals(account.getRole())) role = "Viewer";
        roleCombo.setValue(role);
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

    private void closeWindow() {
        Stage stage = (Stage) cancelBtn.getScene().getWindow();
        stage.close();
    }

    private void handleSave() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String role = roleCombo.getValue();
        if (!username.matches("^[A-Za-z0-9_]{3,30}$")) {
            showAlert("Invalid username. Use 3-30 letters, numbers, or underscores.");
            return;
        }
        if (!password.isEmpty() && (password.length() < 8 || !password.matches(".*[A-Za-z].*") || !password.matches(".*[0-9].*"))) {
            showAlert("Password must be at least 8 characters and contain a letter and a number.");
            return;
        }
        if (role == null || role.isEmpty()) {
            showAlert("Please select a role.");
            return;
        }
        // Map role name to single-character code
        String roleCode = "A";
        if (role.equals("Staff")) roleCode = "S";
        else if (role.equals("Viewer")) roleCode = "V";
        String currentUserRole = (this.currentAccount != null && this.currentAccount.getRole() != null) ? this.currentAccount.getRole() : "Viewer";
        boolean success = accountDAO.updateAccount(accountId, username, roleCode, password.isEmpty() ? null : password, currentUserRole);
        if (success) {
            closeWindow();
        } else {
            showAlert("Failed to update account. Username may already exist.");
        }
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        alert.showAndWait();
    }
}
