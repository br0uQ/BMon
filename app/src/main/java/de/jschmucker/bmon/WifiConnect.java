package de.jschmucker.bmon;

import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

/**
 * Created by PA-NB-JS on 01.02.2018.
 */

public class WifiConnect {
    private WifiManager wifiManager;

    public WifiConnect(WifiManager wifiManager)
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
    public void verbinden(String netzwerkName, String passwort)
    {
        // Pruefen ob WLAN Modul aktiv ist
        //while(!wifiManager.isWifiEnabled());

        // Verbindung mit Netzwerk aufbauen
        WifiConfiguration wifiConfig = new WifiConfiguration();
        wifiConfig.SSID = String.format("\"%s\"", netzwerkName);
        wifiConfig.preSharedKey = String.format("\"%s\"", passwort);

        int netId = wifiManager.addNetwork(wifiConfig);
        wifiManager.disconnect();
        wifiManager.enableNetwork(netId, true);
        wifiManager.reconnect();
    }

    public boolean isVerbindungAktiv()
    {
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        return  ((WifiInfo.getDetailedStateOf(wifiInfo.getSupplicantState()) == NetworkInfo.DetailedState.CONNECTED));
    }
}
