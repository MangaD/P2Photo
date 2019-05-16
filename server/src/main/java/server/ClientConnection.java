package server;

import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ClientConnection implements Runnable {

	private Thread t;
	private String threadName;
	private Socket clientSocket;
	private DataOutputStream out;
	private DataInputStream in;
	
	private boolean isLoggedIn;
	
	// Right way to do it: https://codeahoy.com/2016/04/13/generating-session-ids/
	private int sessionID;
	
	private String user;

	ClientConnection(Socket s, String name) throws IOException {
		threadName = name;
		clientSocket = s;
		out = new DataOutputStream(clientSocket.getOutputStream());
		in = new DataInputStream(clientSocket.getInputStream());
		isLoggedIn = false;
		this.user = "";
		this.sessionID = Math.abs(new Random().nextInt());
	}

	@Override
	public void run() {

		try {
			
			String inputLine;

			while ((inputLine = read()) != null) {
				
				System.out.println("Received: " + inputLine);
				
				if (inputLine.equals("login")) {
					
					String user = read();
					while (user.isEmpty()) {
						user = read();
					}
					String password = read();
					while (password.isEmpty()) {
						password = read();
					}

					System.out.println("Received login from '" + user + "' with password '" + password + "'.");

					String[] pair = Main.db.login(user, password);
					
					if (pair != null && !pair[0].isEmpty() && !pair[1].isEmpty()) {
						System.out.println("Login successful.");
						write(Integer.toString(sessionID));
						write(pair[0]);
						write(pair[1]);
						isLoggedIn = true;
						this.user = user;
					} else {
						System.out.println("Login insuccessful.");
						write("");
					}
					
				} else if (inputLine.equals("signup")) {
					
					String user = read();
					while (user.isEmpty()) {
						user = read();
					}
					String password = read();
					while (password.isEmpty()) {
						password = read();
					}
					String pubKey = read();
					while (pubKey.isEmpty()) {
						pubKey = read();
					}
					String privKey = read();
					while (privKey.isEmpty()) {
						privKey = read();
					}

					System.out.println("Received sign up from '" + user + "' with password '" + password + "'.");

					try {
						Main.db.signUp(user, password, pubKey, privKey);
						write("Sign up successful.");
					} catch (SQLException e) {
						// https://www.sqlite.org/rescode.html#constraint
						if (e.getErrorCode() == 19) {
							write("User with that name already exists.");
						} else {
							write("Sign up unsuccessful. Error code: " + e.getErrorCode());
						}
					}
					
				} else if (inputLine.equals("createalbum")) {
					
					if (! isLoggedIn) {
						write("You're not logged in!");
						continue;
					}
					
					if (! verifySessionId()) {
						continue;
					}
					
					String name = read();
					while (name.isEmpty()) {
						name = read();
					}

					System.out.println("Received create album from '" + user + "' with name '" + name + "'.");

					try {
						Main.db.createAlbum(user, name);
						System.out.println("Album created successfully.");
						write("Album created successfully.");
					} catch (SQLException e) {
						// https://www.sqlite.org/rescode.html#constraint
						System.out.println(e.getMessage());
						if (e.getErrorCode() == 19) {
							write("Album with that name already exists.");
						} else {
							write("Album creation failed. Error code: " + e.getErrorCode());
						}
					}
					
				} else if (inputLine.equals("setalbumindex")) {
					
					if (! isLoggedIn) {
						write("You're not logged in!");
						continue;
					}
					
					if (! verifySessionId()) {
						continue;
					}
					
					String name = read();
					while (name.isEmpty()) {
						name = read();
					}
					String index = read();
					while (index.isEmpty()) {
						index = read();
					}
					String key = read();
					while (key.isEmpty()) {
						key = read();
					}

					System.out.println("Received set album index from '" + user + "' with name '" +
							name + "' and index '" + index + "'.");

					try {
						Main.db.setAlbumIndex(name, user, index, key);
						write("Album created successfully.");
					} catch (SQLException e) {
						// https://www.sqlite.org/rescode.html#constraint
						if (e.getErrorCode() == 19) {
							write("Album with that name already exists.");
						} else {
							write("Album creation failed. Error code: " + e.getErrorCode());
						}
					}
				} else if (inputLine.equals("getusers")) {

					if (! isLoggedIn) {
						write("You're not logged in!");
						continue;
					}

					if (! verifySessionId()) {
						continue;
					}

					System.out.println("Received get users from '" + user + "'.");

					HashMap<String, String> res = Main.db.getUsers();

					for (Map.Entry<String, String> entry : res.entrySet()) {
						String key = entry.getKey();
						String value = entry.getValue();
						System.out.println("Key: " + key + "\nValue: " + value);
						write(key);
						write(value);
					}

					// send empty string for terminating
					write("");
				} else if (inputLine.equals("getusersownedalbums")) {
					
					if (! isLoggedIn) {
						System.out.println("You're not logged in!");
						write("You're not logged in!");
						continue;
					}
					
					if (! verifySessionId()) {
						continue;
					}
					
					System.out.println("Received get user's owned albums from '" + user + "'.");

					HashMap<Integer, String[]> res = Main.db.getUsersOwnedAlbums(user);
					
					for (Map.Entry<Integer, String[]> entry : res.entrySet()) {
						Integer aid = entry.getKey();
						String[] pair = entry.getValue();
						String name = pair[0];
						String key = pair[1];
						System.out.println("Aid: " + key + "\nName: " + name + "\nKey: " + key);
						write(Integer.toString(aid));
						write(name);
						write(key);
					}
					// send empty string for terminating
					write("");
				} else if (inputLine.equals("getusersallowedalbums")) {
					
					if (! isLoggedIn) {
						System.out.println("You're not logged in!");
						write("You're not logged in!");
						continue;
					}
					
					if (! verifySessionId()) {
						continue;
					}
					
					System.out.println("Received get user's allowed albums from '" + user + "'.");

					HashMap<Integer, String> res = Main.db.getUsersAllowedAlbums(user);
					
					for (Map.Entry<Integer, String> entry : res.entrySet()) {
						Integer key = entry.getKey();
						String value = entry.getValue();
						System.out.println("Key: " + key + "\nValue: " + value);
						write(Integer.toString(key));
						write(value);
					}
					// send empty string for terminating
					write("");
				} else if (inputLine.equals("getalbumindexes")) {
					
					if (! isLoggedIn) {
						System.out.println("You're not logged in!");
						write("You're not logged in!");
						continue;
					}
					
					if (! verifySessionId()) {
						continue;
					}
					
					String name = read();
					while (name.isEmpty()) {
						name = read();
					}
					
					System.out.println("Received get album indexes from '" + user + "' with album name '" + name + "'.");

					int uid = Main.db.getUid(user);
					HashMap<Integer, String[]> res = Main.db.getAlbumIndexes(name);
					
					// Write encrypted symmetric key that user can decrypt with his private key
					for (Map.Entry<Integer, String[]> entry : res.entrySet()) {
						int u = entry.getKey();
						String key = entry.getValue()[1];
						System.out.println("Key: " + key);
						if(u == uid) {
							write(key);
						}
					}
					
					// Write urls
					for (Map.Entry<Integer, String[]> entry : res.entrySet()) {
						String url = entry.getValue()[0];
						System.out.println("Index: " + url);
						write(url);
					}

					// send empty string for terminating
					write("");
				} else if (inputLine.equals("givepermission")) {
					
					if (! isLoggedIn) {
						write("You're not logged in!");
						continue;
					}
					
					if (! verifySessionId()) {
						continue;
					}
					
					String userName = read();
					while (userName.isEmpty()) {
						userName = read();
					}
					
					String albumName = read();
					while (albumName.isEmpty()) {
						albumName = read();
					}
					
					String key = read();
					while (key.isEmpty()) {
						key = read();
					}
					
					// Can be empty
					String index = read();

					System.out.println("Received give permission from '" + user + "' to user name '" +
							userName + "', for album '" + albumName + "' with index '" + index + "'.");

					try {
						Main.db.setAlbumIndex(albumName, userName, index, key);
						write("Permission given successfully.");
					} catch (SQLException e) {
						// https://www.sqlite.org/rescode.html#constraint
						if (e.getErrorCode() == 19) {
							write("Permission already exists.");
						} else {
							write("Granting permission failed. Error code: " + e.getErrorCode());
						}
					}
					
				}
			}
			System.out.println("Waiting request from user '" + user + "'");
		} catch (IOException e) {}

		System.out.println("Thread " + threadName + " exiting.");
	}

	private boolean verifySessionId() throws IOException {
		String session = read();
		while (session.isEmpty()) {
			session = read();
		}
		try {
			int res = Integer.parseInt(session);
			if (res != sessionID) {
				write("Invalid session ID!");
				return false;
			} else {
				return true;
			}
		} catch (Exception e) {
			write("Invalid session ID!");
			return false;
		}
	}
	
	private String read() throws IOException {
		try {
			return in.readUTF().trim();
		} catch (NullPointerException e) {
			return null;
		}
	}

	private void write(String message) throws IOException {
		out.writeUTF(message + "\n");
	}
	
	public void start() {
		System.out.println("Starting " + threadName);
		if (t == null) {
			t = new Thread(this, threadName);
			t.start();
		}
	}

}
