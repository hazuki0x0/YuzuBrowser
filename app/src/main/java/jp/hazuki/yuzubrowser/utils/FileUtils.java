package jp.hazuki.yuzubrowser.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
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

public class FileUtils {
    private static final String TAG = "FileUtils";

    private FileUtils() {
        throw new UnsupportedOperationException();
    }

    public static class ParsedFileName {
        ParsedFileName(String prefix, String suffix) {
            Prefix = prefix;
            Suffix = suffix;
        }

        public String Prefix;
        public String Suffix;
    }

    public static ParsedFileName getParsedFileName(String fname) {
        int point = fname.lastIndexOf(".");
        if (point >= 0) {
            return new ParsedFileName(fname.substring(0, point), fname.substring(point + 1));
        } else {
            return new ParsedFileName(fname, null);
        }
    }

    public static boolean checkFileExists(String[] filelist, String filename) {
        for (String file : filelist) {
            if (filename.equalsIgnoreCase(file))
                return true;
        }
        return false;
    }

    public static ParsedFileName getParsedFileName(File file) {
        return getParsedFileName(file.getName());
    }

    public static String getFilePrefix(String fname) {
        int point = fname.lastIndexOf(".");
        if (point >= 0) {
            return fname.substring(0, point);
        } else {
            return fname;
        }
    }

    @SuppressLint("SimpleDateFormat")
    public static String getTimeFileName() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        return sdf.format(c.getTime());
    }

    public static String getFilePrefix(File file) {
        return getFilePrefix(file.getName());
    }

    public static String getFileSuffix(String fname) {
        int point = fname.lastIndexOf(".");
        if (point >= 0) {
            return fname.substring(point + 1);
        } else {
            return null;
        }
    }

    public static String getFileSuffix(File file) {
        return getFileSuffix(file.getName());
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

    public static boolean isImageFile(Context context, Uri uri) {
        String mineType = context.getContentResolver().getType(uri);
        return mineType != null && mineType.startsWith("image/");
    }

    public static void notifyImageFile(Context context, String... files) {
        MediaScannerConnection.scanFile(context, files, null, null);
    }

    public static File createUniqueFile(String folderPath, String fileName) {
        FileUtils.ParsedFileName pFname = FileUtils.getParsedFileName(fileName);
        int i = 1;
        final File folder = new File(folderPath);
        String[] fileList = folder.list();
        if (fileList != null) {
            StringBuilder builder = new StringBuilder();
            while (FileUtils.checkFileExists(fileList, fileName)) {
                builder.append(pFname.Prefix).append("-").append(i);
                if (pFname.Suffix != null) {
                    builder.append(".").append(pFname.Suffix);
                }
                fileName = builder.toString();
                ++i;
                builder.delete(0, builder.length());
            }
        }

        return new File(folder, fileName);
    }

    public static File replaceProhibitionWord(File file) {
        String fileName = file.getName();
        String newName = replaceProhibitionWord(fileName);
        if (fileName.equals(newName))
            return file;
        else
            return new File(file.getParentFile(), newName);
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

    public static final FileComparator FILE_COMPARATOR = new FileComparator();

    public static class FileComparator implements Comparator<File> {
        @Override
        public int compare(File lhs, File rhs) {
            if (lhs.isDirectory() && !rhs.isDirectory())
                return -1;
            if (!lhs.isDirectory() && rhs.isDirectory())
                return 1;
            return lhs.getName().compareTo(rhs.getName());
        }
    }
}
