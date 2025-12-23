package application.controllers;

import application.database.HouseholdDAO;
import application.models.Household;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

public class AddHouseholdController {
    @FXML
    private TextField zoneNumberField;
    @FXML
    private TextField houseNumberField;
    @FXML
    private Button saveBtn;
    @FXML
    private Button cancelBtn;

    private boolean householdAdded = false;

    @FXML
    private void initialize() {
        saveBtn.setOnAction(e -> saveHousehold());
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
        // Provide all required arguments for Household constructor
        Household newHousehold = new Household(zone, house, "-", 0, "Active");
        boolean success = dao.addHousehold(newHousehold);
        if (success) {
            householdAdded = true;
            closeWindow();
        } else {
            showAlert("Failed to add household. Please try again.");
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
}
