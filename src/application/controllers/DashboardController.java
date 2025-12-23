package application.controllers;

import application.database.HouseholdDAO;
import application.database.ResidentDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import java.util.Map;

// EXTEND the base controller
public class DashboardController extends NavigationBaseController {

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
    }

    @Override
    protected void navigateToDashboard() {
        // We are already here, so do nothing (prevents reload)
        System.out.println("Already on Dashboard");
    }

    // For login integration: allow passing account info


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