package application.controllers;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import application.models.Household;
import application.database.HouseholdDAO;

public class HouseholdController extends NavigationBaseController {
	@FXML
	private TextField searchField;
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

	private ObservableList<Household> masterData = FXCollections.observableArrayList();
	private FilteredList<Household> filteredData;

	@FXML
	public void initialize() {
		bindNavigationHandlers();
		HouseholdDAO dao = new HouseholdDAO();
		masterData.setAll(dao.getAllHouseholds());
		filteredData = new FilteredList<>(masterData, p -> true);

		searchField.textProperty().addListener((obs, oldVal, newVal) -> {
			String lower = newVal == null ? "" : newVal.toLowerCase();
			filteredData.setPredicate(hh ->
				hh.getZone().toLowerCase().contains(lower) ||
				hh.getHouseNumber().toLowerCase().contains(lower)
			);
		});
		householdTable.setItems(filteredData);

		zoneCol.setCellValueFactory(data -> Bindings.createStringBinding(data.getValue()::getZone));
		houseNumberCol.setCellValueFactory(data -> Bindings.createStringBinding(data.getValue()::getHouseNumber));
		headCol.setCellValueFactory(data -> Bindings.createStringBinding(data.getValue()::getHeadOfHousehold));
		residentsCol.setCellValueFactory(data -> Bindings.createStringBinding(() -> data.getValue().getResidentsCount() + " residents"));
		statusCol.setCellValueFactory(data -> Bindings.createStringBinding(data.getValue()::getStatus));

		// Restrict add/edit for Viewers
		if (isViewer()) {
			// If you have an addHouseholdBtn, disable it here
			// addHouseholdBtn.setDisable(true);
			// addHouseholdBtn.setTooltip(new Tooltip("Viewers cannot add households."));
		}
		// TODO: Add actions column with edit/delete buttons and restrict for Viewers
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

	// Helper for role check
	private boolean isViewer() {
		return this.currentAccount != null && ("Viewer".equalsIgnoreCase(this.currentAccount.getRole()) || "V".equalsIgnoreCase(this.currentAccount.getRole()));
	}
}


