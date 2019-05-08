package server;

import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
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

					if (Main.db.login(user, password)) {
						System.out.println("Login successful.");
						write(Integer.toString(sessionID));
						isLoggedIn = true;
						this.user = user;
					} else {
						System.out.println("Login insuccessful.");
						write("-1");
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

					System.out.println("Received sign up from '" + user + "' with password '" + password + "'.");

					try {
						Main.db.signUp(user, password);
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
						write("Album created successfully.");
					} catch (SQLException e) {
						// https://www.sqlite.org/rescode.html#constraint
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

					System.out.println("Received create album from '" + user + "' with name '" +
							name + "' and index '" + index + "'.");

					try {
						Main.db.setAlbumIndex(name, user, index);
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

					ArrayList<String> res = Main.db.getUsers();
					
					for (String s : res) {
						write(s);
					}
					write("");
				} else if (inputLine.equals("getuseralbums")) {
					
					if (! isLoggedIn) {
						System.out.println("You're not logged in!");
						write("You're not logged in!");
						continue;
					}
					
					if (! verifySessionId()) {
						continue;
					}
					
					System.out.println("Received get user's albums from '" + user + "'.");

					ArrayList<String> res = Main.db.getUsersAlbums(user);
					
					for (String s : res) {
						System.out.println(s);
						write(s);
					}
					write("");
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

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
