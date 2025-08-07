package com.jukkalaitinen.statusbarnetspeed;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.telephony.NetworkScan;
import android.view.View;
import android.widget.CheckBox;
import android.widget.NumberPicker;
import android.widget.Switch;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        SharedPreferences prefs = getSharedPreferences(Global.PREFS_NAME, MODE_PRIVATE);
        Switch enableSwitch = findViewById(R.id.enable_switch);
        enableSwitch.setChecked(prefs.getBoolean(Global.PREF_ENABLED, false));
        enableSwitch.setOnClickListener(v -> {
            boolean enabled = enableSwitch.isChecked();
            prefs.edit().putBoolean(Global.PREF_ENABLED, enabled).apply();
            if (enabled) {
                startService(prefs);
            }
            else {
                stopService();
            }
        });

        CheckBox startOnBootCheckbox = findViewById(R.id.start_on_boot_checkbox);
        startOnBootCheckbox.setChecked(prefs.getBoolean(Global.PREF_START_ON_BOOT, false));
        startOnBootCheckbox.setOnClickListener(v -> {
            boolean startOnBoot = startOnBootCheckbox.isChecked();
            prefs.edit().putBoolean(Global.PREF_START_ON_BOOT, startOnBoot).apply();
        });

        NumberPicker delayNumberPicker = findViewById(R.id.delay_numberpicker);
        delayNumberPicker.setMinValue(1);
        delayNumberPicker.setMinValue(0);
        delayNumberPicker.setMaxValue(100);
        delayNumberPicker.setValue(prefs.getInt(Global.PREF_DELAY, Global.DEFAULT_DELAY));
        delayNumberPicker.setOnValueChangedListener((picker, oldVal, newVal) -> {
            prefs.edit().putInt(Global.PREF_DELAY, newVal).apply();
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void stopService() {
        Intent serviceIntent = new Intent(this, NetSpeedService.class);
        stopService(serviceIntent);
    }

    private void startService(SharedPreferences prefs) {
        Intent serviceIntent = new Intent(this, NetSpeedService.class);
        serviceIntent.putExtra(Global.SERVICE_KEY_DELAY, prefs.getInt(Global.PREF_DELAY, Global.DEFAULT_DELAY));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent); // Required for Android 8.0+
        }
        else {
            startService(serviceIntent);
        }
    }
}