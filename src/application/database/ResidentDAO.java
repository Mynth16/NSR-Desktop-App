package application.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import application.models.Resident;
import java.util.ArrayList;
import java.util.List;

public class ResidentDAO {
	public String addResident(String firstName, String lastName, String birthDate, String gender, String civilStatus, String householdId, String educationalAttainment, String contact, String email, boolean registeredVoter, boolean pwd) {
		String sql = "INSERT INTO residents (first_name, last_name, birth_date, gender, civil_status, household_id, educational_attainment, contact_number, email, registered_voter, pwd, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'A')";
		try (Connection conn = DBConnection.getConnection();
			 PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
			stmt.setString(1, firstName);
			stmt.setString(2, lastName);
			stmt.setString(3, birthDate);
			stmt.setString(4, gender.equalsIgnoreCase("Male") ? "M" : gender.equalsIgnoreCase("Female") ? "F" : "O");
			stmt.setString(5, civilStatus.substring(0, 1).toUpperCase());
			stmt.setString(6, householdId);
			stmt.setString(7, educationalAttainment);
			stmt.setString(8, contact);
			stmt.setString(9, email);
			stmt.setString(10, registeredVoter ? "Y" : "N");
			stmt.setString(11, pwd ? "Y" : "N");
			int rows = stmt.executeUpdate();
			if (rows > 0) {
				try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						return generatedKeys.getString(1);
					}
				}
			}
			// fallback: try to fetch by unique fields if no generated key
			String fetchIdSql = "SELECT resident_id FROM residents WHERE first_name=? AND last_name=? AND birth_date=? ORDER BY resident_id DESC LIMIT 1";
			try (PreparedStatement fetchStmt = conn.prepareStatement(fetchIdSql)) {
				fetchStmt.setString(1, firstName);
				fetchStmt.setString(2, lastName);
				fetchStmt.setString(3, birthDate);
				try (ResultSet rs = fetchStmt.executeQuery()) {
					if (rs.next()) {
						return rs.getString("resident_id");
					}
				}
			}
			return null;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	public boolean updateResident(String residentId, String firstName, String lastName, String birthDate, String gender, String civilStatus, String householdId, String educationalAttainment, String contact, String email, boolean registeredVoter, boolean pwd) {
		String sql = "UPDATE residents SET first_name=?, last_name=?, birth_date=?, gender=?, civil_status=?, household_id=?, educational_attainment=?, contact_number=?, email=?, registered_voter=?, pwd=? WHERE resident_id=?";
		try (Connection conn = DBConnection.getConnection();
			 PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, firstName);
			stmt.setString(2, lastName);
			stmt.setString(3, birthDate);
			stmt.setString(4, gender.equalsIgnoreCase("Male") ? "M" : gender.equalsIgnoreCase("Female") ? "F" : "O");
			stmt.setString(5, civilStatus.substring(0, 1).toUpperCase());
			stmt.setString(6, householdId);
			stmt.setString(7, educationalAttainment);
			stmt.setString(8, contact);
			stmt.setString(9, email);
			stmt.setString(10, registeredVoter ? "Y" : "N");
			stmt.setString(11, pwd ? "Y" : "N");
			stmt.setString(12, residentId);
			int rows = stmt.executeUpdate();
			return rows > 0;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean deleteResident(String residentId) {
		String sql = "UPDATE residents SET status = 'X' WHERE resident_id = ?";
		try (Connection conn = DBConnection.getConnection();
			 PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, residentId);
			int rows = stmt.executeUpdate();
			return rows > 0;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public List<Resident> getAllResidents() {
		List<Resident> list = new ArrayList<>();
		String sql = "SELECT r.resident_id, CONCAT(r.first_name, ' ', r.last_name) as name, " +
				"FLOOR(DATEDIFF(CURDATE(), r.birth_date) / 365.25) as age, r.gender, r.civil_status, " +
				"CONCAT('Zone ', h.zone_num, ' - ', h.house_num) as household, r.contact_number, " +
				"r.birth_date, r.educational_attainment, r.registered_voter, r.pwd, r.status " +
				"FROM residents r JOIN households h ON r.household_id = h.household_id WHERE r.status <> 'X'";
		try (Connection conn = DBConnection.getConnection();
			 PreparedStatement stmt = conn.prepareStatement(sql);
			 ResultSet rs = stmt.executeQuery()) {
			while (rs.next()) {
				String civilStatusCode = rs.getString("civil_status");
				String civilStatus;
				switch (civilStatusCode) {
					case "S": civilStatus = "Single"; break;
					case "M": civilStatus = "Married"; break;
					case "W": civilStatus = "Widowed"; break;
					case "SEP": civilStatus = "Separated"; break;
					default: civilStatus = civilStatusCode;
				}
				list.add(new Resident(
						rs.getString("resident_id"),
						rs.getString("name"),
						rs.getInt("age"),
						rs.getString("gender").equals("M") ? "Male" : "Female",
						civilStatus,
						rs.getString("household"),
						rs.getString("contact_number"),
						rs.getString("birth_date"),
						rs.getString("educational_attainment"),
						rs.getBoolean("registered_voter"),
						rs.getBoolean("pwd"),
						rs.getString("status")
				));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}

	public int getTotalPopulation() {
		String sql = "SELECT COUNT(*) FROM residents";
		try (Connection conn = DBConnection.getConnection();
			 PreparedStatement stmt = conn.prepareStatement(sql);
			 ResultSet rs = stmt.executeQuery()) {
			if (rs.next()) {
				return rs.getInt(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public int getMaleCount() {
		String sql = "SELECT COUNT(*) FROM residents WHERE gender = 'M'";
		try (Connection conn = DBConnection.getConnection();
			 PreparedStatement stmt = conn.prepareStatement(sql);
			 ResultSet rs = stmt.executeQuery()) {
			if (rs.next()) {
				return rs.getInt(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public int getFemaleCount() {
		String sql = "SELECT COUNT(*) FROM residents WHERE gender = 'F'";
		try (Connection conn = DBConnection.getConnection();
			 PreparedStatement stmt = conn.prepareStatement(sql);
			 ResultSet rs = stmt.executeQuery()) {
			if (rs.next()) {
				return rs.getInt(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}


	public int getVoterCount() {
		String sql = "SELECT COUNT(*) FROM residents WHERE registered_voter = 'Y'";
		try (Connection conn = DBConnection.getConnection();
			 PreparedStatement stmt = conn.prepareStatement(sql);
			 ResultSet rs = stmt.executeQuery()) {
			if (rs.next()) {
				return rs.getInt(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}


	public int getPWDCount() {
		String sql = "SELECT COUNT(*) FROM residents WHERE pwd = 'Y'";
		try (Connection conn = DBConnection.getConnection();
			 PreparedStatement stmt = conn.prepareStatement(sql);
			 ResultSet rs = stmt.executeQuery()) {
			if (rs.next()) {
				return rs.getInt(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public Map<String, Integer> getPopulationByZone() {
		Map<String, Integer> zoneMap = new HashMap<>();
		String sql = "SELECT h.zone_num, COUNT(r.resident_id) as count FROM residents r JOIN households h ON r.household_id = h.household_id GROUP BY h.zone_num";
		try (Connection conn = DBConnection.getConnection();
			 PreparedStatement stmt = conn.prepareStatement(sql);
			 ResultSet rs = stmt.executeQuery()) {
			while (rs.next()) {
				zoneMap.put(rs.getString("zone_num"), rs.getInt("count"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return zoneMap;
	}

	public Map<String, Integer> getAgeDistribution() {
		Map<String, Integer> ageMap = new HashMap<>();
		ageMap.put("Minor", getAgeGroupCount(0, 17));
		ageMap.put("Young Adult", getAgeGroupCount(18, 30));
		ageMap.put("Adult", getAgeGroupCount(31, 45));
		ageMap.put("Middle-aged", getAgeGroupCount(46, 60));
		return ageMap;
	}

	private int getAgeGroupCount(int min, int max) {
		String sql = "SELECT COUNT(*) FROM residents WHERE FLOOR(DATEDIFF(CURDATE(), birth_date) / 365.25) BETWEEN ? AND ?";
		try (Connection conn = DBConnection.getConnection();
			 PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, min);
			stmt.setInt(2, max);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return rs.getInt(1);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	// Get all residents for a specific household
	public List<application.models.Resident> getResidentsByHousehold(String householdId) {
		List<application.models.Resident> list = new ArrayList<>();
		String sql = "SELECT r.resident_id, CONCAT(r.first_name, ' ', r.last_name) as name, " +
				"FLOOR(DATEDIFF(CURDATE(), r.birth_date) / 365.25) as age, r.gender, r.civil_status, " +
				"CONCAT('Zone ', h.zone_num, ' - ', h.house_num) as household, r.contact_number, " +
				"r.birth_date, r.educational_attainment, r.registered_voter, r.pwd, r.status " +
				"FROM residents r JOIN households h ON r.household_id = h.household_id " +
				"WHERE r.status <> 'X' AND r.household_id = ?";
		try (Connection conn = DBConnection.getConnection();
			 PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, householdId);
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					String civilStatusCode = rs.getString("civil_status");
					String civilStatus;
					switch (civilStatusCode) {
						case "S": civilStatus = "Single"; break;
						case "M": civilStatus = "Married"; break;
						case "W": civilStatus = "Widowed"; break;
						case "SEP": civilStatus = "Separated"; break;
						default: civilStatus = civilStatusCode;
					}
					list.add(new application.models.Resident(
							rs.getString("resident_id"),
							rs.getString("name"),
							rs.getInt("age"),
							rs.getString("gender").equals("M") ? "Male" : "Female",
							civilStatus,
							rs.getString("household"),
							rs.getString("contact_number"),
							rs.getString("birth_date"),
							rs.getString("educational_attainment"),
							rs.getBoolean("registered_voter"),
							rs.getBoolean("pwd"),
							rs.getString("status")
					));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}
}
