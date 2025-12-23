package application.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import java.util.ArrayList;
import java.util.List;
import application.models.Household;

public class HouseholdDAO {
	public int getTotalHouseholds() {
		String sql = "SELECT COUNT(*) FROM households";
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

	public boolean softDeleteHousehold(Household household) {
		String sql = "UPDATE households SET status = 'X' WHERE zone_num = ? AND house_num = ?";
		try (Connection conn = DBConnection.getConnection();
			 PreparedStatement stmt = conn.prepareStatement(sql)) {
			String zone = household.getZone();
			if (zone.toLowerCase().startsWith("zone ")) {
				zone = zone.substring(5).trim();
			}
			stmt.setString(1, zone);
			stmt.setString(2, household.getHouseNumber());
			int affected = stmt.executeUpdate();
			return affected > 0;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean updateHousehold(Household oldHousehold, Household newHousehold) {
		String sql = "UPDATE households SET zone_num = ?, house_num = ?, status = ? WHERE zone_num = ? AND house_num = ?";
		try (Connection conn = DBConnection.getConnection();
			 PreparedStatement stmt = conn.prepareStatement(sql)) {
			String newZone = newHousehold.getZone();
			if (newZone.toLowerCase().startsWith("zone ")) {
				newZone = newZone.substring(5).trim();
			}
			String oldZone = oldHousehold.getZone();
			if (oldZone.toLowerCase().startsWith("zone ")) {
				oldZone = oldZone.substring(5).trim();
			}
			System.out.println("[DEBUG] updateHousehold: newZone=" + newZone + ", newHouse=" + newHousehold.getHouseNumber() + ", oldZone=" + oldZone + ", oldHouse=" + oldHousehold.getHouseNumber());
			stmt.setString(1, newZone);
			stmt.setString(2, newHousehold.getHouseNumber());
			stmt.setString(3, "Active".equalsIgnoreCase(newHousehold.getStatus()) ? "A" : "X");
			stmt.setString(4, oldZone);
			stmt.setString(5, oldHousehold.getHouseNumber());
			int affected = stmt.executeUpdate();
			System.out.println("[DEBUG] Rows affected: " + affected);
			return affected > 0;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public Map<String, Integer> getHouseholdsByZone() {
		Map<String, Integer> zoneMap = new HashMap<>();
		String sql = "SELECT zone_num, COUNT(*) as count FROM households GROUP BY zone_num";
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

	public List<Household> getAllHouseholds() {
		List<Household> list = new ArrayList<>();
		String sql = "SELECT h.zone_num, h.house_num, " +
				"CONCAT(r.first_name, ' ', r.last_name) AS head_name, " +
				"(SELECT COUNT(*) FROM residents r2 WHERE r2.household_id = h.household_id AND r2.status <> 'X') as residents_count, " +
				"CASE WHEN h.status = 'A' THEN 'Active' ELSE 'Inactive' END as status " +
				"FROM households h " +
				"LEFT JOIN residents r ON h.head_resident_id = r.resident_id";
		try (Connection conn = DBConnection.getConnection();
			 PreparedStatement stmt = conn.prepareStatement(sql);
			 ResultSet rs = stmt.executeQuery()) {
			while (rs.next()) {
				list.add(new Household(
						"Zone " + rs.getString("zone_num"),
						rs.getString("house_num"),
						rs.getString("head_name") != null ? rs.getString("head_name") : "-",
						rs.getInt("residents_count"),
						rs.getString("status")
				));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}

	public List<HouseholdOption> getHouseholdOptions() {
		List<HouseholdOption> list = new ArrayList<>();
		String sql = "SELECT household_id, zone_num, house_num FROM households ORDER BY zone_num, house_num";
		try (Connection conn = DBConnection.getConnection();
			 PreparedStatement stmt = conn.prepareStatement(sql);
			 ResultSet rs = stmt.executeQuery()) {
			while (rs.next()) {
				String display = "Zone " + rs.getString("zone_num") + " - " + rs.getString("house_num");
				String id = rs.getString("household_id");
				list.add(new HouseholdOption(id, display));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}

	public static class HouseholdOption {
		private final String id;
		private final String display;
		public HouseholdOption(String id, String display) {
			this.id = id;
			this.display = display;
		}
		public String getId() { return id; }
		public String getDisplay() { return display; }
		@Override
		public String toString() { return display; }
	}

	public boolean addHousehold(Household household) {
		String sql = "INSERT INTO households (zone_num, house_num, status) VALUES (?, ?, 'A')";
		try (Connection conn = DBConnection.getConnection();
			 PreparedStatement stmt = conn.prepareStatement(sql)) {
			// Remove 'Zone ' prefix if present
			String zone = household.getZone();
			if (zone.toLowerCase().startsWith("zone ")) {
				zone = zone.substring(5).trim();
			}
			stmt.setString(1, zone);
			stmt.setString(2, household.getHouseNumber());
			int affected = stmt.executeUpdate();
			return affected > 0;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	// Update the head resident for a household
	public boolean updateHeadResident(String householdId, String residentId) {
		String sql = "UPDATE households SET head_resident_id = ? WHERE household_id = ?";
		try (Connection conn = DBConnection.getConnection();
			 PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, residentId);
			stmt.setString(2, householdId);
			int affected = stmt.executeUpdate();
			return affected > 0;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
}
