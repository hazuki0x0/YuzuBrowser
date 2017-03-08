package jp.hazuki.yuzubrowser;

import android.app.Application;
import android.os.Environment;

import java.io.File;

import jp.hazuki.yuzubrowser.settings.data.AppData;
import jp.hazuki.yuzubrowser.utils.ErrorReport;
import jp.hazuki.yuzubrowser.utils.Logger;

public class BrowserApplication extends Application {
    private static final String TAG = "BrowserApplication";
    public static final String PERMISSION_MYAPP_SIGNATURE = "jp.hazuki.yuzubrowser.permission.myapp.signature";
    private static BrowserApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();

        Logger.d(TAG, "onCreate()");
        instance = this;
        ErrorReport.initialize(this);
        AppData.load(this);
        ErrorReport.setDetailedLog(AppData.detailed_log.get());
    }

    public static File getExternalUserDirectory() {
        return new File(Environment.getExternalStorageDirectory() + File.separator + "YuzuBrowser" + File.separator);
    }

    public static BrowserApplication getInstance() {
        return instance;
    }
}
