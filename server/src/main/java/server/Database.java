package server;

import java.io.File;
import java.io.IOException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

public class Database {

	private Connection conn;
	private String url;

	Database(String url) throws IOException {
		this.url = url;
		this.conn = null;
		
		/**
		 * Check if database file exists because the connect method creates it if it doesn't 
		 */
		File f = new File(this.url);
		if(!(f.exists() && !f.isDirectory())) {
			throw new RuntimeException("Database file not found.");
		}
		
		connect();
	}


	/**
	 * Connect to a database
	 * http://www.sqlitetutorial.net/sqlite-java/sqlite-jdbc-driver/
	 */
	private void connect() {
		String url = "jdbc:sqlite:" + this.url;
		try {
			this.conn = DriverManager.getConnection(url);
			System.out.println("Connection to SQLite has been established.");

		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}

	/**
	 * Select all users
	 * http://www.sqlitetutorial.net/sqlite-java/select/
	 * 
	 * Select with parameters: http://www.sqlitetutorial.net/sqlite-java/select/
	 */
	public void selectAllUsers() {
		String sql = "SELECT uid, username, password FROM users";

		// try-with-resources
		// https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html
		try (Statement stmt = this.conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {

			// loop through the result set
			while (rs.next()) {
				System.out.println(rs.getInt("uid") + "\t" +
						rs.getString("username") + "\t" + rs.getString("password"));
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}
	
	public boolean login(String user, String password) {
		String sql = "SELECT username, password FROM users WHERE username = ? AND password = ?";
		
		try (PreparedStatement pstmt  = conn.prepareStatement(sql)) {
			pstmt.setString(1, user);
			pstmt.setString(2, password);
			
			ResultSet rs  = pstmt.executeQuery();

			while (rs.next()) {
				String u = rs.getString("username");
				String p = rs.getString("password");
				if (u.equals(user) && p.equals(password)) {
					return true;
				}
			}

		} catch (SQLException e) {
			System.out.println(e.getMessage());
			return false;
		}
		
		return false;
	}
	
	public void signUp(String user, String password) throws SQLException {
		String sql = "INSERT INTO users (username, password) VALUES(?, ?)";
		
		PreparedStatement pstmt  = conn.prepareStatement(sql);
		pstmt.setString(1, user);
		pstmt.setString(2, password);
		
		pstmt.executeUpdate();
	}
	
	public void createAlbum(String user, String name) throws SQLException {
		
		String sql = "INSERT INTO albums (name, owner_id) " +
				" VALUES(?, (SELECT uid FROM users WHERE username = ?))";
		
		PreparedStatement pstmt  = conn.prepareStatement(sql);
		pstmt.setString(1, name);
		pstmt.setString(2, user);
		
		pstmt.executeUpdate();
	}
	
	public void setAlbumIndex(String album_name, String user_name, String index) throws SQLException {
		
		String sql = "INSERT INTO album_slices (aid, uid, url) " +
				" VALUES((SELECT aid FROM albums WHERE name = ?), (SELECT uid FROM users WHERE username = ?), ?)";
		
		PreparedStatement pstmt  = conn.prepareStatement(sql);
		pstmt.setString(1, album_name);
		pstmt.setString(2, user_name);
		pstmt.setString(3, index);
		
		pstmt.executeUpdate();
	}
	
	public ArrayList<String> getUsers() {
		String sql = "SELECT username FROM users";
		ArrayList<String> result = new ArrayList<>();
		
		try (Statement stmt = this.conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {

			// loop through the result set
			while (rs.next()) {
				result.add(rs.getString("username").trim());
			}
			return result;
			
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			return null;
		}
	}
	
	public HashMap<Integer, String> getUsersAlbums(String username) {
		String sql = "SELECT aid, name FROM albums WHERE aid IN " +
				" (SELECT aid FROM album_slices WHERE uid IN " +
				" (SELECT uid FROM users WHERE username = ?) )";
		HashMap<Integer, String> result = new HashMap<>();
		
		try (PreparedStatement pstmt  = conn.prepareStatement(sql)) {
			
			pstmt.setString(1, username);
			
			ResultSet rs  = pstmt.executeQuery();

			while (rs.next()) {
				result.put(rs.getInt("aid"), rs.getString("name").trim());
			}
			return result;
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			return null;
		}
	}
	
	public ArrayList<String> getAlbumIndexes(int aid) {
		String sql = "SELECT url FROM album_slices WHERE aid = ?;";
		ArrayList<String> result = new ArrayList<>();
		try (PreparedStatement pstmt  = conn.prepareStatement(sql)) {
			
			pstmt.setInt(1, aid);
			
			ResultSet rs  = pstmt.executeQuery();

			while (rs.next()) {
				result.add(rs.getString("url").trim());
			}
			return result;
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			return null;
		}
	}
}
