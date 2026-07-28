package org;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class MySQLTest {

	private void connectToDB() {
		System.out.println("I am called - connectToDB");
		try {
			Class.forName("com.mysql.jdbc.Driver");
			Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/ghc", "root",
					"s6,SciCyto0729");
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("select * from hospitals");
			while (rs.next())
				System.out.println(rs.getInt(1) + "  " + rs.getString(2) + "  " + rs.getString(3));
			con.close();
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	private void insertHospitals() {
		System.out.println("I am called - insertHospitals");
	}

	private void select() {
		System.out.println("I am called - select");
	}

	public static void main(String[] args) {
		System.out.println("I am called - main");
		MySQLTest test = new MySQLTest();
		test.connectToDB();
		System.out.println("I am ended - main");
		test.insertHospitals();
	}

}
