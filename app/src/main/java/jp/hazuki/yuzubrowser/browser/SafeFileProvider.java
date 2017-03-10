package jp.hazuki.yuzubrowser.browser;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;

import jp.hazuki.yuzubrowser.utils.ErrorReport;
import jp.hazuki.yuzubrowser.utils.Logger;

public class SafeFileProvider extends ContentProvider {
    private static final String TAG = "SafeFileProvider";
    private static final String AUTHORITY = "jp.hazuki.yuzubrowser.browser.SafeFileProvider";
    //private static final String PROVIDER_URI = "content://" + AUTHORITY + "/";

    private String[] mAllowedFolder;

    @Override
    public boolean onCreate() {
        try {
            mAllowedFolder = new String[]{
                    getContext().getDir("public_html", Context.MODE_PRIVATE).getCanonicalPath(),
                    Environment.getExternalStorageDirectory().getCanonicalPath(),
            };
        } catch (IOException e) {
            ErrorReport.printAndWriteLog(e);
        }
        return true;
    }

    @Override
    public ParcelFileDescriptor openFile(@NonNull Uri uri, @NonNull String mode) throws FileNotFoundException {
        if (mAllowedFolder == null)
            return super.openFile(uri, mode);

        URI normalUri = URI.create(uri.buildUpon().scheme("file").authority("").build().toString()).normalize();
        File file = new File(normalUri);

        try {
            if (file.exists() && file.isFile() && file.canRead()) {
                String path = file.getCanonicalPath();

                for (String allowed : mAllowedFolder) {
                    if (path.regionMatches(true, 0, allowed, 0, allowed.length())) {
                        return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
                    }
                }

                Logger.w(TAG, "file exists but access not allowed");
                Logger.w(TAG, "path:" + path);
            }
        } catch (IOException e) {
            ErrorReport.printAndWriteLog(e);
        }
        return super.openFile(uri, mode);
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getType(@NonNull Uri uri) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }

    public static String convertToSaferUrl(String url) {
        URI normalUri = URI.create(Uri.parse(url).buildUpon().scheme("content").authority(AUTHORITY).build().toString()).normalize();
        return normalUri.toString();
    }

}
