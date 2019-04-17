package pt.ulisboa.tecnico.cmov.p2photo;

import android.app.Application;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResourceClient;

import java.util.ArrayList;
import java.util.List;

public class GlobalClass extends Application {

    private ServerConnection serverConn;

    private DriveClient mDriveClient;
    private DriveResourceClient mDriveResourceClient;
    private GoogleSignInAccount account;

    private ArrayList<PhotoAlbum> albumList = new ArrayList<>();

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

    public GoogleSignInAccount getAccount(){ return this.account;}

    public void setAccount (GoogleSignInAccount acc){
        this.account = acc;
    }

    public ArrayList<PhotoAlbum> getAlbumList() {return albumList; }

    public void addAlbumToAlbumList(String albumName,DriveId driveId){
        PhotoAlbum photoAlbum = new PhotoAlbum(albumName,driveId);
        albumList.add(photoAlbum);
    }

    class PhotoAlbum{
        String name;
        DriveId driveid;
        PhotoAlbum(String name,DriveId driveid){
            this.name=name;
            this.driveid=driveid;
        }

        public DriveId getDriveid() {
            return driveid;
        }

        public String getName() {
            return name;
        }
        @Override
        public String toString(){
            return getName();
        }
    }

    PhotoAlbum findPhotoAlbum(String name) {
        for(PhotoAlbum photoalbum : albumList) {
            if(photoalbum.getName().equals(name)) {
                return photoalbum;
            }
        }
        return null;
    }
}
