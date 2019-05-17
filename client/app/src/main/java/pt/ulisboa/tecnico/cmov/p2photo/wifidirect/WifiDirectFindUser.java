package pt.ulisboa.tecnico.cmov.p2photo.wifidirect;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import pt.ulisboa.tecnico.cmov.p2photo.GlobalClass;
import pt.ulisboa.tecnico.cmov.p2photo.R;
import pt.ulisboa.tecnico.cmov.p2photo.activities.CreateAlbumActivity;
import pt.ulisboa.tecnico.cmov.p2photo.activities.FindUserActivity;

import static android.os.Looper.getMainLooper;

public class WifiDirectFindUser {
    private static final String TAG = "WIFI FIND USER";
    private WeakReference<FindUserActivity> activityReference;
    private GlobalClass context;
    private String albumName;


    WifiManager wifiManager;
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;

    BroadcastReceiver mReceiver;
    IntentFilter mIntentFilter;

    List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
    String[] deviceNameArray;
    WifiP2pDevice[] deviceArray;

    static final int MESSAGE_READ = 1;

    ServerClass serverClass;
    ClientClass clientClass;
    SendReceive sendReceive;

    private ListView userListView;


    public WifiDirectFindUser(GlobalClass ctx, FindUserActivity activity, String albumName){

        activityReference = new WeakReference<>(activity);

        this.context = ctx;

        this.albumName = albumName;

        initialization();

        wifiListener();
    }

    private void initialization() {

        this.userListView = activityReference.get().findViewById(R.id.listUsers);

        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        mManager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(activityReference.get(),getMainLooper(),null);

        mReceiver = new WiFiDirectBroadcastReceiver(mManager,mChannel,activityReference.get(),this);

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

    }

    WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {
            if(!peerList.getDeviceList().equals(peers)){
                peers.clear();
                peers.addAll(peerList.getDeviceList());

                deviceNameArray = new String[peerList.getDeviceList().size()];
                deviceArray = new WifiP2pDevice[peerList.getDeviceList().size()];
                int index=0;
                for(WifiP2pDevice device : peerList.getDeviceList()){
                    deviceNameArray[index]=device.deviceName;
                    deviceArray[index]=device;
                    index++;
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,android.R.layout.simple_list_item_1,deviceNameArray);
                userListView.setAdapter(adapter);
            }
            if(peers.size()==0){
                Toast.makeText(context,"No Device Found",Toast.LENGTH_SHORT).show();
                return;
            }
        }
    };

    WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
            final InetAddress groupOwnerAddress = wifiP2pInfo.groupOwnerAddress;

            if(wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner){
                serverClass = new ServerClass();
                serverClass.start();
            } else if(wifiP2pInfo.groupFormed){
                clientClass = new ClientClass(groupOwnerAddress);
                clientClass.start();
            }
        }
    };

    public class ServerClass extends Thread{
        Socket socket;
        ServerSocket serverSocket;

        @Override
        public void run(){
            try{
                serverSocket = new ServerSocket(8888);
                socket=serverSocket.accept();

                sendReceive = new SendReceive(socket);
                sendReceive.start();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    private class SendReceive extends Thread{
        private Socket socket;
        private InputStream inputStream;
        private OutputStream outputStream;

        public SendReceive(Socket skt){
            socket=skt;
            try {
                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            while(socket!=null){
                try {
                    bytes=inputStream.read(buffer);
                    if(bytes>0){
                        handler.obtainMessage(MESSAGE_READ,bytes,-1,buffer).sendToTarget();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void write(byte[] bytes){
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public class ClientClass extends Thread{
        Socket socket;
        String hostAdd;
        public ClientClass(InetAddress hostAddress){
            hostAdd = hostAddress.getHostAddress();
            socket = new Socket();
        }
        @Override
        public void run(){
            try {
                socket.connect(new InetSocketAddress(hostAdd,8888),500);
                sendReceive = new SendReceive(socket);
                sendReceive.start();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what){
                case MESSAGE_READ:
                    byte[] readBuff= (byte[]) msg.obj;
                    String tempMsg = new String(readBuff,0,msg.arg1);
                    //read_msg_box.setText(tempMsg);
                    break;
            }
            return true;
        }
    });

    private void wifiListener() {
        Toast.makeText(context,"Wifi Listener",Toast.LENGTH_LONG).show();
        mManager.discoverPeers(mChannel,new WifiP2pManager.ActionListener(){


            @Override
            public void onSuccess() {

                Toast.makeText(context,"Discovery Started",Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(context,"Discovery Starting Failed",Toast.LENGTH_LONG).show();
            }
        });

        userListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final WifiP2pDevice device = deviceArray[i];
                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = device.deviceAddress;

                mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(context,"Connected to"+device.deviceName,Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int reason) {
                        Toast.makeText(context,"Not Connected",Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

}
