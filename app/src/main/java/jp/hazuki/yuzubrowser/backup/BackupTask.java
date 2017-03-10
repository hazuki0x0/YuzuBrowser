package jp.hazuki.yuzubrowser.backup;

import android.content.AsyncTaskLoader;
import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import jp.hazuki.yuzubrowser.utils.ErrorReport;

/**
 * Created by hazuki on 17/01/25.
 */

public class BackupTask extends AsyncTaskLoader<Boolean> {

    private final File dest;

    public BackupTask(Context context, File dest) {
        super(context);
        this.dest = dest;
    }

    @Override
    public Boolean loadInBackground() {
        BackupManager manager = new BackupManager(getContext());
        try {
            if (!dest.getParentFile().exists()) {
                if (!dest.getParentFile().mkdirs()) {
                    return Boolean.FALSE;
                }
            }
            ZipOutputStream zipStream = new ZipOutputStream(new FileOutputStream(dest));
            URI root = manager.getRoot().toURI();
            byte[] buffer = new byte[8192];
            int len;
            InputStream in;
            String name;

            for (File item : manager.getBackUpFiles()) {
                in = new FileInputStream(item);
                name = root.relativize(item.toURI()).toString();
                zipStream.putNextEntry(new ZipEntry(name));
                while ((len = in.read(buffer)) > 0) {
                    zipStream.write(buffer, 0, len);
                }
                in.close();
            }
            zipStream.close();
            return Boolean.TRUE;
        } catch (IOException e) {
            ErrorReport.printAndWriteLog(e);
        }
        return Boolean.FALSE;
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
