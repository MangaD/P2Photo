package server;

import java.io.IOException;

public class Main {

	public static int port = 4444;
	public static Database db = null;
	public static String db_name = "p2photo.db";

	public static void main(String[] args) {
		
		try {
			db = new Database(db_name);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}
		db.selectAllUsers();

		ClientListener service = new ClientListener(port, "ServiceThread");
		service.start();
	}

}
