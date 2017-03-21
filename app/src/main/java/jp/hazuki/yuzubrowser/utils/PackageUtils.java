package jp.hazuki.yuzubrowser.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.LabeledIntent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Parcelable;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jp.hazuki.yuzubrowser.download.DownloadFileProvider;

public class PackageUtils {
    private PackageUtils() {
        throw new UnsupportedOperationException();
    }

    public static Intent createFileOpenIntent(Context context, String filepath) {
        return createFileOpenIntent(context, new File(filepath));
    }

    public static Intent createFileOpenIntent(Context context, File file) {
        Intent openIntent = new Intent(Intent.ACTION_VIEW);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        Uri uri = DownloadFileProvider.getUriForFIle(file);
        openIntent.setDataAndType(uri, context.getContentResolver().getType(uri));

        openIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

        return openIntent;
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
}