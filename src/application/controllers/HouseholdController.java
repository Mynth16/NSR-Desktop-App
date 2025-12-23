package application.controllers;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import application.models.Household;
import application.database.HouseholdDAO;
import javafx.scene.layout.HBox;

public class HouseholdController extends NavigationBaseController {
	@FXML
	private Label sidebarNameLabel;
	@FXML
	private Label sidebarRoleLabel;
	@FXML
	private Label sidebarUsernameLabel;
	@FXML
	private TextField searchField;
	@FXML
	private Button addHouseholdBtn;
	@FXML
	private TableView<Household> householdTable;
	@FXML
	private TableColumn<Household, String> zoneCol;
	@FXML
	private TableColumn<Household, String> houseNumberCol;
	@FXML
	private TableColumn<Household, String> headCol;
	@FXML
	private TableColumn<Household, String> residentsCol;
	@FXML
	private TableColumn<Household, String> statusCol;
	@FXML
	private TableColumn<Household, Void> actionsCol;

    private Household selectedHousehold;

	private ObservableList<Household> masterData = FXCollections.observableArrayList();
	private FilteredList<Household> filteredData;

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
		HouseholdDAO dao = new HouseholdDAO();
		masterData.setAll(dao.getAllHouseholds());
		filteredData = new FilteredList<>(masterData, hh -> {
			// Only show households with status 'A' or 'Active'
			String status = hh.getStatus();
			return "A".equalsIgnoreCase(status) || "Active".equalsIgnoreCase(status);
		});

		searchField.textProperty().addListener((obs, oldVal, newVal) -> {
			String lower = newVal == null ? "" : newVal.toLowerCase();
			filteredData.setPredicate(hh -> {
				// Only show active
				String status = hh.getStatus();
				boolean isActive = "A".equalsIgnoreCase(status) || "Active".equalsIgnoreCase(status);
				return isActive && (
					hh.getZone().toLowerCase().contains(lower) ||
					hh.getHouseNumber().toLowerCase().contains(lower)
				);
			});
		});
		householdTable.setItems(filteredData);

		zoneCol.setCellValueFactory(data -> Bindings.createStringBinding(data.getValue()::getZone));
		houseNumberCol.setCellValueFactory(data -> Bindings.createStringBinding(data.getValue()::getHouseNumber));
		headCol.setCellValueFactory(data -> Bindings.createStringBinding(data.getValue()::getHeadOfHousehold));
		residentsCol.setCellValueFactory(data -> Bindings.createStringBinding(() -> data.getValue().getResidentsCount() + " residents"));
		statusCol.setCellValueFactory(data -> Bindings.createStringBinding(data.getValue()::getStatus));

		// Restrict add/edit for Viewers
		if (isViewer()) {
			if (addHouseholdBtn != null) {
				addHouseholdBtn.setDisable(true);
				addHouseholdBtn.setTooltip(new Tooltip("Viewers cannot add households."));
			}
		}

		// Add actions column with edit/delete buttons and restrict for Viewers
		actionsCol.setCellFactory(col -> new TableCell<>() {
			private final Button editBtn = new Button("Edit");
			private final Button deleteBtn = new Button("Delete");
			{
				editBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;");
				deleteBtn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;");
				editBtn.setOnAction(e -> {
					Household hh = getTableView().getItems().get(getIndex());
					openEditHouseholdModal(hh);
				});
				deleteBtn.setOnAction(e -> {
					Household hh = getTableView().getItems().get(getIndex());
					confirmAndDeleteHousehold(hh);
				});
			}
			@Override
			protected void updateItem(Void item, boolean empty) {
				super.updateItem(item, empty);
				if (empty) {
					setGraphic(null);
				} else {
					if (isViewer()) {
						editBtn.setDisable(true);
						deleteBtn.setDisable(true);
						editBtn.setTooltip(new Tooltip("Viewers cannot edit households."));
						deleteBtn.setTooltip(new Tooltip("Viewers cannot delete households."));
					} else {
						editBtn.setDisable(false);
						deleteBtn.setDisable(false);
						editBtn.setTooltip(null);
						deleteBtn.setTooltip(null);
					}
					HBox box = new HBox(10, editBtn, deleteBtn);
					setGraphic(box);
				}
			}
		});
	}

	@Override
	protected void navigateToHouseholds() {
		System.out.println("Already on Households");
	}

	@FXML
	private void openAddHouseholdModal() {
		try {
			javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/application/views/add_household.fxml"));
			javafx.scene.Parent root = loader.load();
			application.controllers.AddHouseholdController controller = loader.getController();
			// Pass currentAccount to AddHouseholdController
			if (this.currentAccount != null) {
				controller.setAccount(this.currentAccount);
			}
			javafx.stage.Stage stage = new javafx.stage.Stage();
			stage.setTitle("Add New Household");
			stage.setScene(new javafx.scene.Scene(root));
			stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
			stage.showAndWait();
			if (controller.isHouseholdAdded()) {
				// Refresh table
				HouseholdDAO dao = new HouseholdDAO();
				masterData.setAll(dao.getAllHouseholds());
			}
		} catch (Exception e) {
			e.printStackTrace();
			javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
			alert.setTitle("Error");
			alert.setHeaderText(null);
			alert.setContentText("Failed to open Add Household form.");
			alert.showAndWait();
		}
	}

	// Open edit modal for household
	private void openEditHouseholdModal(Household household) {
		try {
			javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/application/views/add_household.fxml"));
			javafx.scene.Parent root = loader.load();
			application.controllers.AddHouseholdController controller = loader.getController();
			// Pass currentAccount to AddHouseholdController
			if (this.currentAccount != null) {
				controller.setAccount(this.currentAccount);
			}
			controller.setEditMode(household); // You must implement setEditMode in AddHouseholdController
			javafx.stage.Stage stage = new javafx.stage.Stage();
			stage.setTitle("Edit Household");
			stage.setScene(new javafx.scene.Scene(root));
			stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
			stage.showAndWait();
			if (controller.isHouseholdUpdated()) {
				HouseholdDAO dao = new HouseholdDAO();
				masterData.setAll(dao.getAllHouseholds());
			}
		} catch (Exception e) {
			e.printStackTrace();
			javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
			alert.setTitle("Error");
			alert.setHeaderText(null);
			alert.setContentText("Failed to open Edit Household form.");
			alert.showAndWait();
		}
	}

	// Confirm and soft-delete household
	private void confirmAndDeleteHousehold(Household household) {
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.setTitle("Delete Household");
		alert.setHeaderText("Are you sure you want to delete this household?");
		alert.setContentText("This will archive the household and it will not be shown in the table.");
		alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
		alert.showAndWait().ifPresent(type -> {
			if (type == ButtonType.YES) {
				HouseholdDAO dao = new HouseholdDAO();
				if (dao.softDeleteHousehold(household)) {
					masterData.setAll(dao.getAllHouseholds());
				} else {
					Alert err = new Alert(Alert.AlertType.ERROR, "Failed to delete household.");
					err.showAndWait();
				}
			}
		});
	}

	// Helper for role check
	private boolean isViewer() {
		return this.currentAccount != null && ("Viewer".equalsIgnoreCase(this.currentAccount.getRole()) || "V".equalsIgnoreCase(this.currentAccount.getRole()));
	}

}


