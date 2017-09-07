package de.jschmucker.bmon;

import android.app.Activity;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.VideoView;

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

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

public class MainActivity extends Activity {

    private final String videoUrl = "http://bmonpi:8090/";
    private final String audioUrl = "http://bmonpi:8000/raspi";


    private static final String PORT = "48800";
    private static final String HOST = "bmonpi";
    //private TcpClient tcpClient = null;
    private UdpBroadcastReceiver broadcastReceiver;

    private SimpleExoPlayer player;
    private MediaSource audioSource;
    private MjpegView mjpegView;
    private TextView textViewTemp;
    private TextView textViewHumidity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Window window = getWindow();
        window.setStatusBarColor(Color.BLACK);

        textViewHumidity = (TextView) findViewById(R.id.textViewHumidity);
        textViewTemp = (TextView) findViewById(R.id.textViewTemp);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.offButton);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        /*
        Init Video Stream
         */
        mjpegView = (MjpegView) findViewById(R.id.mjpeg_view);
        mjpegView.setDisplayMode(MjpegView.SIZE_BEST_FIT);

        NetworkConnection connection = new NetworkConnection(mjpegView);
        connection.execute(videoUrl);

        /*
        Init Audio Stream
         */
        initAudioStream();
    }

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

    @Override
    protected void onDestroy() {
        player.release();
        super.onDestroy();
    }

    @Override
    protected void onPause() {

        /*if (tcpClient != null) {
            tcpClient.cancel(true);
            tcpClient = null;
        }*/
        broadcastReceiver.stop();
        broadcastReceiver = null;

        mjpegView.stopPlayback();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        mjpegView.startPlayback();

        broadcastReceiver = new UdpBroadcastReceiver(textViewHumidity, textViewTemp);
        broadcastReceiver.execute("255.255.255.255", "54545");
        /*if (tcpClient == null) {
            tcpClient = new TcpClient(textViewTemp, textViewHumidity);
            tcpClient.execute(HOST, PORT);
        }*/
    }
}
