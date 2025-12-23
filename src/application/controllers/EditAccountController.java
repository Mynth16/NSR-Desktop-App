package application.controllers;

import application.database.AccountDAO;
import application.models.Account;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;


import application.database.HouseholdDAO;
import application.database.ResidentDAO;
import application.models.Resident;
import java.util.List;

public class EditAccountController extends NavigationBaseController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label passwordStrengthLabel;
    @FXML private ProgressBar passwordStrengthBar;
    @FXML private ComboBox<String> roleCombo;
    @FXML private ComboBox<Resident> headResidentCombo;
    @FXML private Button cancelBtn;
    @FXML private Button saveBtn;

    private AccountDAO accountDAO = new AccountDAO();
    private ResidentDAO residentDAO = new ResidentDAO();
    private HouseholdDAO householdDAO = new HouseholdDAO();
    private String accountId = null;
    private String householdId = null;


    @FXML
    public void initialize() {
        roleCombo.getItems().addAll("Admin", "Staff", "Viewer");
        passwordStrengthBar.setProgress(0);
        passwordStrengthLabel.setText("Password strength: -");
        passwordField.textProperty().addListener((obs, oldV, newV) -> updatePasswordStrength(newV));
        cancelBtn.setOnAction(e -> closeWindow());
        saveBtn.setOnAction(e -> handleSave());
    }

    // Call this to set household context for editing
    public void setHouseholdContext(String householdId) {
        this.householdId = householdId;
        loadResidentsForHousehold();
    }

    private void loadResidentsForHousehold() {
        if (householdId == null) return;
        List<Resident> residents = residentDAO.getResidentsByHousehold(householdId);
        headResidentCombo.getItems().clear();
        headResidentCombo.getItems().addAll(residents);
    }

    public void setAccount(Account account, String id, String householdId, String headResidentId) {
        this.accountId = id;
        this.householdId = householdId;
        usernameField.setText(account.getUsername());
        // Password left blank for security
        String role = "Admin";
        if ("S".equals(account.getRole())) role = "Staff";
        else if ("V".equals(account.getRole())) role = "Viewer";
        roleCombo.setValue(role);
        loadResidentsForHousehold();
        // Set current head resident if available
        if (headResidentId != null && headResidentCombo.getItems() != null) {
            for (Resident r : headResidentCombo.getItems()) {
                if (headResidentId.equals(r.getResidentId())) {
                    headResidentCombo.setValue(r);
                    break;
                }
            }
        }
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
        Resident selectedHead = headResidentCombo.getValue();
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
        if (selectedHead == null) {
            showAlert("Please select a head resident.");
            return;
        }

        String roleCode = "A";
        if (role.equals("Staff")) roleCode = "S";
        else if (role.equals("Viewer")) roleCode = "V";
        String currentUserRole = (this.currentAccount != null && this.currentAccount.getRole() != null) ? this.currentAccount.getRole() : "Viewer";
        boolean success = accountDAO.updateAccount(accountId, username, roleCode, password.isEmpty() ? null : password, currentUserRole);
        // Update head resident in household
        if (success && householdId != null && selectedHead != null) {
            boolean headUpdated = householdDAO.updateHeadResident(householdId, selectedHead.getResidentId());
            if (headUpdated && this.currentAccount != null) {
                // Audit log for head resident change
                String userId = this.currentAccount.getId();
                application.database.AuditTrailDAO.logUpdate(userId, "H", householdId, "Changed head resident to: " + selectedHead.getName());
            }
        }
        if (success && this.currentAccount != null) {
            // Audit log for account update
            String userId = this.currentAccount.getId();
            application.database.AuditTrailDAO.logUpdate(userId, "A", accountId, "Updated account: " + username + " (Role: " + roleCode + ")");
        }
        closeWindow();
        showAlert("Failed to update account. Username may already exist.");
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        alert.showAndWait();
    }
}
