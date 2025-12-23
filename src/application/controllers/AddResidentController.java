package application.controllers;


import application.database.ResidentDAO;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import application.database.HouseholdDAO;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class AddResidentController {
    @FXML private TextField firstNameField;
    @FXML private Label titleLabel;
    @FXML private TextField lastNameField;
    @FXML private DatePicker birthDatePicker;
    @FXML private ComboBox<String> genderCombo;
    @FXML private ComboBox<String> civilStatusCombo;
    @FXML private ComboBox<HouseholdDAO.HouseholdOption> householdCombo;
    @FXML private TextField educationalAttainmentField;
    @FXML private TextField contactField;
    @FXML private TextField emailField;
    @FXML private ComboBox<String> voterCombo;
    @FXML private ComboBox<String> pwdCombo;
    @FXML private Button cancelBtn;
    @FXML private Button saveBtn;



    private void closeWindow() {
        Stage stage = (Stage) cancelBtn.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void initializeSaveBtn() {
        saveBtn.setOnAction(e -> saveResident());
    }

    @FXML
    private void saveResident() {
        // Validate required fields
        String firstName = firstNameField.getText();
        String lastName = lastNameField.getText();
        LocalDate birthDate = birthDatePicker.getValue();
        String gender = genderCombo.getValue();
        String civilStatus = civilStatusCombo.getValue();
        // Optional fields
        HouseholdDAO.HouseholdOption selectedOption = householdCombo.getValue();
        String educationalAttainment = educationalAttainmentField.getText();
        String contact = contactField.getText();
        String email = emailField.getText();
        String voter = voterCombo.getValue();
        String pwd = pwdCombo.getValue();

        if (firstName == null || firstName.isEmpty() ||
            lastName == null || lastName.isEmpty() ||
            birthDate == null ||
            gender == null || gender.isEmpty() ||
            civilStatus == null || civilStatus.isEmpty()) {
            showAlert("Please fill in all required fields.");
            return;
        }

        // Format birth date
        String birthDateStr = birthDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        // Map household to householdId (store only the id, not the display string)
        String householdId = (selectedOption != null && !"No Household".equals(selectedOption.getDisplay())) ? selectedOption.getId() : null;

        boolean isVoter = "Yes".equalsIgnoreCase(voter);
        boolean isPwd = "Yes".equalsIgnoreCase(pwd);

        ResidentDAO dao = new ResidentDAO();
        boolean success;
        if (residentToEdit != null) {
            // Update existing resident
            success = dao.updateResident(
                residentToEdit.getResidentId(),
                firstName,
                lastName,
                birthDateStr,
                gender,
                civilStatus,
                householdId,
                educationalAttainment != null && !educationalAttainment.isEmpty() ? educationalAttainment : null,
                contact != null && !contact.isEmpty() ? contact : null,
                email != null && !email.isEmpty() ? email : null,
                isVoter,
                isPwd
            );
        } else {
            // Add new resident
            success = dao.addResident(
                firstName,
                lastName,
                birthDateStr,
                gender,
                civilStatus,
                householdId,
                educationalAttainment != null && !educationalAttainment.isEmpty() ? educationalAttainment : null,
                contact != null && !contact.isEmpty() ? contact : null,
                email != null && !email.isEmpty() ? email : null,
                isVoter,
                isPwd
            );
        }
        if (success) {
            closeWindow();
        } else {
            showAlert(residentToEdit != null ? "Failed to update resident. Please try again." : "Failed to add resident. Please try again.");
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Validation Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void initialize() {
        genderCombo.getItems().addAll("Male", "Female", "Other");
        civilStatusCombo.getItems().addAll("Single", "Married", "Widowed", "Separated");
        voterCombo.getItems().addAll("Yes", "No");
        pwdCombo.getItems().addAll("Yes", "No");
        // Populate householdCombo from DB
        HouseholdDAO householdDAO = new HouseholdDAO();
        householdCombo.getItems().clear();
        householdCombo.getItems().add(new HouseholdDAO.HouseholdOption(null, "No Household"));
        householdCombo.getItems().addAll(householdDAO.getHouseholdOptions());
        householdCombo.getSelectionModel().selectFirst();
        cancelBtn.setOnAction(e -> closeWindow());
        saveBtn.setOnAction(e -> saveResident());
    }

    private application.models.Resident residentToEdit = null;

    public void setResidentToEdit(application.models.Resident resident) {
        this.residentToEdit = resident;
        if (titleLabel != null) {
            titleLabel.setText(resident != null ? "Edit Resident" : "Add New Resident");
        }
        // Pre-fill fields
        if (resident != null) {
            String[] names = resident.getName().split(" ", 2);
            firstNameField.setText(names.length > 0 ? names[0] : "");
            lastNameField.setText(names.length > 1 ? names[1] : "");
            if (resident.getBirthDate() != null && !resident.getBirthDate().isEmpty()) {
                birthDatePicker.setValue(LocalDate.parse(resident.getBirthDate()));
            }
            genderCombo.setValue(resident.getGender());
            civilStatusCombo.setValue(resident.getCivilStatus());
            // Household: try to select matching household
            for (HouseholdDAO.HouseholdOption opt : householdCombo.getItems()) {
                if (resident.getHousehold() != null && opt.getDisplay() != null && resident.getHousehold().equals(opt.getDisplay())) {
                    householdCombo.setValue(opt);
                    break;
                }
            }
            educationalAttainmentField.setText(resident.getEducationalAttainment());
            contactField.setText(resident.getContact());
            emailField.setText(""); // Not available in Resident model, left blank
            voterCombo.setValue(resident.isRegisteredVoter() ? "Yes" : "No");
            pwdCombo.setValue(resident.isPwd() ? "Yes" : "No");
            saveBtn.setText("Update Resident");
        } else {
            // Reset title and button for add
            if (titleLabel != null) titleLabel.setText("Add New Resident");
            if (saveBtn != null) saveBtn.setText("Save Resident");
        }
    }
}
