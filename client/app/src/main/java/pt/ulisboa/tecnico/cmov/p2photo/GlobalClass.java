package pt.ulisboa.tecnico.cmov.p2photo;

import android.app.Application;

import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveResourceClient;

public class GlobalClass extends Application {

    private ServerConnection serverConn;

    private DriveClient mDriveClient;
    private DriveResourceClient mDriveResourceClient;

    @Override
    public void onCreate() {
        super.onCreate();
        serverConn = new ServerConnection();
    }

    public ServerConnection getConnection() {
        return serverConn;
    }

    public DriveClient getmDriveClient() {
        return mDriveClient;
    }

    public void setmDriveClient(DriveClient mDriveClient) {
        this.mDriveClient = mDriveClient;
    }

    public DriveResourceClient getmDriveResourceClient() {
        return mDriveResourceClient;
    }

    public void setmDriveResourceClient(DriveResourceClient mDriveResourceClient) {
        this.mDriveResourceClient = mDriveResourceClient;
    }
}
