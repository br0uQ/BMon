package de.jschmucker.bmon;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * Created by joshua on 16.03.17.
 */

class TcpClient extends AsyncTask<String,String,Boolean> {
    private Socket skt;
    private Scanner in;
    private TextView temp;
    private TextView humidity;

    TcpClient(TextView temp, TextView humidity) {
        this.temp = temp;
        this.humidity = humidity;
    }

    @Override
    protected Boolean doInBackground(String... params) {
        String host = params[0];
        int port = Integer.parseInt(params[1]);
        try {
            skt = new Socket(host, port);
            in = new Scanner(skt.getInputStream());
            PrintWriter out = new PrintWriter(skt.getOutputStream(), true);
            Log.d(getClass().getSimpleName(), "Send message to server");
            out.print("bmon_client");
            out.flush();

            while (!isCancelled()) {
                Log.d(getClass().getSimpleName(), "not yet cancelled");
                if (!isCancelled()) {
                    Log.d(getClass().getSimpleName(), "read next line");
                    String message = in.nextLine();
                    Log.d(getClass().getSimpleName(), "Read line: " + message);
                    String mesAr[] = message.split(":");
                    if (mesAr.length >= 2) {
                        float temp = Float.valueOf(mesAr[0]);
                        float humi = Float.valueOf(mesAr[1]);
                        mesAr[0] = String.valueOf(roundOneDigit(temp));
                        mesAr[1] = String.valueOf(roundOneDigit(humi));

                        publishProgress(mesAr[0], mesAr[1]);
                    } else {
                        Log.d(getClass().getSimpleName(),
                                "Wrong message received: length=" + mesAr.length);
                    }
                }
            }

            Log.d(getClass().getSimpleName(), "Close Connection");

            in.close();
            skt.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(String... progress) {
        super.onProgressUpdate(progress);
        temp.setText(progress[0]);
        humidity.setText(progress[1]);
    }

    private float roundOneDigit(float a) {
        int x = Math.round(a * 10);
        return (float) x / 10;
    }
}