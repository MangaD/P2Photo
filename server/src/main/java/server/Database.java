package server;

import java.io.File;
import java.io.IOException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;
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
	
	public String getUserSalt(String user) {
		String sql = "SELECT salt FROM users WHERE username = ?";
		
		try (PreparedStatement pstmt  = conn.prepareStatement(sql)) {
			pstmt.setString(1, user);
			
			ResultSet rs  = pstmt.executeQuery();

			while (rs.next()) {
				return rs.getString("salt");
			}

		} catch (SQLException e) {
			System.out.println(e.getMessage());
			return null;
		}
		
		return null;
	}
	
	public String[] login(String user, String password) {
		String sql = "SELECT username, password, enc_priv_key, pub_key FROM users WHERE username = ? AND password = ?";
		
		try (PreparedStatement pstmt  = conn.prepareStatement(sql)) {
			pstmt.setString(1, user);
			pstmt.setString(2, password);
			
			ResultSet rs  = pstmt.executeQuery();

			while (rs.next()) {
				String u = rs.getString("username");
				String p = rs.getString("password");
				String encPrivKey = rs.getString("enc_priv_key");
				String pubKey = rs.getString("pub_key");
				String[] pair = new String[2];
				pair[0] = encPrivKey;
				pair[1] = pubKey;
				if (u.equals(user) && p.equals(password)) {
					return pair;
				}
			}

		} catch (SQLException e) {
			System.out.println(e.getMessage());
			return null;
		}
		
		return null;
	}
	
	public void signUp(String user, String password, String salt, String pubKey, String privKey) throws SQLException {
		String sql = "INSERT INTO users (username, password, salt, pub_key, enc_priv_key) VALUES(?, ?, ?, ?, ?)";
		
		PreparedStatement pstmt  = conn.prepareStatement(sql);
		pstmt.setString(1, user);
		pstmt.setString(2, password);
		pstmt.setString(3, salt);
		pstmt.setString(4, pubKey);
		pstmt.setString(5, privKey);
		
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
	
	public void setAlbumIndex(String album_name, String user_name, String index, String key) throws SQLException {
		
		String sql = "INSERT INTO album_slices (aid, uid, url, key) " +
				" VALUES((SELECT aid FROM albums WHERE name = ?), (SELECT uid FROM users WHERE username = ?), ?, ?)";
		
		PreparedStatement pstmt  = conn.prepareStatement(sql);
		pstmt.setString(1, album_name);
		pstmt.setString(2, user_name);
		pstmt.setString(3, index);
		pstmt.setString(4, key);
		
		pstmt.executeUpdate();
	}
	
	public HashMap<String, String> getUsersWithoutAlbumAccess(String albumName) {
		String sql = "SELECT username, pub_key FROM users "
				+ " WHERE uid NOT IN (SELECT uid FROM album_slices WHERE aid IN "
				+ " (SELECT aid FROM albums WHERE name = ?)) ";
		HashMap<String, String> result = new HashMap<>();
		
		try (PreparedStatement pstmt  = conn.prepareStatement(sql)) {

			pstmt.setString(1, albumName);
			
			ResultSet rs  = pstmt.executeQuery();
			
			// loop through the result set
			while (rs.next()) {
				result.put(rs.getString("username").trim(), rs.getString("pub_key").trim());
			}
			return result;
			
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			return null;
		}
	}
	
	public HashMap<String, String> getUsers() {
		String sql = "SELECT username, pub_key FROM users";
		HashMap<String, String> result = new HashMap<>();
		
		try (Statement stmt = this.conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {

			// loop through the result set
			while (rs.next()) {
				result.put(rs.getString("username").trim(), rs.getString("pub_key").trim());
			}
			return result;
			
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			return null;
		}
	}
	
	public HashMap<Integer, String[]> getUsersOwnedAlbums(String username) {
		String sql = " SELECT A.aid, A.name, A_S.key " + 
				" FROM albums A INNER JOIN album_slices A_S " + 
				" ON A.aid = A_S.aid " + 
				" WHERE A.owner_id IN " + 
				" (SELECT uid FROM users WHERE username = ?) ";
		HashMap<Integer, String[]> result = new HashMap<>();
		
		try (PreparedStatement pstmt  = conn.prepareStatement(sql)) {
			
			pstmt.setString(1, username);
			
			ResultSet rs  = pstmt.executeQuery();

			while (rs.next()) {
				int aid = rs.getInt("aid");
				String[] pair = new String[2];
				pair[0] = rs.getString("name").trim();
				pair[1] = rs.getString("key").trim();
				result.put(aid, pair);
			}
			return result;
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			return null;
		}
	}
	
	public HashMap<Integer, String> getUsersAllowedAlbums(String username) {
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
	
	public String getAlbumKey(String username, String albumName) {
		String sql = " SELECT key FROM album_slices "
				+ " WHERE aid IN (SELECT aid FROM albums WHERE name = ?) "
				+ " AND uid IN (SELECT uid FROM users WHERE username = ?); ";
		
		try (PreparedStatement pstmt  = conn.prepareStatement(sql)) {
			
			pstmt.setString(1, albumName);
			pstmt.setString(2, username);
			
			ResultSet rs  = pstmt.executeQuery();

			while (rs.next()) {
				return rs.getString("key").trim();
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		return null;
	}
	
	public HashMap<Integer, String> getAlbumIndexes(String albumName) {
		String sql = "SELECT uid, url FROM album_slices "
				+ " WHERE aid IN (SELECT aid FROM albums WHERE name = ?)"
				+ " AND url IS NOT NULL AND URL != ''; ";
		HashMap<Integer, String> result = new HashMap<>();
		try (PreparedStatement pstmt  = conn.prepareStatement(sql)) {
			
			pstmt.setString(1, albumName);
			
			ResultSet rs  = pstmt.executeQuery();

			while (rs.next()) {
				int uid = rs.getInt("uid");
				String url = rs.getString("url").trim();
				result.put(uid, url);
			}
			return result;
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			return null;
		}
	}
	
	public Integer getUid(String username) {
		String sql = "SELECT uid FROM users WHERE username = ?;";
		try (PreparedStatement pstmt  = conn.prepareStatement(sql)) {
			
			pstmt.setString(1, username);
			
			ResultSet rs  = pstmt.executeQuery();

			while (rs.next()) {
				return rs.getInt("uid");
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		return null;
	}
}
