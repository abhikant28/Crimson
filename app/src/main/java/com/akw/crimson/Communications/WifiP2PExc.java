package com.akw.crimson.Communications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import com.akw.crimson.MainActivity;

public class WifiP2PExc extends BroadcastReceiver {

    WifiManager wifiManager;
    WifiP2pManager wifiP2pManager;
    WifiP2pManager.Channel mchannel;
    MainActivity mainActivity;

    @Override
    public void onReceive(Context context, Intent intent) {

        String action= intent.getAction();

        if(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)){

        }else if(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)){

        }else if(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)){

        }else if(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)){

        }
    }


    public WifiP2PExc(Context mContext, WifiP2pManager wifiP2pManager, WifiP2pManager.Channel mchannel, MainActivity mainActivity) {
        this.wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        this.mainActivity=mainActivity;
        this.mchannel=mchannel;
        this.wifiP2pManager=wifiP2pManager;
    }

    public void switchWifi(boolean set){
        wifiManager.setWifiEnabled(set);
        Log.i("COMMUNICATOR:::::::::::", "Wifi_"+set);
    }


}
