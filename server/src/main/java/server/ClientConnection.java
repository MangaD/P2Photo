package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;

public class ClientConnection implements Runnable {

	private Thread t;
	private String threadName;
	private Socket clientSocket;
	private PrintWriter out;
	private BufferedReader in;
	
	private boolean isLoggedIn;
	private String user;

	ClientConnection(Socket s, String name) throws IOException {
		threadName = name;
		clientSocket = s;
		out = new PrintWriter(clientSocket.getOutputStream(), true);
		in = new BufferedReader(
				new InputStreamReader(clientSocket.getInputStream()));
		isLoggedIn = false;
		this.user = "";
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
						out.println("true");
						isLoggedIn = true;
						this.user = user;
					} else {
						System.out.println("Login insuccessful.");
						out.println("false");
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
					
				} else if (inputLine.equals("getusers")) {
					
					if (! isLoggedIn) {
						out.println("You're not logged in!");
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
						out.println("You're not logged in!");
						continue;
					}
					
					System.out.println("Received get user's albums from '" + user + "'.");

					ArrayList<String> res = Main.db.getUsersAlbums(user);
					
					for (String s : res) {
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
