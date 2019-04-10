package pt.ulisboa.tecnico.cmov.p2photo;

import android.app.Application;

public class GlobalClass extends Application {

    private ServerConnection serverConn = new ServerConnection();

    public ServerConnection getConnection() {
        return serverConn;
    }
}
