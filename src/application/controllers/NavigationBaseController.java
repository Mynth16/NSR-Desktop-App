package application.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

public abstract class NavigationBaseController {

    @FXML protected Button dashboardBtn;
    @FXML protected Button populationBtn;
    @FXML protected Button householdsBtn;
    @FXML protected Button accountBtn;
    @FXML protected Button auditBtn;
    @FXML protected Button logoutBtn;

    protected void bindNavigationHandlers() {
        if (dashboardBtn != null) dashboardBtn.setOnAction(e -> navigateToDashboard());
        if (populationBtn != null) populationBtn.setOnAction(e -> navigateToPopulation());
        if (householdsBtn != null) householdsBtn.setOnAction(e -> navigateToHouseholds());
        if (accountBtn != null) accountBtn.setOnAction(e -> navigateToAccounts());
        if (auditBtn != null) auditBtn.setOnAction(e -> navigateToAuditTrail());
        if (logoutBtn != null) logoutBtn.setOnAction(e -> handleLogout());
    }

    application.models.Account currentAccount;

    public void setAccount(application.models.Account account) {
        this.currentAccount = account;
    }

    private void navigate(String page, String errorMessage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(page));
            Parent root = loader.load();

            // Pass account info to next controller
            Object controller = loader.getController();
            if (controller instanceof NavigationBaseController && this.currentAccount != null) {
                ((NavigationBaseController) controller).setAccount(this.currentAccount);
            }

            // Get the current stage
            Stage stage = (Stage) logoutBtn.getScene().getWindow();

            Scene currentScene = stage.getScene();

            if (currentScene != null) {
                currentScene.setRoot(root);
            } else {
                // Only create a new scene if one doesn't exist (failsafe)
                currentScene = new Scene(root);
                stage.setScene(currentScene);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError(errorMessage);
        }
    }

    // --- Navigation Methods ---

    protected void navigateToDashboard() {
        navigate("/application/views/dashboard.fxml", "Failed to load Dashboard.");
    }

    protected void navigateToPopulation() {
        navigate("/application/views/population.fxml", "Failed to load Population module.");
    }

    protected void navigateToHouseholds() {
        navigate("/application/views/households.fxml", "Households module not yet implemented.");
    }

    protected void navigateToAccounts() {
        navigate("/application/views/accounts.fxml", "Accounts module not yet implemented.");
    }

    protected void navigateToAuditTrail() {
        navigate("/application/views/audittrail.fxml", "Audit Trail module not yet implemented.");
    }

    protected void handleLogout() {
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

    protected void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}