package de.jschmucker.bmon;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Created by jschmucker on 15.08.17.
 */

public class UdpBroadcastReceiver extends AsyncTask<String, String, String> {
    private DatagramSocket socket;
    private boolean listen;
    private TextView humidityView;
    private TextView tempView;
    private RelativeLayout layout;

    public UdpBroadcastReceiver(RelativeLayout layout, TextView humidityTextView, TextView tempTextView) {
        humidityView = humidityTextView;
        tempView = tempTextView;
        this.layout = layout;
    }

    private void listenForBroadcast(InetAddress inetAddress, Integer port)
            throws IOException {
        byte[] recvBuf = new byte[15000];
        if (socket == null || socket.isClosed()) {
            socket = new DatagramSocket(port, inetAddress);
            socket.setBroadcast(true);
        }
        //socket.setSoTimeout(1000);
        DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);
        Log.d("UDP", "Waiting for UDP broadcast");
        socket.receive(packet);

        String senderIP = packet.getAddress().getHostAddress();
        String message = new String(packet.getData()).trim();

        Log.d("UDP", "Got UDB broadcast from " + senderIP + ", message: " + message);

        String mesAr[] = message.split(":");
        if (mesAr.length >= 2) {
            float temp = Float.valueOf(mesAr[0]);
            float humi = Float.valueOf(mesAr[1]);
            mesAr[0] = String.valueOf(roundOneDigit(temp));
            mesAr[1] = String.valueOf(roundOneDigit(humi));

            publishProgress(mesAr[0], mesAr[1]);
        } else {
            Log.e(getClass().getSimpleName(),
                    "Wrong message received: length=" + mesAr.length);
        }
        socket.close();
    }

    @Override
    protected String doInBackground(String... strings) {
        try {
            InetAddress broadcastIP = InetAddress.getByName(strings[0]);
            Integer port = Integer.parseInt(strings[1]);
            listen = true;

            while (listen) {
                listenForBroadcast(broadcastIP, port);
            }

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(String... progress) {
        super.onProgressUpdate(progress);
        if (layout.getVisibility() == View.INVISIBLE) {
            layout.setVisibility(View.VISIBLE);
        }
        tempView.setText(progress[1]);
        humidityView.setText(progress[0]);
    }

    public void stop() {
        listen = false;
    }

    private float roundOneDigit(float a) {
        int x = Math.round(a * 10);
        return (float) x / 10;
    }
}
