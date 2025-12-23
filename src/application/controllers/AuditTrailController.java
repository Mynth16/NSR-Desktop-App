package application.controllers;

import application.database.AuditTrailDAO;
import application.models.AuditTrail;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class AuditTrailController extends NavigationBaseController {
	@FXML private ComboBox<String> recordTypeCombo;
	@FXML private ComboBox<String> changeTypeCombo;
	@FXML private TableView<AuditTrail> auditTrailTable;
	@FXML private TableColumn<AuditTrail, String> dateTimeCol;
	@FXML private TableColumn<AuditTrail, String> userCol;
	@FXML private TableColumn<AuditTrail, String> typeCol;
	@FXML private TableColumn<AuditTrail, String> actionCol;
	@FXML private TableColumn<AuditTrail, String> detailsCol;

	@FXML private javafx.scene.control.Label sidebarNameLabel;
	@FXML private javafx.scene.control.Label sidebarRoleLabel;
	@FXML private javafx.scene.control.Label sidebarUsernameLabel;

	private ObservableList<AuditTrail> masterData;

	@FXML
	public void initialize() {
		bindNavigationHandlers();
		// Setup columns
		dateTimeCol.setCellValueFactory(cellData -> javafx.beans.binding.Bindings.createStringBinding(
				() -> cellData.getValue().getDateTime().format(DateTimeFormatter.ofPattern("MMM dd, yyyy, hh:mm a"))));
		userCol.setCellValueFactory(new PropertyValueFactory<>("user"));
		typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
		actionCol.setCellValueFactory(new PropertyValueFactory<>("action"));
		detailsCol.setCellValueFactory(new PropertyValueFactory<>("details"));

		// Load data
		List<AuditTrail> all = AuditTrailDAO.getAllAuditTrails();
		masterData = FXCollections.observableArrayList(all);
		auditTrailTable.setItems(masterData);

		// Populate filter combos
		recordTypeCombo.getItems().add("All Types");
		recordTypeCombo.getItems().addAll(masterData.stream().map(AuditTrail::getType).distinct().collect(Collectors.toList()));
		recordTypeCombo.getSelectionModel().selectFirst();

		changeTypeCombo.getItems().add("All Changes");
		changeTypeCombo.getItems().addAll(masterData.stream().map(AuditTrail::getAction).distinct().collect(Collectors.toList()));
		changeTypeCombo.getSelectionModel().selectFirst();

		// Listeners for filtering
		recordTypeCombo.setOnAction(e -> filterTable());
		changeTypeCombo.setOnAction(e -> filterTable());

		// Set sidebar labels if account is already set
		updateSidebarLabels();
	}

	@Override
	public void setAccount(application.models.Account account) {
		super.setAccount(account);
		updateSidebarLabels();
	}

	private void updateSidebarLabels() {
		if (currentAccount != null) {
			if (sidebarNameLabel != null) sidebarNameLabel.setText(currentAccount.getUsername());
			if (sidebarRoleLabel != null) sidebarRoleLabel.setText(getRoleFullName(currentAccount.getRole()));
			if (sidebarUsernameLabel != null) sidebarUsernameLabel.setText(currentAccount.getUsername());
		}
	}

	private String getRoleFullName(String code) {
		if (code == null) return "";
		switch (code) {
			case "A": return "Admin";
			case "S": return "Staff";
			case "V": return "Viewer";
			default: return code;
		}
	}

	@Override
	protected void navigateToAuditTrail() {
		System.out.println("Already on Audit Trail");
	}

	private void filterTable() {
		String selectedType = recordTypeCombo.getSelectionModel().getSelectedItem();
		String selectedAction = changeTypeCombo.getSelectionModel().getSelectedItem();
		auditTrailTable.setItems(masterData.filtered(row ->
			(selectedType.equals("All Types") || row.getType().equals(selectedType)) &&
			(selectedAction.equals("All Changes") || row.getAction().equals(selectedAction))
		));
	}
}
