/*
 * Copyright (C) 2017-2019 Hazuki
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jp.hazuki.yuzubrowser.legacy.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.view.View;

import java.util.Arrays;

import jp.hazuki.yuzubrowser.core.utility.extensions.ContextExtensionsKt;
import jp.hazuki.yuzubrowser.legacy.Constants;
import jp.hazuki.yuzubrowser.legacy.R;
import jp.hazuki.yuzubrowser.legacy.settings.data.AppData;

public class AppUtils {
    public static void restartApp(Context context) {
        restartApp(context, false);
    }

    public static void restartApp(Context context, boolean forceDestroy) {
        Intent start = new Intent();
        start.setClassName(context, Constants.activity.MAIN_BROWSER);
        start.setAction(Constants.intent.ACTION_FINISH);
        start.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        start.putExtra(Constants.intent.EXTRA_FORCE_DESTROY, forceDestroy);
        context.startActivity(start);
    }

    public static String getVersionDeviceInfo(Context context) {
        return "Yuzu " + ContextExtensionsKt.getVersionName(context) + "/" +
                Build.MANUFACTURER + "/" +
                Build.MODEL + "/" +
                Build.VERSION.RELEASE + "/" +
                getWebViewMode() + "/" +
                "Chrome " + CrashlyticsUtils.getChromeVersion(context);
    }

    private static String getWebViewMode() {
        if (AppData.fast_back.get()) {
            int size = AppData.fast_back_cache_size.get();
            if (size == 0) {
                return "I";
            } else if (size > 1) {
                return "L";
            }
        }
        return "N";
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
