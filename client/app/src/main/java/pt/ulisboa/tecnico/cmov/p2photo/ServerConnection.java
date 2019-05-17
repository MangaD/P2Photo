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
import java.util.HashMap;

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

    /**
     * Returns private key in base64
     */
    public String[] login(String user, String password) throws IOException {
        if (!isConnected()) {
            Log.d("ServerConnection", "Not connected to the server.");
            return null;
        }
        write("login");
        write(user);
        write(password);
        Log.d("ServerConnection", "User: '" + user + "' Password: '" + password + "'.");
        String result = read();
        if (result.isEmpty()) {
            return null;
        }
        Log.d("ServerConnection", "Session ID: '" + result + "'.");
        try {
            int res = Integer.parseInt(result);
            if (res < 0) {
                return null;
            } else {
                this.sessionID = res;
                String[] pair = new String[2];
                String encPrivKeyBase64 = read();
                String pubKeyBase64 = read();
                pair[0] = encPrivKeyBase64;
                pair[1] = pubKeyBase64;
                Log.d("ServerConnection", "Session ID: '" + this.sessionID + "'.");
                return pair;
            }
        } catch (Exception e) {
            return null;
        }
    }

    public String signup(String user, String password, String pubKey, String privKey) throws IOException {
        if (!isConnected()) {
            Log.d("ServerConnection", "Not connected to the server.");
            return "Not connected to the server!";
        }
        write("signup");
        write(user);
        write(password);
        write(pubKey);
        write(privKey);
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

    public String setAlbumIndex(String name, String index, String base64key) throws IOException {
        if (!isConnected()) {
            Log.d("ServerConnection", "Not connected to the server.");
            return "Not connected to the server!";
        }
        write("setalbumindex");
        write(Integer.toString(sessionID));
        write(name);
        write(index);
        write(base64key);
        Log.d("ServerConnection", "Name: '" + name + "' Index: '" + index + "'.");
        String result = read();
        return result;
    }

    public String givePermission(String userName, String albumName, String index, String key) throws IOException {
        if (!isConnected()) {
            Log.d("ServerConnection", "Not connected to the server.");
            return "Not connected to the server!";
        }
        write("givepermission");
        write(Integer.toString(sessionID));
        write(userName);
        write(albumName);
        write(key);
        write(index);
        Log.d("ServerConnection", "User name: '" + userName +
                "' Album name: '" + albumName + "' Index: '" + index + "'.");
        String result = read();
        return result;
    }

    /**
     * Returns username and publicKey in Base64
     */
    public HashMap<String, String> getUsers() throws IOException {
        HashMap<String, String> list = new HashMap<>();
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
                list.put(s, read());
            }
        } catch (Exception e) { }

        return list;
    }

    /**
     * Returns username and publicKey in Base64
     */
    public HashMap<String, String> getUsersWithoutAlbumAccess(String albumName) throws IOException {
        HashMap<String, String> list = new HashMap<>();
        if (!isConnected()) {
            Log.d("ServerConnection", "Not connected to the server.");
            return null;
        }
        write("getuserswithoutalbumaccess");
        write(Integer.toString(sessionID));
        Log.d("ServerConnection", "Get users without album access.");
        write(albumName);

        try {
            String s;
            while ((s = read()) != null && !s.isEmpty()) {
                Log.d("ServerConnection", s);
                list.put(s, read());
            }
        } catch (Exception e) { }

        return list;
    }

    public HashMap<Integer, String[]> getUsersOwnedAlbums() throws IOException {
        HashMap<Integer, String[]> list = new HashMap<>();
        if (!isConnected()) {
            Log.d("ServerConnection", "Not connected to the server.");
            return null;
        }
        write("getusersownedalbums");
        write(Integer.toString(sessionID));
        Log.d("ServerConnection", "Get user's owned albums.");

        try {
            String s;
            while ((s = read()) != null && !s.isEmpty()) {
                String[] pair = new String[2];
                pair[0] = read();
                pair[1] = read();
                list.put(Integer.valueOf(s), pair);
                Log.d("ServerConnection", s);
            }
        } catch (Exception e) { }

        return list;
    }

    public HashMap<Integer, String> getUsersAllowedAlbums() throws IOException {
        HashMap<Integer, String> list = new HashMap<>();
        if (!isConnected()) {
            Log.d("ServerConnection", "Not connected to the server.");
            return null;
        }
        write("getusersallowedalbums");
        write(Integer.toString(sessionID));
        Log.d("ServerConnection", "Get user's allowed albums.");

        try {
            String s;
            while ((s = read()) != null && !s.isEmpty()) {
                list.put(Integer.valueOf(s), read());
                Log.d("ServerConnection", s);
            }
        } catch (Exception e) { }

        return list;
    }

    public String getAlbumKey(String name) throws IOException {
        if (!isConnected()) {
            Log.d("ServerConnection", "Not connected to the server.");
            return null;
        }
        write("getalbumkey");
        write(Integer.toString(sessionID));

        write(name);
        Log.d("ServerConnection", "Get album key.");

        String key = read();
        Log.d("ServerConnection", key);
        return key;
    }

    public ArrayList<String> getAlbumIndexes(String name) throws IOException {
        ArrayList<String> list = new ArrayList<>();
        if (!isConnected()) {
            Log.d("ServerConnection", "Not connected to the server.");
            return null;
        }
        write("getalbumindexes");
        write(Integer.toString(sessionID));

        write(name);
        Log.d("ServerConnection", "Get album indexes.");

        // Read urls
        try {
            String s;
            while (!(s = read()).isEmpty()) {
                list.add(s);
                Log.d("ServerConnection", s);
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
