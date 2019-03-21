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

package jp.hazuki.yuzubrowser.core.utility.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.MediaScannerConnection;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;

import jp.hazuki.yuzubrowser.core.utility.log.ErrorReport;
import jp.hazuki.yuzubrowser.core.utility.log.Logger;

public class FileUtils {
    private static final String TAG = "FileUtils";

    private FileUtils() {
        throw new UnsupportedOperationException();
    }

    @SuppressLint("SimpleDateFormat")
    public static String getTimeFileName() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        return sdf.format(c.getTime());
    }

    public static boolean deleteDirectoryContents(File folder) {
        if (folder == null)
            throw new NullPointerException("file is null");
        if (!folder.isDirectory()) return false;
        File[] list = folder.listFiles();
        if (list == null) {
            Logger.e(TAG, "File#listFiles returns null");
            return false;
        }
        for (File file : folder.listFiles()) {
            if (!deleteFile(file)) return false;
        }
        return true;
    }

    public static boolean deleteFile(File file) {
        if (file == null)
            throw new NullPointerException("file is null");
        if (file.isFile()) {
            return file.delete();
        } else if (file.isDirectory()) {
            File[] list = file.listFiles();
            if (list == null) {
                Log.e(TAG, "File#listFiles returns null");
                return false;
            }
            for (File f : list) {
                if (!deleteFile(f)) return false;
            }
            return file.delete();
        } else {
            return true;
        }
    }

    public static boolean copyFile(File srcFile, File folder) {
        folder.mkdirs();

        if (srcFile.isFile()) {
            return copySingleFile(srcFile, new File(folder, srcFile.getName()));
        } else {
            for (File f : srcFile.listFiles()) {
                if (!copyFile(f, new File(folder, srcFile.getName()))) return false;
            }
            return true;
        }
    }

    public static boolean copySingleFile(File srcFile, File destFile) {
        InputStream is = null;
        OutputStream os = null;

        try {
            is = new FileInputStream(srcFile);
            os = new FileOutputStream(destFile);

            int n;
            byte[] buf = new byte[1024];
            while ((n = is.read(buf)) >= 0) {
                os.write(buf, 0, n);
            }
            return true;
        } catch (IOException e) {
            ErrorReport.printAndWriteLog(e);
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    ErrorReport.printAndWriteLog(e);
                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    ErrorReport.printAndWriteLog(e);
                }
            }
        }

        return false;
    }

    public static void notifyImageFile(Context context, String... files) {
        MediaScannerConnection.scanFile(context, files, null, null);
    }

    public static String replaceProhibitionWord(String name) {
        return name
                .replace('<', '_')
                .replace('>', '_')
                .replace(':', '_')
                .replace('*', '_')
                .replace('?', '_')
                .replace('"', '_')
                .replace('\\', '_')
                .replace('/', '_')
                .replace('|', '_');
    }

    public static String getMineType(File file) {
        return FileUtilsKt.getMimeType(file.getName());
    }

    public static final FileComparator FILE_COMPARATOR = new FileComparator();

    public static class FileComparator implements Comparator<File> {
        @Override
        public int compare(File lhs, File rhs) {
            if (lhs.isDirectory() && !rhs.isDirectory())
                return -1;
            if (!lhs.isDirectory() && rhs.isDirectory())
                return 1;
            return lhs.getName().compareToIgnoreCase(rhs.getName());
        }
    }
}
