package jp.hazuki.yuzubrowser.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import jp.hazuki.yuzubrowser.BrowserActivity;

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
}
