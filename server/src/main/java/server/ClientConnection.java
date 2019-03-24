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

	ClientConnection(Socket s, String name) {
		threadName = name;
		clientSocket = s;
	}

	@Override
	public void run() {

		try (PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
				BufferedReader in = new BufferedReader(
						new InputStreamReader(clientSocket.getInputStream()));
				) {
			
			String inputLine;
			out.println("test");

			while ((inputLine = in.readLine()) != null) {
				if (inputLine.equals("Bye.")) {
					break;
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		System.out.println("Thread " + threadName + " exiting.");
	}

	public void start() {
		System.out.println("Starting " + threadName);
		if (t == null) {
			t = new Thread(this, threadName);
			t.start();
		}
	}

}
