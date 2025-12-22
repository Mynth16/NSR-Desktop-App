
package application.controllers;



import application.models.Account;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import application.database.HouseholdDAO;
import application.database.ResidentDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import java.util.Map;



public class DashboardController {
    @FXML private Label sidebarNameLabel;
    @FXML private Label sidebarRoleLabel;

        private Account currentAccount;

        public void setAccount(Account account) {
            this.currentAccount = account;
            if (sidebarNameLabel != null && sidebarRoleLabel != null && account != null) {
                sidebarNameLabel.setText(account.getUsername() != null ? account.getUsername() : "User");
                String role = account.getRole();
                String displayRole;
                if (role == null) {
                    displayRole = "Viewer";
                } else {
                    switch (role) {
                        case "A": displayRole = "Admin"; break;
                        case "S": displayRole = "Staff"; break;
                        case "V": displayRole = "Viewer"; break;
                        default: displayRole = role;
                    }
                }
                sidebarRoleLabel.setText(displayRole);
            }
        }
    @FXML private Button dashboardBtn;
    @FXML private Button populationBtn;
    @FXML private Button householdsBtn;
    @FXML private Button accountBtn;
    @FXML private Button auditBtn;
    @FXML private Button logoutBtn;

    // Dashboard statistic labels (add fx:id to FXML for these)
    @FXML private Label totalPopulationLabel;
    @FXML private Label totalHouseholdsLabel;
    @FXML private Label maleCountLabel;
    @FXML private Label femaleCountLabel;
    @FXML private Label voterCountLabel;
    @FXML private Label pwdCountLabel;

    // Zone and age distribution labels (add fx:id to FXML for these)
    @FXML private Label zone1HouseholdsLabel;
    @FXML private Label zone1PeopleLabel;
    @FXML private Label zone2HouseholdsLabel;
    @FXML private Label zone2PeopleLabel;
    @FXML private Label zone3HouseholdsLabel;
    @FXML private Label zone3PeopleLabel;
    @FXML private Label zone4HouseholdsLabel;
    @FXML private Label zone4PeopleLabel;
    @FXML private Label zone5HouseholdsLabel;
    @FXML private Label zone5PeopleLabel;

    @FXML private Label minorCountLabel;
    @FXML private Label youngAdultCountLabel;
    @FXML private Label adultCountLabel;
    @FXML private Label middleAgedCountLabel;
    @FXML private Label seniorCountLabel;

    @FXML private javafx.scene.control.ProgressBar minorProgressBar;
    @FXML private javafx.scene.control.ProgressBar youngAdultProgressBar;
    @FXML private javafx.scene.control.ProgressBar adultProgressBar;
    @FXML private javafx.scene.control.ProgressBar middleAgedProgressBar;
    @FXML private javafx.scene.control.ProgressBar seniorProgressBar;

    private ResidentDAO residentDAO = new ResidentDAO();
    private HouseholdDAO householdDAO = new HouseholdDAO();

    @FXML
    private void initialize() {
        dashboardBtn.setOnAction(e -> navigateToDashboard());
        populationBtn.setOnAction(e -> navigateToPopulation());
        householdsBtn.setOnAction(e -> navigateToHouseholds());
        accountBtn.setOnAction(e -> navigateToAccounts());
        auditBtn.setOnAction(e -> navigateToAuditTrail());
        logoutBtn.setOnAction(e -> handleLogout());

        loadDashboardStats();
    }

    private void navigateToDashboard() {
        // Already on dashboard
        System.out.println("Already on Dashboard");
    }

    private void navigateToPopulation() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/views/population.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) dashboardBtn.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
            showError("Population module not yet implemented.");
        }
    }

    private void navigateToHouseholds() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/views/households.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) dashboardBtn.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
            showError("Households module not yet implemented.");
        }
    }

    private void navigateToAccounts() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/views/accounts.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) dashboardBtn.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
            showError("Accounts module not yet implemented.");
        }
    }

    private void navigateToAuditTrail() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/views/audittrail.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) dashboardBtn.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
            showError("Audit Trail module not yet implemented.");
        }
    }

    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to logout?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/views/login.fxml"));
                    Parent root = loader.load();
                    Scene scene = new Scene(root);
                    Stage stage = (Stage) logoutBtn.getScene().getWindow();
                    stage.setScene(scene);
                    stage.setMaximized(false);
                    stage.setResizable(false);
                    stage.centerOnScreen();
                } catch (Exception e) {
                    e.printStackTrace();
                    showError("Failed to logout: " + e.getMessage());
                }
            }
        });
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private void loadDashboardStats() {
        // Population and households
        int totalPopulation = residentDAO.getTotalPopulation();
        int totalHouseholds = householdDAO.getTotalHouseholds();
        int maleCount = residentDAO.getMaleCount();
        int femaleCount = residentDAO.getFemaleCount();
        int voterCount = residentDAO.getVoterCount();
        int pwdCount = residentDAO.getPWDCount();

        totalPopulationLabel.setText(String.valueOf(totalPopulation));
        totalHouseholdsLabel.setText(String.valueOf(totalHouseholds));
        maleCountLabel.setText(String.valueOf(maleCount));
        femaleCountLabel.setText(String.valueOf(femaleCount));
        voterCountLabel.setText(String.valueOf(voterCount));
        pwdCountLabel.setText(String.valueOf(pwdCount));

        // Zone breakdown
        Map<String, Integer> zonePeople = residentDAO.getPopulationByZone();
        Map<String, Integer> zoneHouseholds = householdDAO.getHouseholdsByZone();
        zone1HouseholdsLabel.setText(zoneHouseholds.getOrDefault("1", 0) + " households");
        zone1PeopleLabel.setText(String.valueOf(zonePeople.getOrDefault("1", 0)));
        zone2HouseholdsLabel.setText(zoneHouseholds.getOrDefault("2", 0) + " households");
        zone2PeopleLabel.setText(String.valueOf(zonePeople.getOrDefault("2", 0)));
        zone3HouseholdsLabel.setText(zoneHouseholds.getOrDefault("3", 0) + " households");
        zone3PeopleLabel.setText(String.valueOf(zonePeople.getOrDefault("3", 0)));
        zone4HouseholdsLabel.setText(zoneHouseholds.getOrDefault("4", 0) + " households");
        zone4PeopleLabel.setText(String.valueOf(zonePeople.getOrDefault("4", 0)));
        zone5HouseholdsLabel.setText(zoneHouseholds.getOrDefault("5", 0) + " households");
        zone5PeopleLabel.setText(String.valueOf(zonePeople.getOrDefault("5", 0)));

        // Age distribution
        Map<String, Integer> ageDist = residentDAO.getAgeDistribution();
        int minor = ageDist.getOrDefault("Minor", 0);
        int youngAdult = ageDist.getOrDefault("Young Adult", 0);
        int adult = ageDist.getOrDefault("Adult", 0);
        int middleAged = ageDist.getOrDefault("Middle-aged", 0);
        int senior = ageDist.getOrDefault("Senior", 0);

        int total = minor + youngAdult + adult + middleAged + senior;
        minorCountLabel.setText(String.valueOf(minor));
        youngAdultCountLabel.setText(String.valueOf(youngAdult));
        adultCountLabel.setText(String.valueOf(adult));
        middleAgedCountLabel.setText(String.valueOf(middleAged));
        seniorCountLabel.setText(String.valueOf(senior));

        minorProgressBar.setProgress(total > 0 ? (double) minor / total : 0);
        youngAdultProgressBar.setProgress(total > 0 ? (double) youngAdult / total : 0);
        adultProgressBar.setProgress(total > 0 ? (double) adult / total : 0);
        middleAgedProgressBar.setProgress(total > 0 ? (double) middleAged / total : 0);
        seniorProgressBar.setProgress(total > 0 ? (double) senior / total : 0);
    }

    private String percentString(int count, int total) {
        if (total == 0) return "(0%)";
        double percent = (double) count / total * 100.0;
        return String.format("(%1.1f%%)", percent);
    }

    // ...existing navigation and error handling code...
}