package server;

import java.io.IOException;

public class Main {

	public static void main(String[] args) {
		Database db = null;
		try {
			db = new Database("p2photo.db");
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}
		db.selectAllUsers();
	}

}
