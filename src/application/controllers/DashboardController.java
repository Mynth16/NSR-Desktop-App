package application.controllers;

import application.database.HouseholdDAO;
import application.database.ResidentDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import java.util.Map;

public class DashboardController extends NavigationBaseController {
    @FXML private javafx.scene.layout.VBox recentActivityVBox;
    @FXML private Label noRecentActivityLabel;
    @FXML
    private Label sidebarNameLabel;
    @FXML
    private Label sidebarRoleLabel;
    @FXML
    private Label sidebarUsernameLabel;

    // --- Dashboard Specific Labels ---
    @FXML private Label totalPopulationLabel;
    @FXML private Label totalHouseholdsLabel;
    @FXML private Label maleCountLabel;
    @FXML private Label femaleCountLabel;
    @FXML private Label voterCountLabel;
    @FXML private Label pwdCountLabel;

    @FXML private Label zone1HouseholdsLabel;
    @FXML private Label zone1PeopleLabel;
    @FXML private Label zone2HouseholdsLabel;
    @FXML private Label zone2PeopleLabel;
    @FXML private Label zone3HouseholdsLabel;
    @FXML private Label zone3PeopleLabel;
    @FXML private Label zone4HouseholdsLabel;
    @FXML private Label zone4PeopleLabel;
    @FXML private Label zone5HouseholdsLabel;
    @FXML private Label zone5PeopleLabel;

    @FXML private Label minorCountLabel;
    @FXML private Label youngAdultCountLabel;
    @FXML private Label adultCountLabel;
    @FXML private Label middleAgedCountLabel;
    @FXML private Label seniorCountLabel;

    @FXML private ProgressBar minorProgressBar;
    @FXML private ProgressBar youngAdultProgressBar;
    @FXML private ProgressBar adultProgressBar;
    @FXML private ProgressBar middleAgedProgressBar;
    @FXML private ProgressBar seniorProgressBar;

    private ResidentDAO residentDAO = new ResidentDAO();
    private HouseholdDAO householdDAO = new HouseholdDAO();

    @FXML
    private void initialize() {
        // 1. Setup Navigation & Sidebar (Inherited)
        bindNavigationHandlers();
        // 2. Load Data (Specific to Dashboard)
        loadDashboardStats();
        // 3. Load Recent Audit Trail Entries
        loadRecentAuditTrail();
    }

    private void loadRecentAuditTrail() {
        recentActivityVBox.getChildren().clear();
        java.util.List<application.models.AuditTrail> recentEntries = application.database.AuditTrailDAO.getRecentAuditTrails(3);
        if (recentEntries == null || recentEntries.isEmpty()) {
            noRecentActivityLabel.setVisible(true);
        } else {
            noRecentActivityLabel.setVisible(false);
            for (application.models.AuditTrail entry : recentEntries) {
                javafx.scene.layout.HBox hbox = new javafx.scene.layout.HBox(10);
                hbox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                javafx.scene.shape.Circle circle = new javafx.scene.shape.Circle(6);
                circle.setFill(javafx.scene.paint.Color.web("#28a745"));
                javafx.scene.control.Label actionLabel = new javafx.scene.control.Label(entry.getAction());
                actionLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #333; -fx-font-size: 13px;");
                javafx.scene.control.Label detailsLabel = new javafx.scene.control.Label(entry.getDetails());
                detailsLabel.setStyle("-fx-text-fill: #444; -fx-font-size: 12px; -fx-font-style: italic;");
                javafx.scene.control.Label userLabel = new javafx.scene.control.Label("by " + entry.getUser());
                userLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");
                javafx.scene.control.Label timeLabel = new javafx.scene.control.Label(entry.getDateTime().toString());
                timeLabel.setStyle("-fx-text-fill: #999; -fx-font-size: 11px;");
                hbox.getChildren().addAll(circle, actionLabel, detailsLabel, userLabel, timeLabel);
                recentActivityVBox.getChildren().add(hbox);
            }
        }
    }


    @Override
    public void setAccount(application.models.Account account) {
        super.setAccount(account);
        // Set sidebar username and role when account is set
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
    }

    @Override
    protected void navigateToDashboard() {
        System.out.println("Already on Dashboard");
    }

    private void loadDashboardStats() {
        // Population and households
        int totalPopulation = residentDAO.getTotalPopulation();
        int totalHouseholds = householdDAO.getTotalHouseholds();
        int maleCount = residentDAO.getMaleCount();
        int femaleCount = residentDAO.getFemaleCount();
        int voterCount = residentDAO.getVoterCount();
        int pwdCount = residentDAO.getPWDCount();

        totalPopulationLabel.setText(String.valueOf(totalPopulation));
        totalHouseholdsLabel.setText(String.valueOf(totalHouseholds));
        maleCountLabel.setText(String.valueOf(maleCount));
        femaleCountLabel.setText(String.valueOf(femaleCount));
        voterCountLabel.setText(String.valueOf(voterCount));
        pwdCountLabel.setText(String.valueOf(pwdCount));

        // Zone breakdown
        Map<String, Integer> zonePeople = residentDAO.getPopulationByZone();
        Map<String, Integer> zoneHouseholds = householdDAO.getHouseholdsByZone();
        zone1HouseholdsLabel.setText(zoneHouseholds.getOrDefault("1", 0) + " households");
        zone1PeopleLabel.setText(String.valueOf(zonePeople.getOrDefault("1", 0)));
        zone2HouseholdsLabel.setText(zoneHouseholds.getOrDefault("2", 0) + " households");
        zone2PeopleLabel.setText(String.valueOf(zonePeople.getOrDefault("2", 0)));
        zone3HouseholdsLabel.setText(zoneHouseholds.getOrDefault("3", 0) + " households");
        zone3PeopleLabel.setText(String.valueOf(zonePeople.getOrDefault("3", 0)));
        zone4HouseholdsLabel.setText(zoneHouseholds.getOrDefault("4", 0) + " households");
        zone4PeopleLabel.setText(String.valueOf(zonePeople.getOrDefault("4", 0)));
        zone5HouseholdsLabel.setText(zoneHouseholds.getOrDefault("5", 0) + " households");
        zone5PeopleLabel.setText(String.valueOf(zonePeople.getOrDefault("5", 0)));

        // Age distribution
        Map<String, Integer> ageDist = residentDAO.getAgeDistribution();
        int minor = ageDist.getOrDefault("Minor", 0);
        int youngAdult = ageDist.getOrDefault("Young Adult", 0);
        int adult = ageDist.getOrDefault("Adult", 0);
        int middleAged = ageDist.getOrDefault("Middle-aged", 0);
        int senior = ageDist.getOrDefault("Senior", 0);

        int total = minor + youngAdult + adult + middleAged + senior;
        minorCountLabel.setText(String.valueOf(minor));
        youngAdultCountLabel.setText(String.valueOf(youngAdult));
        adultCountLabel.setText(String.valueOf(adult));
        middleAgedCountLabel.setText(String.valueOf(middleAged));
        seniorCountLabel.setText(String.valueOf(senior));

        minorProgressBar.setProgress(total > 0 ? (double) minor / total : 0);
        youngAdultProgressBar.setProgress(total > 0 ? (double) youngAdult / total : 0);
        adultProgressBar.setProgress(total > 0 ? (double) adult / total : 0);
        middleAgedProgressBar.setProgress(total > 0 ? (double) middleAged / total : 0);
        seniorProgressBar.setProgress(total > 0 ? (double) senior / total : 0);
    }
}