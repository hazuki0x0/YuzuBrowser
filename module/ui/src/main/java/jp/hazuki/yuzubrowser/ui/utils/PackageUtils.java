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

package jp.hazuki.yuzubrowser.ui.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.LabeledIntent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Parcelable;
import android.text.TextUtils;
import android.webkit.URLUtil;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.core.content.pm.ShortcutInfoCompat;
import androidx.core.content.pm.ShortcutManagerCompat;
import androidx.core.graphics.drawable.IconCompat;
import jp.hazuki.yuzubrowser.core.utility.utils.ImageUtils;
import jp.hazuki.yuzubrowser.ui.BrowserApplication;
import jp.hazuki.yuzubrowser.ui.ConstantsKt;
import jp.hazuki.yuzubrowser.ui.R;
import jp.hazuki.yuzubrowser.ui.provider.ISafeFileProvider;

public class PackageUtils {
    private PackageUtils() {
        throw new UnsupportedOperationException();
    }

    public static boolean isPermissionDerivedFromMyPackage(Context context, String permssion_name) {
        PackageManager pm = context.getPackageManager();
        try {
            return context.getApplicationInfo().packageName.equals(pm.getPermissionInfo(permssion_name, 0).packageName);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static Intent createChooser(Context context, String url, CharSequence title) {
        Intent query = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        return createChooser(context, query, title);
    }

    public static Intent createChooser(Context context, Intent query, CharSequence title) {
        PackageManager manager = context.getPackageManager();
        int flag;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flag = PackageManager.MATCH_ALL;
        } else {
            flag = PackageManager.MATCH_DEFAULT_ONLY;
        }

        List<ResolveInfo> infoList = manager.queryIntentActivities(query, flag);
        List<Intent> intents = new ArrayList<>();
        String appId = context.getPackageName();

        Collections.sort(infoList, new ResolveInfo.DisplayNameComparator(manager));

        Intent appIntent;

        for (ResolveInfo info : infoList) {
            if (appId.equals(info.activityInfo.packageName)) {
                continue;
            }
            ActivityInfo activityInfo = info.activityInfo;
            appIntent = new Intent(query);
            appIntent.setPackage(activityInfo.packageName);
            appIntent.setComponent(new ComponentName(activityInfo.packageName, activityInfo.name));

            LabeledIntent intent = new LabeledIntent(appIntent, activityInfo.packageName, info.labelRes, info.icon);
            intents.add(intent);
        }

        Intent chooser;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M || intents.size() == 0) {
            chooser = Intent.createChooser(new Intent(), title);
        } else {
            chooser = Intent.createChooser(intents.remove(0), title);
        }
        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intents.toArray(new Parcelable[intents.size()]));
        return chooser;
    }

    public static void createShortcut(Context context, String title, String url, Bitmap favicon) {
        if (url != null && !TextUtils.isEmpty(title) && ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
            Intent target = new Intent();
            target.setClassName(context, ConstantsKt.ACTIVITY_MAIN_BROWSER);
            target.setAction(Intent.ACTION_VIEW);
            if (URLUtil.isFileUrl(url)) {
                ISafeFileProvider provider = ((BrowserApplication)context.getApplicationContext()).getProviderManager().getSafeFileProvider();
                url = provider.convertToSaferUrl(url);
            }
            target.setData(Uri.parse(url));

            IconCompat icon;

            if (favicon != null) {
                icon = IconCompat.createWithBitmap(ImageUtils.trimSquare(favicon, 192));
            } else {
                icon = IconCompat.createWithResource(context, R.mipmap.ic_link_shortcut);
            }

            ShortcutInfoCompat shortcutInfo = new ShortcutInfoCompat.Builder(context, Long.toString(System.currentTimeMillis()))
                    .setShortLabel(title)
                    .setIcon(icon)
                    .setIntent(target)
                    .build();

            ShortcutManagerCompat.requestPinShortcut(context, shortcutInfo, null);
        } else {
            Toast.makeText(context, R.string.failed, Toast.LENGTH_SHORT).show();
        }
    }
}
