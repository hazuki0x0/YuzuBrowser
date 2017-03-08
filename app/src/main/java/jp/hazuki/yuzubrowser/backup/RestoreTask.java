package jp.hazuki.yuzubrowser.backup;

import android.content.AsyncTaskLoader;
import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import jp.hazuki.yuzubrowser.utils.ErrorReport;
import jp.hazuki.yuzubrowser.utils.FileUtils;

/**
 * Created by hazuki on 17/01/25.
 */

public class RestoreTask extends AsyncTaskLoader<Boolean> {

    private File zip;

    public RestoreTask(Context context, File item) {
        super(context);
        zip = item;
    }

    @Override
    public Boolean loadInBackground() {
        BackupManager manager = new BackupManager(getContext());
        File root = manager.getRoot();
        manager.cleanFiles();
        OutputStream os = null;
        ZipInputStream zipStream = null;
        ZipEntry entry;

        try {
            zipStream = new ZipInputStream(new FileInputStream(zip));

            byte[] buffer = new byte[8192];
            int len;
            File file;

            while ((entry = zipStream.getNextEntry()) != null) {
                file = new File(root, entry.getName());
                if (entry.isDirectory()) {
                    if (file.exists()) {
                        FileUtils.deleteFile(file);
                    }
                    file.mkdir();
                } else {
                    if (file.exists()) {
                        FileUtils.deleteFile(file);
                    } else if (!file.getParentFile().exists()) {
                        file.getParentFile().mkdirs();
                    }

                    os = new FileOutputStream(file);

                    while ((len = zipStream.read(buffer)) > 0) {
                        os.write(buffer, 0, len);
                    }
                    os.close();
                    os = null;
                }
            }
            return true;
        } catch (IOException e) {
            ErrorReport.printAndWriteLog(e);
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (zipStream != null) {
                try {
                    zipStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }
}
