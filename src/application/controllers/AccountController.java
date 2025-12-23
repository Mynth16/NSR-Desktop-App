package application.controllers;

import application.models.Account;
import application.database.AccountDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;

public class AccountController extends NavigationBaseController {
	@FXML
	private Label sidebarNameLabel;
	@FXML
	private Label sidebarRoleLabel;
	@FXML
	private Label sidebarUsernameLabel;

	@FXML private TableView<Account> accountTable;
	@FXML private TableColumn<Account, String> usernameCol;
	@FXML private TableColumn<Account, String> roleCol;
	@FXML private TableColumn<Account, String> createdCol;
	@FXML private TableColumn<Account, Void> actionsCol;
	@FXML private Button addAccountBtn;

	private ObservableList<Account> masterList = FXCollections.observableArrayList();
	private AccountDAO accountDAO = new AccountDAO();

	// Use the currentAccount from NavigationBaseController
	private String getCurrentUserRole() {
		if (this.currentAccount != null && this.currentAccount.getRole() != null) {
			String role = this.currentAccount.getRole();
			if (role.equalsIgnoreCase("A") || role.equalsIgnoreCase("Admin")) return "Admin";
			if (role.equalsIgnoreCase("S") || role.equalsIgnoreCase("Staff")) return "Staff";
			if (role.equalsIgnoreCase("V") || role.equalsIgnoreCase("Viewer")) return "Viewer";
			return role;
		}
		return "Viewer";
	}

	private boolean isAdmin() {
		return "Admin".equals(getCurrentUserRole());
	}

	private boolean isStaff() {
		return "Staff".equals(getCurrentUserRole());
	}

	private boolean isViewer() {
		return "Viewer".equals(getCurrentUserRole());
	}

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
		usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
		roleCol.setCellValueFactory(cellData -> {
			String role = cellData.getValue().getRole();
			String display;
			if (role == null) display = "Viewer";
			else if (role.equalsIgnoreCase("A") || role.equalsIgnoreCase("Admin")) display = "Admin";
			else if (role.equalsIgnoreCase("S") || role.equalsIgnoreCase("Staff")) display = "Staff";
			else if (role.equalsIgnoreCase("V") || role.equalsIgnoreCase("Viewer")) display = "Viewer";
			else display = role;
			return new javafx.beans.property.SimpleStringProperty(display);
		});
		createdCol.setCellValueFactory(new PropertyValueFactory<>("created"));
		addActionsColumn();
		loadAccounts();
		addAccountBtn.setOnAction(e -> openAddAccountModal());
		// Only Admin can add accounts
		addAccountBtn.setDisable(!isAdmin());
		if (isStaff() || isViewer()) {
			addAccountBtn.setTooltip(new Tooltip("Only Admins can add accounts."));
		}
	}

	private void loadAccounts() {
		masterList.setAll(accountDAO.getAllAccounts());
		accountTable.setItems(masterList);
	}

	private void addActionsColumn() {
		actionsCol.setCellFactory(new Callback<TableColumn<Account, Void>, TableCell<Account, Void>>() {
			@Override
			public TableCell<Account, Void> call(final TableColumn<Account, Void> param) {
				return new TableCell<Account, Void>() {
					   private final Button editBtn = new Button("Edit");
					   private final Button deleteBtn = new Button("Delete");
					   {
						   editBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;");
						   deleteBtn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;");
						   editBtn.setOnAction(e -> onEditAccount(getTableView().getItems().get(getIndex())));
						   deleteBtn.setOnAction(e -> deleteAccount(getTableView().getItems().get(getIndex())));
					   }
					@Override
					public void updateItem(Void item, boolean empty) {
						super.updateItem(item, empty);
						if (empty) {
							setGraphic(null);
						} else if (isAdmin()) {
							HBox box = new HBox(10, editBtn, deleteBtn);
							setGraphic(box);
						} else if (isStaff() || isViewer()) {
							// Show disabled buttons with tooltip for Staff/Viewer
							editBtn.setDisable(true);
							deleteBtn.setDisable(true);
							editBtn.setTooltip(new Tooltip("Only Admins can edit accounts."));
							deleteBtn.setTooltip(new Tooltip("Only Admins can delete accounts."));
							HBox box = new HBox(10, editBtn, deleteBtn);
							setGraphic(box);
						} else {
							setGraphic(null);
						}
					}
				};
			}
		});
	}

	private void openAddAccountModal() {
		if (!isAdmin()) return;
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/views/add_account.fxml"));
			Parent root = loader.load();
			// Pass currentAccount to AddAccountController
			Object controller = loader.getController();
			if (controller instanceof application.controllers.AddAccountController && this.currentAccount != null) {
				((application.controllers.AddAccountController) controller).setAccount(this.currentAccount);
			}
			Stage stage = new Stage();
			stage.setTitle("Add New Account");
			stage.setScene(new Scene(root));
			stage.setResizable(false);
			stage.showAndWait();
			// After closing, reload accounts
			loadAccounts();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public void onEditAccount(Account account) {
		if (!isAdmin() || account == null) return;
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/views/edit_account.fxml"));
			Parent root = loader.load();
			application.controllers.EditAccountController controller = loader.getController();
			String id = accountDAO.getAccountIdByUsername(account.getUsername());
			controller.setAccount(account, id);
			Stage stage = new Stage();
			stage.setTitle("Edit Account");
			stage.setScene(new Scene(root));
			stage.setResizable(false);
			stage.showAndWait();
			loadAccounts();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void deleteAccount(Account account) {
		if (!isAdmin() || account == null) return;
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.setTitle("Delete Account");
		alert.setHeaderText(null);
		alert.setContentText("Are you sure you want to delete this account?");
		alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
		alert.showAndWait().ifPresent(type -> {
			if (type == ButtonType.YES) {
				String id = accountDAO.getAccountIdByUsername(account.getUsername());
				String currentUserRole = (this.currentAccount != null && this.currentAccount.getRole() != null) ? this.currentAccount.getRole() : "Viewer";
				boolean success = accountDAO.deleteAccount(id, currentUserRole);
				if (success) {
					// Audit log
					if (this.currentAccount != null) {
						String userId = this.currentAccount.getId();
						application.database.AuditTrailDAO.logDelete(userId, "A", id, "Deleted account: " + account.getUsername());
					}
					loadAccounts();
				} else {
					Alert error = new Alert(Alert.AlertType.ERROR, "Failed to delete account. You may lack permission.", ButtonType.OK);
					error.showAndWait();
				}
			}
		});
	}
}
