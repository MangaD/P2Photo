package pt.ulisboa.tecnico.cmov.p2photo;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ServerConnection {

    public static String addr = "127.0.0.1";
    public static int port = 4444;
    public static Socket conn;
    private DataOutputStream out;
    private DataInputStream in;

    public void connect() throws IOException {
        conn = new Socket(addr, port);
        out = new DataOutputStream(conn.getOutputStream());
        in = new DataInputStream(conn.getInputStream());
    }

    public boolean login(String user, String password) throws IOException {
        write("login");
        write(user);
        write(password);
        Log.d("ServerConnection", "User: '" + user + "' Password: '" + password + "'.");
        String result = read();
        return Boolean.valueOf(result);
    }

    private String read() throws IOException {
        return in.readUTF();
    }

    private void write(String message) throws IOException {
        out.writeUTF(message);
    }

    //Checking Internet is available or not
    public static boolean isOnline(Context ctx) {
        ConnectivityManager connMgr = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        else {
            return false;
        }
    }

}
