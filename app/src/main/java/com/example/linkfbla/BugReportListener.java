package com.example.linkfbla;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

import androidx.appcompat.app.AppCompatActivity;

public class BugReportListener implements SensorEventListener {
    private static final int SHAKE_THRESHOLD = 2000;

    private long lastUpdate = 0;
    // to calculate change in speed
    private float last_x;
    private float last_y;
    private float last_z;
    private boolean dialogVisible;

    private AppCompatActivity activity;

    public BugReportListener(AppCompatActivity activity) {
        this.activity = activity;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        long curTime = System.currentTimeMillis();
        // only allow one update every 100ms.
        if ((curTime - lastUpdate) > 100) {
            long diffTime = (curTime - lastUpdate);
            lastUpdate = curTime;

            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            float speed = Math.abs(x+y+z - last_x - last_y - last_z) / diffTime * 10000;

            if (speed > SHAKE_THRESHOLD && !dialogVisible) {
                // Phone shaken, report bug
                BugReportDialog dialog = new BugReportDialog(() -> dialogVisible = false);
                dialog.show(activity.getSupportFragmentManager(), "bug report dialog");
                dialogVisible = true;
            }
            last_x = x;
            last_y = y;
            last_z = z;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }
}
