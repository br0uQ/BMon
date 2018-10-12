package de.jschmucker.bmon;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;

/**
 * Created by joshua on 07.09.17.
 */

public class AudioPlayerEventListener implements ExoPlayer.EventListener {
    private Activity activity;

    public AudioPlayerEventListener(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest) {

    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
        if (trackSelections.length == 0)
        {
            restartApp("Audio Fehler: Playlist ist leer");
        }
    }

    @Override
    public void onLoadingChanged(boolean isLoading) {

    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        if ((playbackState == ExoPlayer.STATE_ENDED)
                || (playbackState == ExoPlayer.STATE_IDLE))
        {
            String state = "";
            if (playbackState == ExoPlayer.STATE_ENDED)
            {
                state = "STATE_ENDED";
            }
            if (playbackState == ExoPlayer.STATE_IDLE)
            {
                state = "STATE_IDLE";
            }
            restartApp("Audio Fehler: Player ist im Status: " + state);
        }
    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
        Log.d(getClass().getSimpleName(), "ExoPlaybackException");
        Log.d(getClass().getSimpleName(), error.toString());
        restartApp(activity.getString(R.string.error_audio_connection));
    }

    @Override
    public void onPositionDiscontinuity() {

    }

    private void restartApp(String crashReport) {
        Intent intent = new Intent(activity, MainActivity.class);
        intent.putExtra("crash", true);
        intent.putExtra(MainActivity.CRASH_REPORT_EXTRA, crashReport);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_CLEAR_TASK
                | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(MyApplication.getInstance().getBaseContext(), 0, intent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager mgr = (AlarmManager) MyApplication.getInstance().getBaseContext().getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, pendingIntent);
        activity.finish();
        System.exit(2);
    }
}
