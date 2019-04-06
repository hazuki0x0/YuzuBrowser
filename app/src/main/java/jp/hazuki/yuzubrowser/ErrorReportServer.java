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

package jp.hazuki.yuzubrowser;

import android.content.Context;
import android.os.Build;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.Thread.UncaughtExceptionHandler;

import jp.hazuki.yuzubrowser.core.utility.log.ErrorReport;
import jp.hazuki.yuzubrowser.core.utility.log.IErrorReport;
import jp.hazuki.yuzubrowser.core.utility.log.Logger;

public class ErrorReportServer implements UncaughtExceptionHandler, IErrorReport {
    private static final String TAG = "ErrorReportServer";
    private static final UncaughtExceptionHandler sDefHandler = Thread.getDefaultUncaughtExceptionHandler();
    private static boolean detailedLog = true;
    private static File filesDir;

    public static void initialize(Context context) {
        filesDir = context.getExternalFilesDir("");
        ErrorReport.INSTANCE.init(new ErrorReportServer());
        //Thread.setDefaultUncaughtExceptionHandler(new ErrorReportServer());
    }

    public static void setDetailedLog(boolean enable) {
        detailedLog = enable;
    }

    @Override
    public void setDetailedLogRemote(boolean enable) {
        setDetailedLog(enable);
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        writeErrorLog(ex, "UCE");
        sDefHandler.uncaughtException(thread, ex);
    }


    public static boolean printAndWriteLog(Throwable e) {
        e.printStackTrace();
        return detailedLog && writeErrorLog(e, "CE");
    }

    @Override
    public boolean printAndWriteLogRemote(Throwable e) {
        return printAndWriteLog(e);
    }

    private static boolean writeErrorLog(Throwable ex, String type) {
        if (filesDir == null) {
            Logger.e(TAG, "filesDir is null");
            return false;
        }

        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File file = new File(filesDir, "./error_log/" + System.currentTimeMillis() + ".txt");
            file.getParentFile().mkdirs();

            try (PrintWriter writer = new PrintWriter(new FileOutputStream(file))) {
                writer.printf("ERROR TYPE:%s\n", type);
                writer.printf("PACKAGE:%s\n", BuildConfig.APPLICATION_ID);
                writer.printf("VERSION:%s (%d)\n", BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE);
                writer.printf("BUILD:%s\n", BuildConfig.GIT_HASH);
                writer.printf("BUILD TIME:%s\n", BuildConfig.BUILD_TIME);
                writer.printf("BUILD TYPE:%s\n", BuildConfig.BUILD_TYPE);
                writer.printf("BUILD FLAVOR%s\n", BuildConfig.FLAVOR);
                writer.printf("MANUFACTURER:%s\n", Build.MANUFACTURER);
                writer.printf("DEVICE:%s\n", Build.DEVICE);
                writer.printf("MODEL:%s\n", Build.MODEL);
                writer.printf("PRODUCT:%s\n", Build.PRODUCT);
                writer.printf("SDK:%s (%d)\n", Build.VERSION.RELEASE, Build.VERSION.SDK_INT);
                writer.println("");
                ex.printStackTrace(writer);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
