package de.jschmucker.bmon;

import android.os.AsyncTask;
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

public class NetworkConnection extends AsyncTask<String, Void, Void> {
    MjpegInputStream inputStream = null;
    MjpegView view;

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
            publishProgress();
        } catch (ClientProtocolException e) {
            Log.e("MainActivity", "ClientProtocolException");
        } catch (IOException e) {
            Log.e("MainActivity", e.toString());
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Void... progress) {
        if (inputStream != null) {
            view.setSource(inputStream);
        }
    }
}
