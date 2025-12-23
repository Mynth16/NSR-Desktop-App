package application.database;

import application.models.AuditTrail;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


import java.sql.*;

public class AuditTrailDAO {

	    public static List<AuditTrail> getAllAuditTrails() {
		List<AuditTrail> list = new ArrayList<>();
		String sql = "SELECT at.change_date, COALESCE(a.username, 'Unknown') AS username, at.record_type, at.change_type, at.details " +
			"FROM audit_trail at LEFT JOIN account a ON at.acc_id = a.acc_id ORDER BY at.change_date DESC";
		try (Connection conn = DBConnection.getConnection();
		     PreparedStatement stmt = conn.prepareStatement(sql);
		     ResultSet rs = stmt.executeQuery()) {
		    while (rs.next()) {
			LocalDateTime dateTime = rs.getTimestamp("change_date").toLocalDateTime();
			String user = rs.getString("username");
			String type = rs.getString("record_type");
			String action = mapChangeType(rs.getString("change_type"));
			String details = rs.getString("details");
			list.add(new AuditTrail(dateTime, user, type, action, details));
		    }
		} catch (Exception e) {
		    e.printStackTrace();
		}
		return list;
	    }

	public static void logCreate(String userId, String recordType, String recordId, String details) {
		logAction(userId, recordType, recordId, "C", details);
	}

	public static void logUpdate(String userId, String recordType, String recordId, String details) {
		logAction(userId, recordType, recordId, "U", details);
	}

	public static void logDelete(String userId, String recordType, String recordId, String details) {
		logAction(userId, recordType, recordId, "D", details);
	}

	private static void logAction(String userId, String recordType, String recordId, String changeType, String details) {
		String sql = "INSERT INTO audit_trail (record_type, record_id, details, change_type, change_date, acc_id) VALUES (?, ?, ?, ?, NOW(), ?)";
		try (Connection conn = DBConnection.getConnection();
			 PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, recordType);
			stmt.setString(2, recordId);
			stmt.setString(3, details);
			stmt.setString(4, changeType);
			stmt.setString(5, userId);
			stmt.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static String mapChangeType(String code) {
		switch (code) {
			case "C": return "Create";
			case "U": return "Update";
			case "D": return "Delete";
			default: return code;
		}
	}

	public static List<AuditTrail> getRecentAuditTrails(int count) {
		List<AuditTrail> list = new ArrayList<>();
		String sql = "SELECT at.change_date, COALESCE(a.username, 'Unknown') AS username, at.record_type, at.change_type, at.details " +
				"FROM audit_trail at LEFT JOIN account a ON at.acc_id = a.acc_id ORDER BY at.change_date DESC LIMIT ?";
		try (Connection conn = DBConnection.getConnection();
			 PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, count);
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					LocalDateTime dateTime = rs.getTimestamp("change_date").toLocalDateTime();
					String user = rs.getString("username");
					String type = rs.getString("record_type");
					String action = mapChangeType(rs.getString("change_type"));
					String details = rs.getString("details");
					list.add(new AuditTrail(dateTime, user, type, action, details));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}
}
