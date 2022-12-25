package controllerDb;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import common.CommonFunctions;
import common.Message;
import common.TaskType;
import entity.OrderReportEntity;
import entity.SupplyReportEntity;
import mysql.MySqlClass;
import ocsf.server.ConnectionToClient;

public class SupplyReportDBController {
	private static String month, year, region;

	/**
	 * Parse the string array into username and password
	 * 
	 * @param usernamePassword
	 * @return
	 */
	public static boolean setReport(String[] details) {
		if (details.length == 3) {
			region = details[0];
			month = details[1];
			year = details[2];
			return true;
		}
		return false;
	}
	/**
	 * Handles getting selected report and sending the entity back to client
	 * 
	 * @param usernamePassword
	 * @param client
	 */
	public static void getSupplyReportEntity(String[] details, ConnectionToClient client) {
		if (setReport(details)) {
			// SQL query //
			SupplyReportEntity res = getSupplyReportFromDB();
			try {
				client.sendToClient(new Message(TaskType.RequestSupplyReport, res));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	/**
	 * Handles the query of getting the report from DB
	 * 
	 * @return
	 */
	protected static SupplyReportEntity getSupplyReportFromDB() {
		
		SupplyReportEntity report = new SupplyReportEntity();
		try {
			if (MySqlClass.getConnection() == null)
				return report;
			Connection conn = MySqlClass.getConnection();
			PreparedStatement ps = conn.prepareStatement("SELECT * FROM ekrut.supply_report WHERE month=? AND year=? AND region=?;");
			ps.setString(1, CommonFunctions.getNumericMonth(month));
			ps.setString(2, year);
			ps.setString(3, region);
			ResultSet res = ps.executeQuery();
			if (res.next()) {
				report = new SupplyReportEntity(res.getInt(1), res.getString(2), res.getString(3), res.getString(4),
						res.getString(5), res.getString(6), res.getString(7), res.getString(8), res.getString(9), res.getString(10));
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return report;

	}
}
