package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientConnection implements Runnable {

	private Thread t;
	private String threadName;
	private Socket clientSocket;
	private PrintWriter out;
	private BufferedReader in;
	
	private boolean isLoggedIn;

	ClientConnection(Socket s, String name) throws IOException {
		threadName = name;
		clientSocket = s;
		out = new PrintWriter(clientSocket.getOutputStream(), true);
		in = new BufferedReader(
				new InputStreamReader(clientSocket.getInputStream()));
		isLoggedIn = false;
	}

	@Override
	public void run() {

		try {
			
			String inputLine;

			while ((inputLine = read()) != null) {
				System.out.println("Received: " + inputLine);
				if (inputLine.equals("login")) {
					
					String user = read();
					String password = read();

					System.out.println("Received login from '" + user + "' with password '" + password + "'.");

					if (Main.db.login(user, password)) {
						System.out.println("Login successful.");
						out.println("true");
						isLoggedIn = true;
					} else {
						System.out.println("Login insuccessful.");
						out.println("false");
					}
					
				}
			}

		} catch (IOException | NullPointerException e) {
			e.printStackTrace();
			return;
		}

		System.out.println("Thread " + threadName + " exiting.");
	}

	private String read() throws IOException {
		return in.readLine().trim();
	}
	
	public void start() {
		System.out.println("Starting " + threadName);
		if (t == null) {
			t = new Thread(this, threadName);
			t.start();
		}
	}

}
