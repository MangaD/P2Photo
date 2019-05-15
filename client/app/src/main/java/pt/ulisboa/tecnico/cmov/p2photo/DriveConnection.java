package pt.ulisboa.tecnico.cmov.p2photo;

import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.api.services.drive.Drive;

import java.util.ArrayList;

public class DriveConnection {

    public static final int REQUEST_CODE_SIGN_IN = 0;

    private GlobalClass context;

    private DriveClient driveClient;
    private DriveResourceClient driveResourceClient;
    private GoogleSignInAccount googleAccount;
    private GoogleSignInClient googleSignInClient;
    private com.google.api.services.drive.Drive service;

    private ArrayList<IndexAlbum> indexList = new ArrayList<>();
    private ArrayList<PhotoAlbum> albumList = new ArrayList<>();

    public DriveConnection(GlobalClass ctx) {
        this.context = ctx;
    }

    public void addAlbumToAlbumList(String albumName, DriveId driveId) {
        PhotoAlbum photoAlbum = new PhotoAlbum(albumName, driveId);
        albumList.add(photoAlbum);
    }

    public class PhotoAlbum {
        String name;
        DriveId driveid;
        ArrayList<PhotoImage> imagesArr;

        PhotoAlbum(String name, DriveId driveid) {
            this.name = name;
            this.driveid = driveid;
            this.imagesArr = new ArrayList<>();
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return getName();
        }
    }

    public PhotoAlbum findPhotoAlbum(String name) {
        Log.d("GlobalClass", Integer.toString(albumList.size()));
        for (PhotoAlbum photoalbum : albumList) {
            if (photoalbum.getName().equals(name)) {
                Log.d("GlobalClass", photoalbum.getName() + " " + name);
                return photoalbum;
            }
        }
        return null;
    }

    public class PhotoImage {
        String name;
        DriveId driveid;
        PhotoAlbum photoAlbum;
        IndexAlbum indexAlbum;

        PhotoImage(String name, DriveId driveid, PhotoAlbum pa, IndexAlbum ia) {
            this.name = name;
            this.driveid = driveid;
            this.photoAlbum = pa;
            this.indexAlbum = ia;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return getName();
        }
    }

    public class IndexAlbum {
        String name;
        DriveId driveid;

        IndexAlbum(String name, DriveId driveid) {
            this.name = name;
            this.driveid = driveid;
        }

        public String getName() {
            return this.name;
        }

        @Override
        public String toString() {
            return this.getName();
        }
    }

    public void addIndexToIndexList(String indexName, DriveId driveId) {
        IndexAlbum indexAlbum = new IndexAlbum(indexName, driveId);
        indexList.add(indexAlbum);
    }

    public void setDriveClient(DriveClient driveClient) {
        this.driveClient = driveClient;
    }

    public void setDriveResourceClient(DriveResourceClient driveResourceClient) {
        this.driveResourceClient = driveResourceClient;
    }

    public void setGoogleAccount(GoogleSignInAccount googleAccount) {
        this.googleAccount = googleAccount;
    }

    public void setGoogleSignInClient(GoogleSignInClient googleSignInClient) {
        this.googleSignInClient = googleSignInClient;
    }

    public void setService(Drive service) {
        this.service = service;
    }

    public DriveClient getDriveClient() {
        return driveClient;
    }

    public DriveResourceClient getDriveResourceClient() {
        return driveResourceClient;
    }

    public GoogleSignInAccount getGoogleAccount() {
        return googleAccount;
    }

    public GoogleSignInClient getGoogleSignInClient() {
        return googleSignInClient;
    }

    public Drive getService() {
        return service;
    }
}
