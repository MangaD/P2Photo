package pt.ulisboa.tecnico.cmov.p2photo;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

public class ServerConnection {

    /**
     * To access your PC localhost from Android emulator, use 10.0.2.2 instead of 127.0.0.1.
     * localhost or 127.0.0.1 refers to the emulated device itself, not the host the emulator is running on.
     * https://stackoverflow.com/questions/18341652/connect-failed-econnrefused
     */
    public static String addr = "10.0.2.2"; //null for loopback address
    public static int port = 12345;
    public static Socket conn;
    private DataOutputStream out;
    private DataInputStream in;

    int sessionID = -1;

    public void connect() throws IOException {
        if (!isConnected()) {
            conn = new Socket(addr, port);
            out = new DataOutputStream(conn.getOutputStream());
            in = new DataInputStream(conn.getInputStream());
        }
    }

    public boolean isConnected() {
        if (conn == null || !conn.isConnected() || conn.isClosed()) {
            return false;
        } else {
            return true;
        }
    }

    public void disconnect() {
        try {
            if (conn != null) {
                conn.close();
            }
        } catch (IOException e) {}
    }

    public boolean login(String user, String password) throws IOException {
        if (!isConnected()) {
            Log.d("ServerConnection", "Not connected to the server.");
            return false;
        }
        write("login");
        write(user);
        write(password);
        Log.d("ServerConnection", "User: '" + user + "' Password: '" + password + "'.");
        String result = read();
        Log.d("ServerConnection", "Session ID: '" + result + "'.");
        try {
            int res = Integer.parseInt(result);
            if (res < 0) {
                return false;
            } else {
                this.sessionID = res;
                Log.d("ServerConnection", "Session ID: '" + this.sessionID + "'.");
                return true;
            }
        } catch (Exception e) {
            return false;
        }
    }

    public String signup(String user, String password) throws IOException {
        if (!isConnected()) {
            Log.d("ServerConnection", "Not connected to the server.");
            return "Not connected to the server!";
        }
        write("signup");
        write(user);
        write(password);
        Log.d("ServerConnection", "User: '" + user + "' Password: '" + password + "'.");
        String result = read();
        return result;
    }

    public String createAlbum(String name) throws IOException {
        if (!isConnected()) {
            Log.d("ServerConnection", "Not connected to the server.");
            return "Not connected to the server!";
        }
        write("createalbum");
        write(Integer.toString(sessionID));
        write(name);
        Log.d("ServerConnection", "Name: '" + name + "'.");
        String result = read();
        return result;
    }

    public String setAlbumIndex(String name, String index) throws IOException {
        if (!isConnected()) {
            Log.d("ServerConnection", "Not connected to the server.");
            return "Not connected to the server!";
        }
        write("setalbumindex");
        write(Integer.toString(sessionID));
        write(name);
        write(index);
        Log.d("ServerConnection", "Name: '" + name + "' Index: '" + index + "'.");
        String result = read();
        return result;
    }

    public ArrayList<String> getUsers() throws IOException {
        ArrayList<String> list = new ArrayList<>();
        if (!isConnected()) {
            Log.d("ServerConnection", "Not connected to the server.");
            return null;
        }
        write("getusers");
        write(Integer.toString(sessionID));
        Log.d("ServerConnection", "Get users.");

        try {
            String s;
            while ((s = read()) != null && !s.isEmpty()) {
                Log.d("ServerConnection", s);
                list.add(s);
            }
        } catch (Exception e) { }

        return list;
    }

    public ArrayList<String> getUserAlbums() throws IOException {
        ArrayList<String> list = new ArrayList<>();
        if (!isConnected()) {
            Log.d("ServerConnection", "Not connected to the server.");
            return null;
        }
        write("getuseralbums");
        write(Integer.toString(sessionID));
        Log.d("ServerConnection", "Get user's albums.");

        try {
            String s;
            while ((s = read()) != null && !s.isEmpty()) {
                list.add(s);
            }
        } catch (Exception e) { }

        return list;
    }

    private String read() throws IOException {
        return in.readUTF().trim();
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
