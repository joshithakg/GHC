package com.ghc.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class GHCServlet extends HttpServlet {
	String jdbcDriver;
	String jdbcURL;
	String userName;
	String userPass;
	public GHCServlet() {
		
	}
	
	public void init(final ServletConfig config) throws ServletException {
		super.init(config);
		jdbcDriver = getInitParameter("jdbcDriver");
		jdbcURL = getInitParameter("jdbcURL");
        userName = getInitParameter ("jdbcUsername");
        userPass = getInitParameter ("jdbcUserpass");
	}
	
	private Connection getConnection() {
		Connection con=null;
		try {
			Class.forName(jdbcDriver);
			con = DriverManager.getConnection(jdbcURL, userName, userPass);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return con;
	}
	
	public void doGet(final HttpServletRequest req,
            final HttpServletResponse res) throws ServletException {
		doPost(req, res);
   		
	}
	
	public void doPost(final HttpServletRequest req,
            final HttpServletResponse res) throws ServletException {
		String op = req.getParameter("op");
		
		System.out.println("op: " + op);
		String resData = "Success";
		if (op.equals("hosp")) {
			String lat = req.getParameter("lat");
			String lng = req.getParameter("lng");
			System.out.println("lat: " + lat + ", lng: " + lng);
			resData = getHospitals(lat,lng);
		}
		else if (op.equals("alert")) {
			String lat = req.getParameter("lat");
			String lng = req.getParameter("lng");
			System.out.println("lat: " + lat + ", lng: " + lng);
			resData = alert(lat, lng);
		}
		else if (op.equals("new")) {
			String latstart = req.getParameter("latstart");
			String lngstart = req.getParameter("lngstart");
			String latend = req.getParameter("latend");
			String lngend = req.getParameter("lngend");
			String dest = req.getParameter("dest");
			System.out.println("latstart: " + latstart + ", lngstart: " + lngstart);
			System.out.println("latend: " + latend + ", latend: " + latend);
			System.out.println("dest: " + dest);
			resData = newcommute(latstart, lngstart, latend, lngend, dest);
		}
		else if (op.equals("update")) {
			String commuteid = req.getParameter("commuteid");
			String latcurr = req.getParameter("latcurr");
			String lngcurr = req.getParameter("lngcurr");
			System.out.println("commuteid: " + commuteid);
			System.out.println("latcurr: " + latcurr + ", lngcurr: " + lngcurr);
			updatecommute(commuteid, latcurr, lngcurr);
		}
		System.out.println("data: " + resData);
		PrintWriter pw;
		try {
			res.setContentType("text/plain");
			pw = res.getWriter();
			pw.print(resData);
	   		pw.close();	
		} catch (IOException e) {
			System.out.println("Error: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	private String alert(String lat, String lng) {
		String details = "";
		Connection con = null;
		Statement stmt;
		try {
			con = getConnection();
			stmt = con.createStatement();
			
			String sql = "SELECT commuteid, latcurr, lngcurr, round(( 6371 * acos( cos( radians(" + lat + ") ) * cos( radians( latcurr ) ) "
					+ "* cos( radians( lngcurr ) - radians(" + lng +") ) + sin( radians("+ lat + ") ) * "
					+ "sin( radians( latcurr ) ) ) ),2) AS distance FROM commute HAVING distance < 0.5 ORDER BY distance LIMIT 0 , 25";
			System.out.println("sql: " + sql);
			ResultSet rs = stmt.executeQuery(sql);
			details = "{\"commute\":[";
			boolean first = true;
			while (rs.next()) {
				if (!first) {
					details = details + ",";
				}
				first = false;
				details = details + "{\"commuteid\":\"";
				details = details + rs.getString("commuteid");
				details = details + "\",\"latcurr\":\"";
				details = details + rs.getString("latcurr");
				details = details + "\",\"lngcurr\":\"";
				details = details + rs.getString("lngcurr");
				details = details + "\",\"distance\":\"";
				details = details + rs.getString("distance");
				details = details + "\"}";
			}
			details = details + "]}";
		}
		catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (con != null) {
				try {
					con.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return details;
	}
	
	private String newcommute(String latstart, String lngstart, String latend, String lngend, 
			String dest) {
		Connection con = null;
		Statement stmt;
		String commuteid = "-1";
		try {
			con = getConnection();
			stmt = con.createStatement();
			
			String sql = "delete from commute";
			stmt.executeUpdate(sql);
			System.out.println("sql: " + sql);
			
			sql = "insert into commute(latstart,lngstart,latend,lngend,latcurr,lngcurr,lastupdated,dest) values(" +
					Float.parseFloat(latstart) + "," + Float.parseFloat(lngstart) + "," +
					Float.parseFloat(latend) + "," + Float.parseFloat(lngend) + "," +
					Float.parseFloat(latstart) + "," + Float.parseFloat(lngstart) + ", now(), '" +
					dest + "')";
			System.out.println("sql: " + sql);
			stmt.executeUpdate(sql);
			
			/*sql = "update commute set latcurr=" +
					Float.parseFloat(latstart) + ",lngcurr=" + Float.parseFloat(lngstart) + 
					" where commuteid=" + commuteid;
			System.out.println("sql: " + sql);
			stmt.executeUpdate(sql);*/
			
			sql = "select max(commuteid) from commute";
			ResultSet rs = stmt.executeQuery(sql);
			if (rs.next()) {
				commuteid = rs.getString(1);
			}
			System.out.println("commuteid: " + commuteid);
		}
		catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (con != null) {
				try {
					con.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		
		return commuteid;
	}
	
	private void updatecommute(String commuteid, String latcurr, String lngcurr) {
		Connection con = null;
		Statement stmt;
		try {
			con = getConnection();
			stmt = con.createStatement();
			String sql = "update commute set latcurr=" +
					Float.parseFloat(latcurr) + ",lngcurr=" + Float.parseFloat(lngcurr) + 
					" where commuteid=" + commuteid;
			System.out.println("sql: " + sql);
			stmt.executeUpdate(sql);
		}
		catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (con != null) {
				try {
					con.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private String getHospitals(String lat, String lng) {
		String details = "";
		Connection con = null;
		Statement stmt;
		try {
			con = getConnection();
			stmt = con.createStatement();
			
			String sql = "SELECT hospid, hospname, lat, lng, phone, round(( 6371 * acos( cos( radians(" + lat + ") ) * cos( radians( lat ) ) "
					+ "* cos( radians( lng ) - radians(" + lng +") ) + sin( radians("+ lat + ") ) * "
					+ "sin( radians( lat ) ) ) ),2) AS distance FROM hospitals HAVING distance < 25 ORDER BY distance LIMIT 0 , 20";
			System.out.println("sql: " + sql);
			ResultSet rs = stmt.executeQuery(sql);
			details = "{\"hospitals\":[";
			boolean first = true;
			while (rs.next()) {
				if (!first) {
					details = details + ",";
				}
				first = false;
				details = details + "{\"id\":\"";
				details = details + rs.getString("hospid");
				details = details + "\",\"name\":\"";
				details = details + rs.getString("hospname");
				details = details + "\",\"lat\":\"";
				details = details + rs.getString("lat");
				details = details + "\",\"lng\":\"";
				details = details + rs.getString("lng");
				details = details + "\",\"phone\":\"";
				details = details + rs.getString("phone");
				details = details + "\",\"distance\":\"";
				details = details + rs.getString("distance");
				details = details + "\"}";
			}
			details = details + "]}";
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (con != null) {
				try {
					con.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return details;
	}

}
