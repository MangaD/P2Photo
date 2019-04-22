package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Random;

public class ClientConnection implements Runnable {

	private Thread t;
	private String threadName;
	private Socket clientSocket;
	private PrintWriter out;
	private BufferedReader in;
	
	private boolean isLoggedIn;
	
	// Right way to do it: https://codeahoy.com/2016/04/13/generating-session-ids/
	private int sessionID;
	
	private String user;

	ClientConnection(Socket s, String name) throws IOException {
		threadName = name;
		clientSocket = s;
		out = new PrintWriter(clientSocket.getOutputStream(), true);
		in = new BufferedReader(
				new InputStreamReader(clientSocket.getInputStream()));
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
						out.println(Integer.toString(sessionID));
						isLoggedIn = true;
						this.user = user;
					} else {
						System.out.println("Login insuccessful.");
						out.println("-1");
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
						out.println("Sign up successful.");
					} catch (SQLException e) {
						// https://www.sqlite.org/rescode.html#constraint
						if (e.getErrorCode() == 19) {
							out.println("User with that name already exists.");
						} else {
							out.println("Sign up unsuccessful. Error code: " + e.getErrorCode());
						}
					}
					
				} else if (inputLine.equals("createalbum")) {
					
					if (! isLoggedIn) {
						out.println("You're not logged in!");
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
						out.println("Album created successfully.");
					} catch (SQLException e) {
						// https://www.sqlite.org/rescode.html#constraint
						if (e.getErrorCode() == 19) {
							out.println("Album with that name already exists.");
						} else {
							out.println("Album creation failed. Error code: " + e.getErrorCode());
						}
					}
					
				} else if (inputLine.equals("setalbumindex")) {
					
					if (! isLoggedIn) {
						out.println("You're not logged in!");
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
						out.println("Album created successfully.");
					} catch (SQLException e) {
						// https://www.sqlite.org/rescode.html#constraint
						if (e.getErrorCode() == 19) {
							out.println("Album with that name already exists.");
						} else {
							out.println("Album creation failed. Error code: " + e.getErrorCode());
						}
					}
					
				} else if (inputLine.equals("getusers")) {
					
					if (! isLoggedIn) {
						out.println("You're not logged in!");
						continue;
					}
					
					if (! verifySessionId()) {
						continue;
					}
					
					System.out.println("Received get users from '" + user + "'.");

					ArrayList<String> res = Main.db.getUsers();
					
					for (String s : res) {
						out.println(s);
					}
					// send empty string for terminating
					out.println();
				} else if (inputLine.equals("getuseralbums")) {
					
					if (! isLoggedIn) {
						System.out.println("You're not logged in!");
						out.println("You're not logged in!");
						continue;
					}
					
					if (! verifySessionId()) {
						continue;
					}
					
					System.out.println("Received get user's albums from '" + user + "'.");

					ArrayList<String> res = Main.db.getUsersAlbums(user);
					
					for (String s : res) {
						System.out.println(s);
						out.println(s);
					}
					// send empty string for terminating
					out.println();
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
            	out.println("Invalid session ID!");
				return false;
            } else {
                return true;
            }
        } catch (Exception e) {
        	out.println("Invalid session ID!");
            return false;
        }
	}
	
	private String read() throws IOException {
		try {
			return in.readLine().trim();
		} catch (NullPointerException e) {
			return null;
		}
	}
	
	public void start() {
		System.out.println("Starting " + threadName);
		if (t == null) {
			t = new Thread(this, threadName);
			t.start();
		}
	}

}
