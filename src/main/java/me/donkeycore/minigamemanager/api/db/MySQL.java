package me.donkeycore.minigamemanager.api.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.commons.lang.Validate;

public class MySQL {
	
	private final String ip, database;
	private int port;
	
	public MySQL(String ip, int port, String database) {
		Validate.notEmpty(ip, "IP cannot be empty");
		Validate.isTrue(port >= 0 && port <= 65536, "Port must be within 0-65536");
		Validate.notEmpty(database, "Database cannot be empty");
		this.ip = ip;
		this.port = port;
		this.database = database;
	}
	
	public String getIp() {
		return ip;
	}
	
	public int getPort() {
		return port;
	}
	
	public String getDatabase() {
		return database;
	}
	
	public Connection connect() throws SQLException {
		return connect(null, null);
	}
	
	public Connection connect(String username, String password) throws SQLException {
		if ((username == null && password != null) || (username != null && password == null))
			throw new IllegalArgumentException("Username and password must both be null or both be used");
		String db = "jdbc:mysql://" + ip + ":" + port + "/" + database;
		try {
			Class.forName("com.mysql.jdbc.Driver"); // ensure driver exists
			
			if (username == null && password == null)
				return DriverManager.getConnection(db);
			else
				return DriverManager.getConnection(db, username, password);
		} catch (ClassNotFoundException nodriver) {
			throw new NoDriverException(nodriver);
		}
	}
	
}
