package de.jschmucker.bmon;

import android.util.Log;

public class Watchdog implements Runnable {
    private Thread thread = null;
    private int time;
    private volatile int counter;
    private boolean keepRunning;

    public Watchdog(int time) {
        this.time = time;
    }

    public void setWatchdog() {
        counter = time;
    }

    public void startWatchdog() {
        if (thread != null) {
            thread = new Thread(this);
            keepRunning = true;
            counter = time;
            thread.start();
        } else {
            Log.d(getClass().getSimpleName(), "Watchdog already running");
        }
    }

    public void stopWatchdog() {
        keepRunning = false;
    }

    @Override
    public void run() {
        while (keepRunning) {
            try {
                Thread.sleep(1000); // sleep 1 second
            }

            counter--;
            if (counter <= 0) {
                // time is over
            }
        }
    }
}
