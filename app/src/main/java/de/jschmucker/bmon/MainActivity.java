package de.jschmucker.bmon;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.design.widget.FloatingActionButton;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveVideoTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.util.List;

public class MainActivity extends Activity {
    private final String BMON_AP_IP = "192.168.2.1";
    private final String BMON_HOSTNAME = "bmonpi";
    private final String BMON_AP_NAME = "SchmuckerBmonAP";

//    private String bmon_address = BMON_AP_IP;

    /* Audio */
    private String audioUrl = "http://192.168.2.1:8000/raspi";
    private SimpleExoPlayer player;
    private MediaSource audioSource;

    /* Video */
    private String videoUrl = "http://192.168.2.1:8090/";
    private MjpegView mjpegView;

    /* Temperatur and Humidity Receiver */
    private UdpBroadcastReceiver udpBroadcastReceiver;
    private RelativeLayout tempHumidLayout;
    private TextView textViewTemp;
    private TextView textViewHumidity;
    private static final String PORT = "54545";


    /**********************************************************************************************\
     * Activity overrides
     **********************************************************************************************/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Window window = getWindow();
        window.setStatusBarColor(Color.BLACK);
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //initAdress();

        textViewHumidity = (TextView) findViewById(R.id.textViewHumidity);
        textViewTemp = (TextView) findViewById(R.id.textViewTemp);
        tempHumidLayout = (RelativeLayout) findViewById(R.id.temp_humid_layout);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.offButton);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.exit(0);
            }
        });

        /*
        Init Video Stream
         */
        mjpegView = (MjpegView) findViewById(R.id.mjpeg_view);
        mjpegView.setDisplayMode(MjpegView.SIZE_BEST_FIT);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Check WIFI Network and connect to BMonPi
        checkWiFiConnection();

        NetworkConnection connection = new NetworkConnection(mjpegView);
        connection.execute(videoUrl);

        /*
        Init Audio Stream
         */
        initAudioStream();
    }

    @Override
    protected void onStop() {
        mjpegView.stopPlayback();

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        player.release();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        udpBroadcastReceiver.stop();
        udpBroadcastReceiver = null;

        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        udpBroadcastReceiver = new UdpBroadcastReceiver(tempHumidLayout, textViewHumidity, textViewTemp);
        udpBroadcastReceiver.execute("255.255.255.255", PORT);

        mjpegView.startPlayback();
    }


    /**********************************************************************************************\
     * Connecting to BMon Wifi
     **********************************************************************************************/

    private void checkWiFiConnection() {
        WifiManager wifiManager =
                (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        String ssid = wifiInfo.getSSID();

        if (!ssid.equals(BMON_AP_NAME)) {
            searchForBmonWifi();
            connectToBmonWifi();
        }
    }

    private void searchForBmonWifi() {
        final WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                List<ScanResult> results;
                results = wifiManager.getScanResults();

                boolean found = false;
                for (ScanResult result : results) {
                    if (BMON_AP_NAME.equals(result.SSID)) {
                        found = true;
                        break;
                    }
                }
                if (found) {
                    connectToBmonWifi();
                } else {
                    wifiManager.startScan();
                }
            }
        };

        registerReceiver(wifiReceiver, new IntentFilter(
                WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        if(wifiManager.isWifiEnabled()==false)
        {
            wifiManager.setWifiEnabled(true);
        }
        wifiManager.startScan();
    }

    private void connectToBmonWifi() {
        String networkSSID = "test";
        String networkPass = "pass";

        // create wifi configuration
        WifiConfiguration conf = new WifiConfiguration();
        conf.SSID = "\"" + networkSSID + "\"";   // Please note the quotes. String should contain ssid in quotes

        // add passphrase to configuration
        conf.preSharedKey = "\""+ networkPass +"\"";

        // open network
        conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);

        // Add to Wifi manager
        WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        wifiManager.addNetwork(conf);

        // enable network in wifimanager
        List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
        for( WifiConfiguration i : list ) {
            if(i.SSID != null && i.SSID.equals("\"" + networkSSID + "\"")) {
                wifiManager.disconnect();
                wifiManager.enableNetwork(i.networkId, true);
                wifiManager.reconnect();

                break;
            }
        }
    }


    /**********************************************************************************************\
     * Initialize the Audio stream
     **********************************************************************************************/

    private void initAudioStream() {
        // 1. Create a default TrackSelector
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory =
                new AdaptiveVideoTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector =
                new DefaultTrackSelector(videoTrackSelectionFactory);

        // 2. Create a default LoadControl
        LoadControl loadControl = new DefaultLoadControl();

        // 3. Create the player
        player = ExoPlayerFactory.newSimpleInstance(this, trackSelector, loadControl);
        player.addListener(new AudioPlayerEventListener(this));

        // Measures bandwidth during playback. Can be null if not required.
        DefaultBandwidthMeter defaultBandwidthMeter = new DefaultBandwidthMeter();
        // Produces DataSource instances through which media data is loaded.
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this,
                Util.getUserAgent(this, getApplicationInfo().name), defaultBandwidthMeter);
        // Produces Extractor instances for parsing the media data.
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
        // This is the MediaSource representing the media to be played.
        audioSource = new ExtractorMediaSource(Uri.parse(audioUrl),
                dataSourceFactory, extractorsFactory, null, null);
        // Prepare the player with the source.

        player.setPlayWhenReady(true);

        player.prepare(audioSource);
        player.setVolume(1);
    }
}
