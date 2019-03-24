package server;

import java.io.IOException;

public class Main {

	public static Database db = null;
	
	public static void main(String[] args) {
		
		try {
			db = new Database("p2photo.db");
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}
		db.selectAllUsers();

		ClientListener service = new ClientListener(4444, "ServiceThread");
		service.start();
	}

}
