package application.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class ResidentDAO {
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
}
