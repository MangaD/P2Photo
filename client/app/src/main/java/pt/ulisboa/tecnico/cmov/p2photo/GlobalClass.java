package pt.ulisboa.tecnico.cmov.p2photo;

import android.app.Application;

import java.security.PrivateKey;
import java.security.PublicKey;

public class GlobalClass extends Application {

    private ServerConnection serverConn;
    private DriveConnection driveConn;
    private String storageMode;

    private String keyPassword;

    private PublicKey pubKey;
    private PrivateKey privKey;

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

    public PrivateKey getPrivKey() {
        return this.privKey;
    }

    public void setPrivKey(PrivateKey privKey) {
        this.privKey = privKey;
    }

    public PublicKey getPubKey() {
        return pubKey;
    }

    public void setPubKey(PublicKey pubKey) {
        this.pubKey = pubKey;
    }

    public String getKeyPassword() {
        return keyPassword;
    }

    public void setKeyPassword(String keyPassword) {
        this.keyPassword = keyPassword;
    }
}
