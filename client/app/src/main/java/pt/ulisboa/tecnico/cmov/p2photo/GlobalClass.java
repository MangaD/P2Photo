package pt.ulisboa.tecnico.cmov.p2photo;

import android.app.Application;

public class GlobalClass extends Application {

    private ServerConnection serverConn;
    private DriveConnection driveConn;
    private String storageMode;

    @Override
    public void onCreate() {
        super.onCreate();
        serverConn = new ServerConnection();
        driveConn = new DriveConnection(this);
    }

    /*
     * Getters and Setters
     */
    public ServerConnection getServerConnection() {
        return serverConn;
    }

    public DriveConnection getDriveConnection() {
        return driveConn;
    }

    public String getStorageMode() {
        return this.storageMode;
    }

    public void setStorageMode(String mode) {
        this.storageMode = mode;
    }
}
