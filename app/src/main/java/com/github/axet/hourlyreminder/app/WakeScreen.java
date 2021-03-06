package com.github.axet.hourlyreminder.app;

import android.app.Notification;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.github.axet.androidlibrary.app.AlarmManager;
import com.github.axet.androidlibrary.app.NotificationManagerCompat;
import com.github.axet.androidlibrary.widgets.NotificationChannelCompat;
import com.github.axet.androidlibrary.preferences.OptimizationPreferenceCompat;
import com.github.axet.hourlyreminder.BuildConfig;
import com.github.axet.hourlyreminder.R;

public class WakeScreen {
    public static final String TAG = WakeScreen.class.getSimpleName();

    public static int ID = -5; // notification wake id

    public static final String DOZE_ENABLED = "doze_enabled"; // Bettery / Ambient Display / New notifications
    public static final String LOCK_SCREEN_SHOW_NOTIFICATIONS = "lock_screen_show_notifications"; // Security / Lock screen preferences / On the lock screen

    public static final String WAKEID = "wake";

    Context context;
    ContentResolver resolver;
    Notification n;
    NotificationManagerCompat nm;
    NotificationChannelCompat wake;
    PowerManager.WakeLock wl;
    PowerManager.WakeLock wlCpu;
    Handler handler = new Handler();
    Runnable wakeClose = new Runnable() {
        @Override
        public void run() {
            close();
        }
    };

    public static Notification build(Context context) {
        NotificationCompat.Builder b = new NotificationCompat.Builder(context);
        b.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        b.setSmallIcon(R.drawable.ic_launcher_notification);
        Notification n = b.build();
        NotificationChannelCompat.setChannelId(n, WAKEID);
        return n;
    }

    public static class ForceRemove extends Service { // sometimes notification not removed in time, force remove it with delay
        Handler handler = new Handler();

        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        @Override
        public void onCreate() {
            super.onCreate();
            startForeground(ID, build(this));
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    stopSelf();
                }
            }, 200);
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            stopForeground(true);
        }
    }

    public WakeScreen(Context context) {
        this.context = context;
        this.resolver = context.getContentResolver();
        this.nm = NotificationManagerCompat.from(context);
        this.wake = new NotificationChannelCompat(WAKEID, "Wake", NotificationManagerCompat.IMPORTANCE_HIGH);
        this.wake.setSound(null, null);
        this.wake.enableVibration(false);
        this.wake.create(context);
    }

    public boolean isDoze() {
        boolean doze = false;
        try {
            doze = Build.VERSION.SDK_INT >= 26 && Settings.Secure.getInt(resolver, DOZE_ENABLED) == 1 && Settings.Secure.getInt(resolver, LOCK_SCREEN_SHOW_NOTIFICATIONS) == 1 && wake.getImportance() >= NotificationManagerCompat.IMPORTANCE_HIGH;
        } catch (Settings.SettingNotFoundException ignore) {
        }
        return doze;
    }

    public void wake() {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn;
        if (Build.VERSION.SDK_INT >= 20)
            isScreenOn = pm.isInteractive();
        else
            isScreenOn = pm.isScreenOn();
        if (isScreenOn == false) {
            close();
            if (isDoze()) {
                n = build(context);
                nm.notify(ID, n);
                handler.post(wakeClose);
                OptimizationPreferenceCompat.startService(context, new Intent(context, ForceRemove.class));
                return;
            }
            wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, BuildConfig.APPLICATION_ID + ":wakelock");
            wl.acquire();
            wlCpu = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, BuildConfig.APPLICATION_ID + ":cpulock");
            wlCpu.acquire();
            handler.postDelayed(wakeClose, 3 * AlarmManager.SEC1); // old phones crash on handle wl.acquire(10000)
        }
    }

    public void close() {
        if (wl != null) {
            if (wl.isHeld())
                wl.release();
            wl = null;
        }
        if (wlCpu != null) {
            if (wlCpu.isHeld())
                wlCpu.release();
            wlCpu = null;
        }
        if (n != null) {
            nm.cancel(ID);
            n = null;
        }
        handler.removeCallbacks(wakeClose);
    }
}
