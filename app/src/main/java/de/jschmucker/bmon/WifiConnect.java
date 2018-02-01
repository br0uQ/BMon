package de.jschmucker.bmon;

import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

/**
 * Created by jschmucker
 * Class to connect to a wifi ap
 */

public class WifiConnect {
    private WifiManager wifiManager;
    private int netId;

    WifiConnect(WifiManager wifiManager)
    {
        this.wifiManager = wifiManager;
        wifiManager.setWifiEnabled(true);
    }

    public void aktiviereWLanModul()
    {

    }

    public boolean isWLanModulAktiv()
    {
        return wifiManager.isWifiEnabled();
    }
    void verbinden(String netzwerkName, String passwort)
    {
        // Pruefen ob WLAN Modul aktiv ist
        //while(!wifiManager.isWifiEnabled());

        // Verbindung mit Netzwerk aufbauen
        WifiConfiguration wifiConfig = new WifiConfiguration();
        wifiConfig.SSID = String.format("\"%s\"", netzwerkName);
        wifiConfig.preSharedKey = String.format("\"%s\"", passwort);

        netId = wifiManager.addNetwork(wifiConfig);
        wifiManager.disconnect();
        wifiManager.enableNetwork(netId, true);
        wifiManager.reconnect();
    }

    boolean isVerbindungAktiv()
    {
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        return  ((WifiInfo.getDetailedStateOf(wifiInfo.getSupplicantState()) == NetworkInfo.DetailedState.CONNECTED));
    }

    void removeWifi() {
        if (!wifiManager.removeNetwork(netId)) {
            Log.d(getClass().getSimpleName(), "Could not remove Wifi");
        }
    }
}
