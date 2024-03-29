package pt.ulisboa.tecnico.cmov.p2photo.wifidirect;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.widget.Toast;

import pt.ulisboa.tecnico.cmov.p2photo.activities.FindUserWifiActivity;

public class WiFiDirectBroadcastReceiver extends BroadcastReceiver{
    private  WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private FindUserWifiActivity mActivity;

    public WiFiDirectBroadcastReceiver(WifiP2pManager mManager, WifiP2pManager.Channel mChannel, FindUserWifiActivity mainActivity){
        this.mManager=mManager;
        this.mChannel = mChannel;
        this.mActivity = mainActivity;
    }
    @Override
    public void onReceive(Context context, Intent intent){
        String action = intent.getAction();
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)){
            //Check to see if Wi-Fi is enabled and notify appropriate activity
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE,-1);
            if(state==WifiP2pManager.WIFI_P2P_STATE_ENABLED){
                Toast.makeText(context,"Wifi is ON",Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(context,"Wifi is OFF",Toast.LENGTH_SHORT).show();
            }
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)){
            //Call WifiP2pManager.requestPeers() to get a list of current peers
            if(mManager!=null){
                mManager.requestPeers(mChannel,mActivity.peerListListener);
            }
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)){
            //Respond to new connection or disconnections
            if(mManager==null){
                return;
            }
            NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

            if(networkInfo.isConnected()){
                mManager.requestConnectionInfo(mChannel,mActivity.connectionInfoListener);
            } else {
                Toast.makeText(mActivity.getApplicationContext(),"device disconnected",Toast.LENGTH_SHORT);
            }
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)){
            //Respond to this device's Wi-Fi state changing
        }
    }
}