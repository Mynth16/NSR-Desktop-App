package application.controllers;

import application.models.Resident;
import application.database.ResidentDAO;
import application.database.AuditTrailDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.util.List;
import java.util.stream.Collectors;

public class ResidentController extends NavigationBaseController {
	// Sidebar navigation handlers
	@FXML
	private Label sidebarNameLabel;
	@FXML
	private Label sidebarRoleLabel;
	@FXML
	private Label sidebarUsernameLabel;


	@FXML private TableView<Resident> residentTable;
	@FXML private TableColumn<Resident, String> nameCol;
	@FXML private TableColumn<Resident, Integer> ageCol;
	@FXML private TableColumn<Resident, String> genderCol;
	@FXML private TableColumn<Resident, String> civilStatusCol;
	@FXML private TableColumn<Resident, String> householdCol;
	@FXML private TableColumn<Resident, String> contactCol;
	@FXML private TableColumn<Resident, String> birthDateCol;
	@FXML private TableColumn<Resident, String> educationalAttainmentCol;
	@FXML private TableColumn<Resident, String> registeredVoterCol;
	@FXML private TableColumn<Resident, Void> actionsCol;

	@FXML private TextField searchField;
	@FXML private ComboBox<String> genderFilter;
	@FXML private TextField minAgeField;
	@FXML private TextField maxAgeField;
	@FXML private ComboBox<String> zoneFilter;
	@FXML private ComboBox<String> civilStatusFilter;
	@FXML private ComboBox<String> voterFilter;
	@FXML private ComboBox<String> pwdFilter;
	@FXML private Button clearFiltersBtn;
	@FXML private Button addResidentBtn;

	private ObservableList<Resident> masterList = FXCollections.observableArrayList();
	private ResidentDAO residentDAO = new ResidentDAO();

	@FXML
	public void initialize() {
		bindNavigationHandlers();
	}

	@Override
	public void setAccount(application.models.Account account) {
		super.setAccount(account);
		if (account != null) {
			if (sidebarNameLabel != null) {
				sidebarNameLabel.setText(account.getUsername());
			}
			if (sidebarUsernameLabel != null) {
				sidebarUsernameLabel.setText(account.getUsername());
			}
			if (sidebarRoleLabel != null) {
				String role = account.getRole();
				String display;
				if (role == null) display = "Viewer";
				else if (role.equalsIgnoreCase("A") || role.equalsIgnoreCase("Admin")) display = "Admin";
				else if (role.equalsIgnoreCase("S") || role.equalsIgnoreCase("Staff")) display = "Staff";
				else if (role.equalsIgnoreCase("V") || role.equalsIgnoreCase("Viewer")) display = "Viewer";
				else display = role;
				sidebarRoleLabel.setText(display);
			}
		}
		nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
		ageCol.setCellValueFactory(new PropertyValueFactory<>("age"));
		genderCol.setCellValueFactory(new PropertyValueFactory<>("gender"));
		civilStatusCol.setCellValueFactory(new PropertyValueFactory<>("civilStatus"));
		householdCol.setCellValueFactory(new PropertyValueFactory<>("household"));
		contactCol.setCellValueFactory(new PropertyValueFactory<>("contact"));
		birthDateCol.setCellValueFactory(new PropertyValueFactory<>("birthDate"));
		educationalAttainmentCol.setCellValueFactory(new PropertyValueFactory<>("educationalAttainment"));
		registeredVoterCol.setCellValueFactory(new PropertyValueFactory<>("registeredVoter"));
		addActionsColumn();

		genderFilter.setItems(FXCollections.observableArrayList("All", "Male", "Female"));
		civilStatusFilter.setItems(FXCollections.observableArrayList("All", "Single", "Married", "Widowed"));
		voterFilter.setItems(FXCollections.observableArrayList("All", "Yes", "No"));
		pwdFilter.setItems(FXCollections.observableArrayList("All", "Yes", "No"));

		loadResidents();

		searchField.textProperty().addListener((obs, oldV, newV) -> filterResidents());
		genderFilter.valueProperty().addListener((obs, oldV, newV) -> filterResidents());
		minAgeField.textProperty().addListener((obs, oldV, newV) -> filterResidents());
		maxAgeField.textProperty().addListener((obs, oldV, newV) -> filterResidents());
		zoneFilter.valueProperty().addListener((obs, oldV, newV) -> filterResidents());
		civilStatusFilter.valueProperty().addListener((obs, oldV, newV) -> filterResidents());
		voterFilter.valueProperty().addListener((obs, oldV, newV) -> filterResidents());
		pwdFilter.valueProperty().addListener((obs, oldV, newV) -> filterResidents());
		clearFiltersBtn.setOnAction(this::clearFilters);

		addResidentBtn.setOnAction(e -> openAddResidentModal());
		// Restrict add button for Viewers
		if (isViewer()) {
		    addResidentBtn.setDisable(true);
		    addResidentBtn.setTooltip(new Tooltip("Viewers cannot add residents."));
		}
	    }

    @Override
    protected void navigateToPopulation() {
        System.out.println("Already on Population");
    }

	private void loadResidents() {
		masterList.setAll(residentDAO.getAllResidents());
		residentTable.setItems(masterList);
	}

	private void filterResidents() {
		String search = searchField.getText() == null ? "" : searchField.getText().toLowerCase();
		String gender = genderFilter.getValue();
		String minAgeStr = minAgeField.getText();
		String maxAgeStr = maxAgeField.getText();
		String zone = zoneFilter.getValue();
		String civilStatus = civilStatusFilter.getValue();
		String voter = voterFilter.getValue();
		String pwd = pwdFilter.getValue();

		List<Resident> filtered = masterList.stream().filter(r -> {
			boolean matches = true;
			if (!search.isEmpty() && !r.getName().toLowerCase().contains(search)) matches = false;
			if (gender != null && !gender.equals("All") && !r.getGender().equalsIgnoreCase(gender)) matches = false;
			if (minAgeStr != null && !minAgeStr.isEmpty()) {
				try { if (r.getAge() < Integer.parseInt(minAgeStr)) matches = false; } catch (Exception ignored) {}
			}
			if (maxAgeStr != null && !maxAgeStr.isEmpty()) {
				try { if (r.getAge() > Integer.parseInt(maxAgeStr)) matches = false; } catch (Exception ignored) {}
			}
			if (zone != null && !zone.equals("All") && !r.getHousehold().contains(zone)) matches = false;
			if (civilStatus != null && !civilStatus.equals("All") && !r.getCivilStatus().equalsIgnoreCase(civilStatus)) matches = false;
			if (voter != null && !voter.equals("All")) {
				if (voter.equals("Yes") && !r.isRegisteredVoter()) matches = false;
				if (voter.equals("No") && r.isRegisteredVoter()) matches = false;
			}
			if (pwd != null && !pwd.equals("All")) {
				if (pwd.equals("Yes") && !r.isPwd()) matches = false;
				if (pwd.equals("No") && r.isPwd()) matches = false;
			}
			return matches;
		}).collect(Collectors.toList());
		residentTable.setItems(FXCollections.observableArrayList(filtered));
	}

	private void clearFilters(ActionEvent event) {
		searchField.clear();
		genderFilter.setValue("All");
		minAgeField.clear();
		maxAgeField.clear();
		zoneFilter.setValue("All");
		civilStatusFilter.setValue("All");
		voterFilter.setValue("All");
		pwdFilter.setValue("All");
		filterResidents();
	}

		private void addActionsColumn() {
			actionsCol.setCellFactory(new Callback<TableColumn<Resident, Void>, TableCell<Resident, Void>>() {
				@Override
				public TableCell<Resident, Void> call(final TableColumn<Resident, Void> param) {
					return new TableCell<Resident, Void>() {
						private final Button editBtn = new Button("Edit");
						private final Button deleteBtn = new Button("Delete");
						{
							editBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;");
							deleteBtn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;");
							editBtn.setOnAction(e -> onEditResident(getTableView().getItems().get(getIndex())));
							deleteBtn.setOnAction(e -> deleteResident(getTableView().getItems().get(getIndex())));
						}
						@Override
						public void updateItem(Void item, boolean empty) {
							super.updateItem(item, empty);
							if (empty) {
								setGraphic(null);
							} else if (isViewer()) {
								editBtn.setDisable(true);
								deleteBtn.setDisable(true);
								editBtn.setTooltip(new Tooltip("Viewers cannot edit residents."));
								deleteBtn.setTooltip(new Tooltip("Viewers cannot delete residents."));
								HBox box = new HBox(10, editBtn, deleteBtn);
								setGraphic(box);
							} else {
								editBtn.setDisable(false);
								deleteBtn.setDisable(false);
								editBtn.setTooltip(null);
								deleteBtn.setTooltip(null);
								HBox box = new HBox(10, editBtn, deleteBtn);
								setGraphic(box);
							}
						}
					};
				}
			});
		}


	@FXML
	private void openAddResidentModal() {
		if (isViewer()) return;
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/views/add_resident.fxml"));
			Parent root = loader.load();
			application.controllers.AddResidentController controller = loader.getController();
			if (this.currentAccount != null) {
				controller.setCurrentAccount(this.currentAccount);
			}
			Stage stage = new Stage();
			stage.setTitle("Add New Resident");
			stage.setScene(new Scene(root));
			stage.setResizable(false);
			stage.showAndWait();
			loadResidents();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Called by the Edit button in the table cell factory
	public void onEditResident(Resident resident) {
		if (isViewer()) return;
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/views/add_resident.fxml"));
			Parent root = loader.load();
			application.controllers.AddResidentController controller = loader.getController();
			controller.setResidentToEdit(resident); // Pre-fill form
			if (this.currentAccount != null) {
				controller.setCurrentAccount(this.currentAccount);
			}
			Stage stage = new Stage();
			stage.setTitle("Edit Resident");
			stage.setScene(new Scene(root));
			stage.setResizable(false);
			stage.showAndWait();
			loadResidents();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void deleteResident(Resident resident) {
		if (isViewer() || resident == null) return;
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.setTitle("Delete Resident");
		alert.setHeaderText(null);
		alert.setContentText("Are you sure you want to delete this resident? This action can be undone by an admin.");
		alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
		alert.showAndWait().ifPresent(type -> {
			if (type == ButtonType.YES) {
				// Log before state
				String before = resident.getName() + ", " + resident.getBirthDate() + ", " + resident.getGender() + ", " + resident.getCivilStatus() + ", " + resident.getHousehold() + ", " + resident.getContact() + ", " + resident.getEducationalAttainment() + ", Voter: " + (resident.isRegisteredVoter() ? "Yes" : "No") + ", PWD: " + (resident.isPwd() ? "Yes" : "No");
				boolean success = residentDAO.deleteResident(resident.getResidentId());
				if (success) {
					if (this.currentAccount != null) {
						String details = "Deleted resident: " + resident.getName();
						AuditTrailDAO.logDelete(this.currentAccount.getId(), "R", resident.getResidentId(), details);
					}
					loadResidents();
				} else {
					Alert error = new Alert(Alert.AlertType.ERROR, "Failed to delete resident.", ButtonType.OK);
					error.showAndWait();
				}
			}
		});
	}

	// Helper for role check
	private boolean isViewer() {
		return this.currentAccount != null && ("Viewer".equalsIgnoreCase(this.currentAccount.getRole()) || "V".equalsIgnoreCase(this.currentAccount.getRole()));
	}
}
