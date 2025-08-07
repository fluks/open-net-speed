package com.jukkalaitinen.statusbarnetspeed;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.net.TrafficStats;
import android.os.Build;
import android.os.IBinder;
import android.app.Notification;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class NetSpeedService extends Service {
    private static final int NOTIFICATION_ID = 1;
    private Handler handler;
    private Runnable updateRunnable;
    private int delay = Global.DEFAULT_DELAY * 1000;
    private long lastUpdate = 0;
    private long lastRX = 0;
    private long lastTX = 0;
    private static final String TAG = "NetSpeedService";

    public NetSpeedService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        startForeground(NOTIFICATION_ID, getNotification("Starting updates..."));
        
        handler = new Handler(Looper.getMainLooper());
        updateRunnable = new Runnable() {
            @Override
            public void run() {
                long rx = TrafficStats.getTotalRxBytes();
                long tx = TrafficStats.getTotalTxBytes();
                long now = System.currentTimeMillis();
                double delta = now - lastUpdate;
                Log.d(TAG, "" + delta);
                if (delta == 0) {
                    delta = 1;
                }
                else {
                    delta /= 1000;
                }
                Log.d(TAG, "" + delta);
                // Not first time.
                if (lastUpdate != 0) {
                    String rxDelta = formatBytes((rx - lastRX) / delta);
                    String txDelta = formatBytes((tx - lastTX) / delta);
                    String message = String.format("↓%s↑%s", rxDelta, txDelta);
                    updateNotification(message);
                }

                lastRX = rx;
                lastTX = tx;
                lastUpdate = now;
                handler.postDelayed(this, delay);
            }
        };
        handler.post(updateRunnable);
    }

    private String formatBytes(double bytesPerSec) {
        if (bytesPerSec < 1024) {
            return bytesPerSec + "B";
        }
        int exp = (int) (Math.log(bytesPerSec) / Math.log(1024));
        String unit = "KMGTPE".charAt(exp-1) + "B";

        return String.format("%.1f%s/s", bytesPerSec / Math.pow(1024, exp), unit);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            delay = intent.getIntExtra(Global.SERVICE_KEY_DELAY, Global.DEFAULT_DELAY) * 1000;
        }

        return START_STICKY; // Ensures the service restarts if killed by the system
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacks(updateRunnable);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void updateNotification(String message) {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.notify(NOTIFICATION_ID, getNotification(message));
    }

    private Notification getNotification(String message) {
        return new NotificationCompat.Builder(this, "status_channel")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("StatusBarNetSpeed")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "status_channel",
                    "Status Updates",
                    NotificationManager.IMPORTANCE_LOW
            );
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
    }
}