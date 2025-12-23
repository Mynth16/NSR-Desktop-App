package application.controllers;

import application.database.ResidentDAO;
import application.database.AuditTrailDAO;
import application.models.Account;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import application.database.HouseholdDAO;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class AddResidentController {
    private Account currentAccount;
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

        // Format birthdate
        String birthDateStr = birthDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        // Map household to householdId (store only the id, not the display string)
        String householdId = (selectedOption != null && !"No Household".equals(selectedOption.getDisplay())) ? selectedOption.getId() : null;

        boolean isVoter = "Yes".equalsIgnoreCase(voter);
        boolean isPwd = "Yes".equalsIgnoreCase(pwd);

        ResidentDAO dao = new ResidentDAO();
        boolean success;
        if (residentToEdit != null) {
            StringBuilder changes = new StringBuilder();
            java.util.function.Function<String, String> clean = s -> (s == null || s.trim().isEmpty() || "null".equalsIgnoreCase(s.trim())) ? "" : s.trim();

            String beforeFirstName = clean.apply(residentToEdit.getName().split(" ", 2)[0]);
            String afterFirstName = clean.apply(firstName);
            if (!afterFirstName.equals(beforeFirstName)) {
                changes.append("First Name: '").append(beforeFirstName).append("' -> '").append(afterFirstName).append("'\n");
            }
            String beforeLastName = clean.apply(residentToEdit.getName().split(" ", 2).length > 1 ? residentToEdit.getName().split(" ", 2)[1] : "");
            String afterLastName = clean.apply(lastName);
            if (!afterLastName.equals(beforeLastName)) {
                changes.append("Last Name: '").append(beforeLastName).append("' -> '").append(afterLastName).append("'\n");
            }
            String beforeBirthDate = clean.apply(residentToEdit.getBirthDate());
            String afterBirthDate = clean.apply(birthDateStr);
            if (!afterBirthDate.equals(beforeBirthDate)) {
                changes.append("Birth Date: '").append(beforeBirthDate).append("' -> '").append(afterBirthDate).append("'\n");
            }
            String beforeGender = clean.apply(residentToEdit.getGender());
            String afterGender = clean.apply(gender);
            if (!afterGender.equals(beforeGender)) {
                changes.append("Gender: '").append(beforeGender).append("' -> '").append(afterGender).append("'\n");
            }
            String beforeCivilStatus = clean.apply(residentToEdit.getCivilStatus());
            String afterCivilStatus = clean.apply(civilStatus);
            if (!afterCivilStatus.equals(beforeCivilStatus)) {
                changes.append("Civil Status: '").append(beforeCivilStatus).append("' -> '").append(afterCivilStatus).append("'\n");
            }
            String beforeHousehold = clean.apply(residentToEdit.getHousehold());
            String afterHousehold = beforeHousehold;
            if (selectedOption != null && !"No Household".equals(selectedOption.getDisplay())) {
                afterHousehold = clean.apply(selectedOption.getDisplay());
            } else if (selectedOption != null) {
                afterHousehold = "No Household";
            }
            if (!afterHousehold.equals(beforeHousehold)) {
                changes.append("Household: '").append(beforeHousehold).append("' -> '").append(afterHousehold).append("'\n");
            }
            String beforeContact = clean.apply(residentToEdit.getContact());
            String afterContact = clean.apply(contact);
            if (!afterContact.equals(beforeContact)) {
                changes.append("Contact: '").append(beforeContact).append("' -> '").append(afterContact).append("'\n");
            }
            String beforeEdu = clean.apply(residentToEdit.getEducationalAttainment());
            String afterEdu = clean.apply(educationalAttainment);
            if (!afterEdu.equals(beforeEdu)) {
                changes.append("Educational Attainment: '").append(beforeEdu).append("' -> '").append(afterEdu).append("'\n");
            }
            String beforeVoter = residentToEdit.isRegisteredVoter() ? "Yes" : "No";
            String afterVoter = isVoter ? "Yes" : "No";
            if (!afterVoter.equals(beforeVoter)) {
                changes.append("Voter: '").append(beforeVoter).append("' -> '").append(afterVoter).append("'\n");
            }
            String beforePwd = residentToEdit.isPwd() ? "Yes" : "No";
            String afterPwd = isPwd ? "Yes" : "No";
            if (!afterPwd.equals(beforePwd)) {
                changes.append("PWD: '").append(beforePwd).append("' -> '").append(afterPwd).append("'\n");
            }
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
            if (success && currentAccount != null && changes.length() > 0) {
                AuditTrailDAO.logUpdate(currentAccount.getId(), "R", residentToEdit.getResidentId(), changes.toString().trim());
            }
        } else {
            // Add new resident
            String newResidentId = dao.addResident(
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
            success = newResidentId != null;
            if (success && currentAccount != null) {
                String details = "Created resident: " + firstName + " " + lastName;
                AuditTrailDAO.logCreate(currentAccount.getId(), "R", newResidentId, details);
            }
        }
        if (success) {
            closeWindow();
        } else {
            showAlert(residentToEdit != null ? "Failed to update resident. Please try again." : "Failed to add resident. Please try again.");
        }
    }

    public void setCurrentAccount(Account account) {
        this.currentAccount = account;
    }


    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Validation Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


}
