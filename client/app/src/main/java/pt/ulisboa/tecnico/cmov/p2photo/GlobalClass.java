package pt.ulisboa.tecnico.cmov.p2photo;

import android.app.Application;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResourceClient;

import java.util.ArrayList;

public class GlobalClass extends Application {

    private ServerConnection serverConn;

    private DriveClient mDriveClient;
    private DriveResourceClient mDriveResourceClient;
    private GoogleSignInAccount account;

    private ArrayList<PhotoAlbum> albumList = new ArrayList<>();
    private ArrayList<IndexAlbum> indexList = new ArrayList<>();
    private ArrayList<PhotoImage> photosList = new ArrayList<>();

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

    public class PhotoAlbum {
        String name;
        DriveId driveid;
        ArrayList<PhotoImage> imagesArr;

        PhotoAlbum(String name,DriveId driveid){
            this.name=name;
            this.driveid=driveid;
            this.imagesArr = new ArrayList<>();
        }

        public DriveId getDriveid() {
            return driveid;
        }
        public String getName() {
            return name;
        }
        public ArrayList<PhotoImage> getImagesArr(){ return imagesArr;}

        @Override
        public String toString(){
            return getName();
        }
    }

    public PhotoAlbum findPhotoAlbum(String name) {
        Log.d("GlobalClass", Integer.toString(albumList.size()));
        for(PhotoAlbum photoalbum : albumList) {
            if(photoalbum.getName().equals(name)) {
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
        String url;

        PhotoImage(String name,DriveId driveid, PhotoAlbum pa, IndexAlbum ia){
            this.name=name;
            this.driveid=driveid;
            this.photoAlbum = pa;
            this.indexAlbum = ia;
        }
        public DriveId getDriveID() {
            return driveid;
        }
        public String getName() {
            return name;
        }
        public String getURL(){ return url; }
        @Override
        public String toString(){
            return getName();
        }
    }

    public void addPhotoToPhotosList(String name, DriveId driveId, PhotoAlbum pa, IndexAlbum ia){
        PhotoImage pi = new PhotoImage(name, driveId, pa, ia);
        photosList.add(pi);
    }

    public ArrayList<PhotoImage> getPhotosList(){ return this.photosList;}

    public PhotoImage findPhotoImage(String name) {
        for(PhotoImage photoImage : photosList) {
            if(photoImage.getName().equals(name)) {
                return photoImage;
            }
        }
        return null;
    }

    public class IndexAlbum {
        String name;
        DriveId driveid;

        IndexAlbum(String name,DriveId driveid){
            this.name=name;
            this.driveid=driveid;
        }

        public DriveId getDriveid() {
            return this.driveid;
        }

        public String getName() {
            return this.name;
        }
        @Override
        public String toString(){
            return this.getName();
        }
    }

    public ArrayList<IndexAlbum> getIndexList() {return indexList; }

    public void addIndexToIndexList(String indexName,DriveId driveId){
        IndexAlbum indexAlbum = new IndexAlbum(indexName,driveId);
        indexList.add(indexAlbum);
    }

    public IndexAlbum findIndexAlbum(String name) {
        for(IndexAlbum indexalbum : indexList) {
            if(indexalbum.getName().equals(name)) {
                return indexalbum;
            }
        }
        return null;
    }
}
