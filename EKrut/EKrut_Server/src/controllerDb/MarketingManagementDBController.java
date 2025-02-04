package controllerDb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import common.Message;
import enums.TaskType;
import entity.SaleEntity;
import entity.SaleEntity.SaleStatus;
import mysql.MySqlClass;
import ocsf.server.ConnectionToClient;

/**
 * The Class MarketingManagementDBController.
 */
public class MarketingManagementDBController {
	/**
	This variable holds a connection to a MySQL database.
	*/
	private static Connection con = MySqlClass.getConnection();
	
	/**
	 * Insert sale entities.
	 *
	 * @param saleEntity the sale entity
	 * @param client the client
	 */
	/*
	 * This method is for inserting a new Sale Entity into the database.
	 * @param SaleEntity The SaleEntity object which contains all the details of the sale
	 * @param client The ConnectionToClient object, represent the connection between the server and the client.
	*/
	public static void insertSaleEntities(SaleEntity saleEntity, ConnectionToClient client) {
		
		try {
			if (con == null)
				return;
				if(isSaleExist(saleEntity)) {
					client.sendToClient(new Message(TaskType.InsertSaleAnswer, false));
					return;
				} 
			PreparedStatement ps=con.prepareStatement("INSERT INTO ekrut.sales (region, sale_type,days, start_time, end_time) "
					+ "VALUES (?, ?, ?, ?, ?);");
			ps.setString(1,saleEntity.getRegion());
			ps.setString(2,saleEntity.getSaleType());
			ps.setString(3,saleEntity.getDays());
			ps.setString(4, saleEntity.getStartTime().toString());
			ps.setString(5, saleEntity.getEndTime().toString());
			ps.executeUpdate();
			client.sendToClient(new Message(TaskType.InsertSaleAnswer, true));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Update sale entities.
	 *
	 * @param saleLst the sale lst
	 * @param client the client
	 */
	public static void updateSaleEntities(ArrayList<SaleEntity> saleLst, ConnectionToClient client) {
		
		try {
			if (con == null)
				return;
			for (SaleEntity saleEntity : saleLst) {
				PreparedStatement ps=con.prepareStatement("UPDATE ekrut.sales SET sale_status=? WHERE id=?;");
				ps.setString(1, saleEntity.getSaleStatus().toString());
				ps.setInt(2, saleEntity.getSaleID());
				ps.executeUpdate();

			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Gets the sales.
	 *
	 * @param region the region
	 * @param client the client
	 * return the sales
	 */
	public static void getSales(String region ,ConnectionToClient client) {
		ArrayList<SaleEntity> sales=new ArrayList<SaleEntity>();
		SaleEntity saleEntity;
		SaleStatus saleStatus;
		try {
			if (con == null)
				return;
			PreparedStatement ps=con.prepareStatement("SELECT * FROM ekrut.sales WHERE region=?;");
			ps.setString(1,region);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				saleStatus=SaleStatus.valueOf(rs.getString(7));
				saleEntity = new SaleEntity(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4)
						 , LocalTime.parse( rs.getString(5)), LocalTime.parse(rs.getString(6)), saleStatus);
				sales.add(saleEntity);
			} 
			client.sendToClient(new Message(TaskType.ReceiveSalesFromServer, sales));
			rs.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Checks if is sale exist.
	 *
	 * @param saleEntity the sale entity
	 * @return the boolean
	 */
	public static Boolean isSaleExist(SaleEntity saleEntity) {
	
		try {
			if (con == null)
				return false;
			PreparedStatement ps=con.prepareStatement("SELECT * FROM ekrut.sales WHERE region=? And sale_type=? And days=?"
					+ " And start_time=? And end_time=?;");
			ps.setString(1,saleEntity.getRegion());
			ps.setString(2,saleEntity.getSaleType());
			ps.setString(3,saleEntity.getDays());
			ps.setString(4,saleEntity.getStartTime().toString());
			ps.setString(5,saleEntity.getEndTime().toString());
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				return true;
			} 
			rs.close();
			return false;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	//"Monday","Tuesday","Wednesday","Thursday","Friday","Saturday","Sunday"
	/**
	 * Gets the active sales by region.
	 *
	 * @param region the region
	 * @param client the client
	 * return the active sales by region
	 */
	public static void getActiveSalesByRegion(String region ,ConnectionToClient client) {
		ArrayList<SaleEntity> sales=new ArrayList<SaleEntity>();
		SaleEntity saleEntity;
		SaleStatus saleStatus;
		Locale.setDefault(Locale.ENGLISH);
		Format dayFormat = new SimpleDateFormat("EEEEEEE"); 
		Date date = new Date();
		String day = dayFormat.format(date);
		try {
			if (con == null)
				return;
			// CONTAINS (days, '<yourSubstring>');
			PreparedStatement ps=con.prepareStatement("SELECT * FROM ekrut.sales"
					+ " WHERE region =? AND sale_status = 'Active' AND days LIKE ?"
					+ "  AND ( (start_time < end_time AND CURTIME() BETWEEN start_time AND end_time)"
					+ "        OR"
					+ "        (end_time < start_time AND CURTIME() NOT BETWEEN start_time AND end_time)"
					+ "      );");
			ps.setString(1,region);
			ps.setString(2,"%"+day+"%");
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				saleStatus=SaleStatus.valueOf(rs.getString(7));
				saleEntity = new SaleEntity(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4)
						 , LocalTime.parse(rs.getString(5)), LocalTime.parse(rs.getString(6)), saleStatus);
				sales.add(saleEntity);
			} 
			client.sendToClient(new Message(TaskType.ReceiveActiveSales, sales));
			
			rs.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
}
