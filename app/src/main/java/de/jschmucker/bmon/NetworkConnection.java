package de.jschmucker.bmon;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.net.URI;
import java.net.URL;

/**
 * Created by jschmucker on 09.08.17.
 */

public class NetworkConnection extends AsyncTask<String, String, Void> {
    private final String CONNECTION_SUCCESS = "CONNECTION_SUCCESS";
    private MjpegInputStream inputStream = null;
    private MjpegView view;

    public NetworkConnection(MjpegView mjpegView) {
        view = mjpegView;
    }

    @Override
    protected Void doInBackground(String... urls) {
        HttpResponse res;
        DefaultHttpClient httpclient = new DefaultHttpClient();
        try {
            res = httpclient.execute(new HttpGet(URI.create(urls[0])));
            inputStream = new MjpegInputStream(res.getEntity().getContent());
            publishProgress(CONNECTION_SUCCESS);
        } catch (ClientProtocolException e) {
            Log.e(getClass().getSimpleName(), "ClientProtocolException");
            publishProgress("ClientProtocolException");
        } catch (IOException e) {
            Log.e(getClass().getSimpleName(), e.toString());
            publishProgress("Konnte keine Verbindung zum Videostream herstellen.");

        }
        return null;
    }

    @Override
    protected void onProgressUpdate(String... progress) {
        if (progress[0] == CONNECTION_SUCCESS) {
            if (inputStream != null) {
                view.setSource(inputStream);
            }
        } else {
            showErrorDialog(progress[0]);
        }
    }

    private void showErrorDialog(String error) {
        // 1. Instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());

        // 2. Chain together various setter methods to set the dialog characteristics
        builder.setMessage(error)
                .setTitle(R.string.error_dialog_title);

        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
            }
        });

        // 3. Get the AlertDialog from create()
        AlertDialog dialog = builder.create();

        dialog.show();
    }
}
