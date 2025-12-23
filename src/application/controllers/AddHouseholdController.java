package application.controllers;

import application.database.HouseholdDAO;
import application.models.Household;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ComboBox;
import application.models.Resident;
import application.database.ResidentDAO;
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
    @FXML
    private ComboBox<Resident> headResidentCombo;

    private boolean householdAdded = false;
    private boolean householdUpdated = false;
    private boolean editMode = false;
    private Household originalHousehold = null;
    private ResidentDAO residentDAO = new ResidentDAO();
    private String currentHouseholdId = null;


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
        String headResidentId = "-";
        Resident selectedHead = headResidentCombo.getValue();
        if (selectedHead != null) {
            headResidentId = selectedHead.getResidentId();
        }
        HouseholdDAO dao = new HouseholdDAO();
        Household newHousehold = new Household(zone, house, headResidentId, 0, "Active");
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
                AuditTrailDAO.logCreate(userId, "H", householdId, "Created household: Zone " + zone + ", House " + house);
            }
            householdAdded = true;
            closeWindow();
        } else {
            showAlert("Failed to add household. Please try again.");
        }
    }

    private void updateHousehold() {
        String zone = zoneNumberField.getText().trim();
        String house = houseNumberField.getText().trim();
        Resident selectedHead = headResidentCombo.getValue();
        if (zone.isEmpty() || house.isEmpty()) {
            showAlert("Zone Number and House Number are required.");
            return;
        }
        if (selectedHead == null) {
            showAlert("Please select a head resident.");
            return;
        }
        Household newHousehold = new Household(zone, house, selectedHead.getResidentId(), 0, "Active");
        HouseholdDAO dao = new HouseholdDAO();
        boolean zoneOrHouseChanged =
            !originalHousehold.getZone().replace("Zone ", "").equals(zone) ||
            !originalHousehold.getHouseNumber().equals(house);
        boolean headChanged = false;
        String oldHeadId = null;
        String oldHeadName = null;
        // Fetch current head resident id for comparison
        try (Connection conn = application.database.DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT head_resident_id FROM households WHERE zone_num = ? AND house_num = ? ORDER BY household_id DESC LIMIT 1")) {
            stmt.setString(1, originalHousehold.getZone().replace("Zone ", ""));
            stmt.setString(2, originalHousehold.getHouseNumber());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                oldHeadId = rs.getString("head_resident_id");
            }
        } catch (Exception e) { e.printStackTrace(); }
        if (oldHeadId != null && !oldHeadId.equals(selectedHead.getResidentId())) {
            headChanged = true;
            // Try to get old head name for better logging (optional)
            try (Connection conn = application.database.DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("SELECT CONCAT(first_name, ' ', last_name) as name FROM residents WHERE resident_id = ?")) {
                stmt.setString(1, oldHeadId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    oldHeadName = rs.getString("name");
                }
            } catch (Exception e) { e.printStackTrace(); }
        }
        boolean success = dao.updateHousehold(originalHousehold, newHousehold);
        boolean headUpdated = false;
        if (success && currentHouseholdId != null && headChanged) {
            headUpdated = dao.updateHeadResident(currentHouseholdId, selectedHead.getResidentId());
        }
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
                if (zoneOrHouseChanged) {
                    String before = "Zone " + originalHousehold.getZone() + " - " + originalHousehold.getHouseNumber();
                    String after = "Zone " + zone + " - " + house;
                    AuditTrailDAO.logUpdate(userId, "H", householdId, "Household: '" + before + "' -> '" + after + "'");
                }
                if (headChanged && headUpdated) {
                    String oldHeadDisplay = (oldHeadName != null) ? oldHeadName : (oldHeadId != null ? oldHeadId : "(unknown)");
                    AuditTrailDAO.logUpdate(userId, "H", householdId, "Head Resident: '" + oldHeadDisplay + "' -> '" + selectedHead.getName() + "'");
                }
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

    public void setEditMode(Household household) {
        this.editMode = true;
        this.originalHousehold = household;
        zoneNumberField.setText(household.getZone().replace("Zone ", ""));
        houseNumberField.setText(household.getHouseNumber());
        saveBtn.setText("Update");
        // Fetch household_id for this household
        try (Connection conn = application.database.DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT household_id, head_resident_id FROM households WHERE zone_num = ? AND house_num = ? ORDER BY household_id DESC LIMIT 1")) {
            stmt.setString(1, household.getZone().replace("Zone ", ""));
            stmt.setString(2, household.getHouseNumber());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                currentHouseholdId = rs.getString("household_id");
                String headResidentId = rs.getString("head_resident_id");
                // Load residents for this household
                java.util.List<Resident> residents = residentDAO.getResidentsByHousehold(currentHouseholdId);
                headResidentCombo.getItems().clear();
                headResidentCombo.getItems().addAll(residents);
                // Set current head resident if available
                if (headResidentId != null) {
                    for (Resident r : residents) {
                        if (headResidentId.equals(r.getResidentId())) {
                            headResidentCombo.setValue(r);
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
