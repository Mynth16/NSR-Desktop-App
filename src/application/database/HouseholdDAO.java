package application.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

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
}
