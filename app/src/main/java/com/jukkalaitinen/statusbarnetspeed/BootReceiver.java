package com.jukkalaitinen.statusbarnetspeed;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import static android.content.Context.MODE_PRIVATE;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Intent serviceIntent = new Intent(context, NetSpeedService.class);
            SharedPreferences prefs = context.getSharedPreferences(Global.PREFS_NAME, MODE_PRIVATE);
            serviceIntent.putExtra(Global.SERVICE_KEY_DELAY, prefs.getInt(Global.PREF_DELAY, Global.DEFAULT_DELAY));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent);
            }
            else {
                context.startService(serviceIntent);
            }
        }
    }
}