package jp.hazuki.yuzubrowser.utils.appinfo;

import android.graphics.drawable.Drawable;

/**
 * Created by hazuki on 16/12/10.
 */

public class AppInfo {
    private String appName;
    private String packageName;
    private String className;
    private Drawable icon;

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }
}
