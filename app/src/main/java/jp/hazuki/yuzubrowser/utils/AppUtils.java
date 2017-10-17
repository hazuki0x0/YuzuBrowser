package jp.hazuki.yuzubrowser.utils;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.view.View;

import java.util.Arrays;

import jp.hazuki.yuzubrowser.BrowserActivity;
import jp.hazuki.yuzubrowser.BuildConfig;
import jp.hazuki.yuzubrowser.Constants;
import jp.hazuki.yuzubrowser.R;

public class AppUtils {
    public static void restartApp(Context context) {
        restartApp(context, false);
    }

    public static void restartApp(Context context, boolean forceDestroy) {
        Intent intent = new Intent(context, BrowserActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent start = new Intent(context, BrowserActivity.class);
        start.setAction(BrowserActivity.ACTION_FINISH);
        start.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        start.putExtra(BrowserActivity.EXTRA_FORCE_DESTROY, forceDestroy);
        context.startActivity(start);
        manager.setExact(AlarmManager.RTC, System.currentTimeMillis() + 800, pendingIntent);
    }

    public static String getVersionDeviceInfo(Context context) {
        return "Yuzu " + BuildConfig.VERSION_NAME + "/" +
                Build.MANUFACTURER + "/" +
                Build.MODEL + "/" +
                Build.VERSION.RELEASE + "/" +
                "Chrome " + CrashlyticsUtils.getChromeVersion(context);
    }

    public static void registNotification(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager)
                    context.getSystemService(Context.NOTIFICATION_SERVICE);

            NotificationChannel service = new NotificationChannel(
                    Constants.notification.CHANNEL_DOWNLOAD_SERVICE,
                    context.getString(R.string.download_service),
                    NotificationManager.IMPORTANCE_MIN);

            service.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

            NotificationChannel notify = new NotificationChannel(
                    Constants.notification.CHANNEL_DOWNLOAD_NOTIFY,
                    context.getString(R.string.download_notify),
                    NotificationManager.IMPORTANCE_LOW);

            notify.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

            manager.createNotificationChannels(Arrays.asList(service, notify));
        }
    }

    public static boolean isRTL(Context context) {
        Configuration config = context.getResources().getConfiguration();
        return config.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
    }
}
