package pt.ulisboa.tecnico.cmov.p2photo;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ServerConnection {

    /**
     * To access your PC localhost from Android emulator, use 10.0.2.2 instead of 127.0.0.1.
     * localhost or 127.0.0.1 refers to the emulated device itself, not the host the emulator is running on.
     * https://stackoverflow.com/questions/18341652/connect-failed-econnrefused
     */
    public static String addr = "10.0.2.2"; //null for loopback address
    public static int port = 4444;
    public static Socket conn;
    private DataOutputStream out;
    private BufferedReader in;

    public void connect() throws IOException {
        if (conn == null || !conn.isConnected() || conn.isClosed()) {
            conn = new Socket(addr, port);
            out = new DataOutputStream(conn.getOutputStream());
            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        }
    }

    public void disconnect() {
        try {
            conn.close();
        } catch (IOException e) {}
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
        return in.readLine().trim();
    }

    private void write(String message) throws IOException {
        out.writeUTF(message + "\n");
    }

    public String getAddress() {
        return conn.getRemoteSocketAddress().toString();
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
