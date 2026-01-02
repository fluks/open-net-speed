package com.jukkalaitinen.opennetspeed;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.NumberPicker;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
        new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (!isGranted) {
                Toast.makeText(this, R.string.no_notification_permission, Toast.LENGTH_LONG).show();
            }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        SharedPreferences prefs = getSharedPreferences(Global.PREFS_NAME, MODE_PRIVATE);
        SwitchCompat enableSwitch = findViewById(R.id.enable_switch);
        boolean enabled = prefs.getBoolean(Global.PREF_ENABLED, false);
        enableSwitch.setChecked(enabled);
        enableSwitch.setOnClickListener(v -> {
            boolean e = enableSwitch.isChecked();
            prefs.edit().putBoolean(Global.PREF_ENABLED, e).apply();
            if (e) {
                startService(prefs);
            }
            else {
                stopService();
            }
        });
        if (enabled) {
            startService(prefs);
        }
        else {
            stopService();
        }

        CheckBox startOnBootCheckbox = findViewById(R.id.start_on_boot_checkbox);
        startOnBootCheckbox.setChecked(prefs.getBoolean(Global.PREF_START_ON_BOOT, false));
        startOnBootCheckbox.setOnClickListener(v -> {
            boolean startOnBoot = startOnBootCheckbox.isChecked();
            prefs.edit().putBoolean(Global.PREF_START_ON_BOOT, startOnBoot).apply();
        });

        NumberPicker delayNumberPicker = findViewById(R.id.delay_numberpicker);
        delayNumberPicker.setMinValue(1);
        delayNumberPicker.setMinValue(1);
        delayNumberPicker.setMaxValue(10);
        delayNumberPicker.setValue(prefs.getInt(Global.PREF_DELAY, Global.DEFAULT_DELAY));
        delayNumberPicker.setOnValueChangedListener((picker, oldVal, newVal) ->
            prefs.edit().putInt(Global.PREF_DELAY, newVal).apply());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (!hasNotificationPermission(getApplicationContext())) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        }
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

    private Boolean hasNotificationPermission(Context context) {
        // For Android 13 (API 33) and above, check for POST_NOTIFICATIONS
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED;
        }
        // For older versions, notifications don't require a specific runtime permission
        // (though users can still disable them in settings).
        // So, we can consider it "granted" in terms of runtime checks.
        return true;
    }
}