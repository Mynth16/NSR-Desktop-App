package application.controllers;

import application.database.HouseholdDAO;
import application.models.Household;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

import application.database.AuditTrailDAO;
import java.sql.*;
import application.database.DBConnection;

public class AddHouseholdController extends application.controllers.NavigationBaseController {
    @FXML
    private TextField zoneNumberField;
    @FXML
    private TextField houseNumberField;
    @FXML
    private Button saveBtn;
    @FXML
    private Button cancelBtn;

    private boolean householdAdded = false;
    private boolean householdUpdated = false;
    private boolean editMode = false;
    private Household originalHousehold = null;


    @FXML
    private void initialize() {
        saveBtn.setOnAction(e -> {
            if (editMode) {
                updateHousehold();
            } else {
                saveHousehold();
            }
        });
        cancelBtn.setOnAction(e -> closeWindow());
    }

    private void saveHousehold() {
        String zone = zoneNumberField.getText().trim();
        String house = houseNumberField.getText().trim();
        if (zone.isEmpty() || house.isEmpty()) {
            showAlert("Zone Number and House Number are required.");
            return;
        }
        HouseholdDAO dao = new HouseholdDAO();
        Household newHousehold = new Household(zone, house, "-", 0, "Active");
        boolean success = dao.addHousehold(newHousehold);
        if (success) {
            // Get household_id for audit log
            String householdId = null;
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("SELECT household_id FROM households WHERE zone_num = ? AND house_num = ? ORDER BY household_id DESC LIMIT 1")) {
                stmt.setString(1, zone);
                stmt.setString(2, house);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    householdId = rs.getString("household_id");
                }
            } catch (SQLException e) { e.printStackTrace(); }
            if (this.currentAccount != null) {
                String userId = this.currentAccount.getId();
                AuditTrailDAO.logCreate(userId, "H", householdId, "Initial household registration");
            }
            householdAdded = true;
        }
        closeWindow();
        showAlert("Failed to add household. Please try again.");
    }

    private void updateHousehold() {
        String zone = zoneNumberField.getText().trim();
        String house = houseNumberField.getText().trim();
        if (zone.isEmpty() || house.isEmpty()) {
            showAlert("Zone Number and House Number are required.");
            return;
        }
        Household newHousehold = new Household(zone, house, "-", 0, "Active");
        HouseholdDAO dao = new HouseholdDAO();
        boolean success = dao.updateHousehold(originalHousehold, newHousehold);
        if (success) {
            // Get household_id for audit log
            String householdId = null;
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("SELECT household_id FROM households WHERE zone_num = ? AND house_num = ? ORDER BY household_id DESC LIMIT 1")) {
                stmt.setString(1, zone);
                stmt.setString(2, house);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    householdId = rs.getString("household_id");
                }
            } catch (SQLException e) { e.printStackTrace(); }
            if (this.currentAccount != null) {
                String userId = this.currentAccount.getId();
                AuditTrailDAO.logUpdate(userId, "H", householdId, "Updated household: Zone " + zone + ", House " + house);
            }
            householdUpdated = true;
            closeWindow();
        } else {
            showAlert("Failed to update household. Please try again.");
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) cancelBtn.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public boolean isHouseholdAdded() {
        return householdAdded;
    }

    public boolean isHouseholdUpdated() {
        return householdUpdated;
    }

    /**
     * Call this to set the controller in edit mode and load the household data.
     */
    public void setEditMode(Household household) {
        this.editMode = true;
        this.originalHousehold = household;
        zoneNumberField.setText(household.getZone().replace("Zone ", ""));
        houseNumberField.setText(household.getHouseNumber());
        saveBtn.setText("Update");
    }
}
